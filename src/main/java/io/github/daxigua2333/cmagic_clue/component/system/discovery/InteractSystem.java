package io.github.daxigua2333.cmagic_clue.component.system.discovery;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.Assembler;
import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.component.ComponentFamilyRegistry;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.component.data.DetailData;
import io.github.daxigua2333.cmagic_clue.component.data.ItemClue;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractEntry;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractResult;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractState;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.cluebook.DetailWithCompleteness;
import io.github.daxigua2333.cmagic_clue.data.ObjectsWithLocation;
import io.github.daxigua2333.cmagic_clue.data.common.DataManager;
import io.github.daxigua2333.cmagic_clue.data.location.FromClueBook;
import io.github.daxigua2333.cmagic_clue.data.location.IRuntimeLocation;
import io.github.daxigua2333.cmagic_clue.items.ModItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CMagicClue.MODID)
public final class InteractSystem {
    private static final ComponentFamilyRegistry.SystemFamily FAMILY = ComponentFamilyRegistry.SystemFamily.INTERACT_SYSTEM;


    private static final Map<UUID, Integer> LAST_RIGHT_CLICK = new HashMap<>();
    private static final int DELTA = 5;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private static void onRegularClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        if (!event.getLevel().isClientSide()
                && LAST_RIGHT_CLICK.computeIfAbsent(player.getUUID(), k -> 0) + DELTA < event.getLevel().getServer().getTickCount()) {
            LAST_RIGHT_CLICK.put(player.getUUID(), event.getLevel().getServer().getTickCount());
            processInteractEvent(
                    DataManager.Server.retrieveByBlockPos(event.getLevel(), event.getPos()),
                    InteractEntry.EntryType.REGULAR_RIGHT_CLICK,
                    (ServerPlayer) player
            );
        }

        event.setCanceled(false);
        event.setCancellationResult(InteractionResult.PASS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private static void onRegularClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();

        if (!event.getLevel().isClientSide()
                && LAST_RIGHT_CLICK.computeIfAbsent(player.getUUID(), k -> 0) + DELTA < event.getLevel().getServer().getTickCount()) {
            LAST_RIGHT_CLICK.put(player.getUUID(), event.getLevel().getServer().getTickCount());
            processInteractEvent(
                    DataManager.Server.retrieveByEntity(event.getTarget()),
                    InteractEntry.EntryType.REGULAR_RIGHT_CLICK,
                    (ServerPlayer) event.getEntity()
            );
        }

        event.setCanceled(false);
        event.setCancellationResult(InteractionResult.PASS);
    }

    @SubscribeEvent
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
        var level = event.getLevel();
        var pos = event.getPos();
        Player player = event.getEntity();
        boolean canInteract = false;
        InteractEntry.EntryType entryType = InteractEntry.EntryType.CLICK_WITH_FINDER;

        if (!level.isClientSide()) {
            ObjectsWithLocation data = DataManager.Server.retrieveByBlockPos(level, pos);
            if (canInteract(data, entryType, player)) {
                canInteract = true;
                processInteractEvent(data, entryType, (ServerPlayer) player);
            }
        } else {
            canInteract = canInteract(DataManager.Client.retrieveByBlockPos(level, pos), entryType, player);
        }

        // ONLY cancel and bypass normal interaction if data exists
        if (canInteract) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        } else {
            event.setCanceled(false);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent
    private static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
        var level = event.getLevel();
        Player player = event.getEntity();
        boolean canInteract = false;
        InteractEntry.EntryType entryType = InteractEntry.EntryType.CLICK_WITH_FINDER;

        if (!level.isClientSide()) {
            ObjectsWithLocation data = DataManager.Server.retrieveByEntity(event.getTarget());
            if (canInteract(data, entryType, player)) {
                canInteract = true;
                processInteractEvent(data, entryType, (ServerPlayer) player);
            }
        } else {
            canInteract = canInteract(DataManager.Client.retrieveByEntity(event.getTarget()), entryType, player);
        }

        // ONLY cancel and bypass normal interaction if data exists
        if (canInteract) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        } else {
            event.setCanceled(false);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
            processInteractEvent(DataManager.Server.retrieveByBlockPos(level, event.getPos()), InteractEntry.EntryType.REGULAR_LEFT_CLICK, (ServerPlayer) event.getEntity());
        }

        // DO NOT cancel the event, and do not set useBlock/useItem to DENY.
        // This ensures the player starts mining the block normally.
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


    private static void processInteractEvent(ObjectsWithLocation ol, InteractEntry.EntryType
            entryType, ServerPlayer player) {
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

            // perform the interactions
            InteractResult rCompo = obj.getComponentOrThrow(ComponentType.INTERACT_RESULT);
            for (InteractResult.ResultType type : rCompo.getEnabled()) {
                switch (type) {
                    case SEND_ITEM -> sendItem(player, obj, ol.location());
//                    case SEND_MANUAL_CLUE -> sendManualClue(player, obj);
                    case SEND_TO_CLUE_BOOK -> sendToClueBook(player, obj);
                    case SEND_TO_CHAT_BOX -> sendToChatBox(player, obj);
                }
            }

            bCompo.onFound();
            ol.location().markDirty(obj);
        }
    }

    private static boolean canInteract(ObjectsWithLocation ol, InteractEntry.EntryType entryType, Player player) {
        boolean success = false;
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

            // perform the interactions
            success = true;
        }
        return success;
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
            player.sendSystemMessage(Component.translatable(CMagicClue.MODID + ".finder.result.new"));
        } else {
            if (copy.equals(holder.get(copy.getId()))) {  // repeat
                player.sendSystemMessage(Component.translatable(CMagicClue.MODID + ".finder.result.repeated"));
            } else {  // merge
                holder.put(copy);
                player.sendSystemMessage(Component.translatable(CMagicClue.MODID + ".finder.result.update"));
            }
        }

        // setUnsaved
        location.markDirty(copy);
    }

    public static void sendToChatBox(ServerPlayer player, ClueObject obj) {
        DetailData dCompo = obj.getComponentOrThrow(ComponentType.DETAIL_DATA);
        for (String text : dCompo.getDetails()) {
            player.sendSystemMessage(Component.literal(text));
        }
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
