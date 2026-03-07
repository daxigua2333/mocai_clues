package io.github.daxigua2333.mocai_clues.component.system.discovery;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentFamilyRegistry;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractEntry;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractResult;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractState;
import io.github.daxigua2333.mocai_clues.component.data.discovery.cluebook.DetailWithCompleteness;
import io.github.daxigua2333.mocai_clues.data.ObjectsWithLocation;
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import io.github.daxigua2333.mocai_clues.data.location.FromClueBook;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class InteractSystem {
    private static final ComponentFamilyRegistry.SystemFamily FAMILY = ComponentFamilyRegistry.SystemFamily.INTERACT_SYSTEM;

    @SubscribeEvent
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
        if (!event.getLevel().isClientSide()) {
            processInteractEvent(
                    DataManager.Server.retrieveByBlockPos(event.getLevel(), event.getPos()),
                    InteractEntry.EntryType.CLICK_WITH_FINDER,
                    (ServerPlayer) event.getEntity()
            );
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
    }

    @SubscribeEvent
    private static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
        if (!event.getLevel().isClientSide()) {
            processInteractEvent(
                    DataManager.Server.retrieveByEntity(event.getTarget()),
                    InteractEntry.EntryType.CLICK_WITH_FINDER,
                    (ServerPlayer) event.getEntity()
            );
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
    }

    private final static Map<UUID, BlockPos> prevPos = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        BlockPos pos = player.getOnPos();

        if (player.level().isClientSide()) return;
        if (pos.equals(prevPos.get(player.getUUID()))) return;
        prevPos.put(player.getUUID(), pos);

        processInteractEvent(
                DataManager.Server.retrieveByBlockPos(player.level(), pos),
                InteractEntry.EntryType.WALK_ON,
                (ServerPlayer) player
        );
    }


    private static void processInteractEvent(ObjectsWithLocation ol, InteractEntry.EntryType entryType, ServerPlayer player) {
        for (ClueObject obj : ol.objects()) {
            if (!obj.hasFamily(FAMILY)) {
                continue;
            }

            // do have active behavior check
            InteractEntry eCompo = obj.getComponentOrThrow(ComponentType.INTERACT_ENTRY);
            if (!eCompo.isEnabled(entryType)) {
                continue;
            }
            // FinderState accessibility check
            InteractState bCompo = obj.getComponentOrThrow(ComponentType.INTERACT_STATE);
            if (!bCompo.isAccessible(player)) {
                continue;
            }

            InteractResult rCompo = obj.getComponentOrThrow(ComponentType.INTERACT_RESULT);
            for (InteractResult.ResultType type : rCompo.getEnabled()) {
                switch (type) {
                    case SEND_ITEM -> sendItem(player, obj, ol.location());
//                    case SEND_MANUAL_CLUE -> sendManualClue(player, obj);
                    case SEND_TO_CLUE_BOOK -> sendToClueBook(player, obj);
                }
            }

            bCompo.onFound();
            ol.location().markDirty(obj);
        }
    }

    private static void sendItem(ServerPlayer player, ClueObject obj, IRuntimeLocation location) {
        ItemClue iCompo = obj.getComponentOrThrow(ComponentType.ITEM_CLUE);
        ItemHandlerHelper.giveItemToPlayer(player, iCompo.getStack().copy());

        location.getHolder().remove(obj.getId());
        location.markDirty();
    }

    public static void sendToClueBook(ServerPlayer player, ClueObject obj) {
        // generate the copy in clue book
        ClueObject copy = switch (obj.type()) {
            case MANUAL -> createFromManual(obj);
            case CLUE_BOOK -> obj.copy();
            case null, default ->
                    throw new RuntimeException("Unimplemented ClueBook detail component converter of ClueObject#" + obj.getId());
        };

        FromClueBook location = new FromClueBook(player);
        var holder = location.getHolder();
        // TODO: merge logic, attention to dirty things
        if (!holder.containsKey(copy.getId())) {  // new
            holder.put(copy);
            player.sendSystemMessage(Component.translatable("mocai_clues.finder.result.new"));
        } else {
            if (copy.equals(holder.get(copy.getId()))) {  // repeat
                player.sendSystemMessage(Component.translatable("mocai_clues.finder.result.repeated"));
            } else {  // merge
                holder.put(copy);
                player.sendSystemMessage(Component.translatable("mocai_clues.finder.result.update"));
            }
        }

        // setUnsaved
        location.markDirty(copy);
    }

    // ======== Create with: DetailsWithCompleteness + clue book UUID
    private static ClueObject createFromManual(ClueObject old) {
        // DetailsWithCompleteness
        var compo = new DetailWithCompleteness();
        DetailData dCompo = old.getComponentOrThrow(ComponentType.DETAIL_DATA);
        for (String s : dCompo.getDetails()) {
            compo.add(s, 1f);
        }

        // clue book UUID
        InteractResult rCompo = old.getComponentOrThrow(ComponentType.INTERACT_RESULT);
        UUID cluebookUUID = rCompo.getClueBookUUID();

        return Assembler.createClueBookClue(old, cluebookUUID, compo);
    }
}
