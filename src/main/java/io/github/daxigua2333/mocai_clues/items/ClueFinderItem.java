package io.github.daxigua2333.mocai_clues.items;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventHolder;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.PlaySound;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.FinderHit;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import io.github.daxigua2333.mocai_clues.guis.ClueInventoryInfiniteMenu;
import io.github.daxigua2333.mocai_clues.guis.ClueInventoryMenu;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainer;
import io.github.daxigua2333.mocai_clues.data_attachments.statics.ClueContainerAttachmentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;

public class ClueFinderItem extends Item {
    public ClueFinderItem(Properties props) {
        super(props);
    }

    private static boolean prevDoFound = false;
    // texture reflection
    public static void registerTextureChange(){
        ItemProperties.register(
                ModItemsRegistry.CLUE_FINDER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "found"),
                (ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int id) -> {
                    if (!(entity instanceof Player player)) return 0.0f;
                    if (player != Minecraft.getInstance().player) return 0f;

                    // finder in hand
                    boolean inHand = ItemStack.isSameItem(player.getMainHandItem(), stack) ||
                            ItemStack.isSameItem(player.getOffhandItem(), stack);

                    // finder hit predicate
                    HitResult hr = Minecraft.getInstance().hitResult;  // TODO: performance issues
                    if (hr == null) return 0f;
                    List<ClueObject> data;
                    switch (hr.getType()) {
                        case BLOCK -> {
                            BlockHitResult bhr = (BlockHitResult) hr;
                            data = ClientAccessor.retrieveByBlockPos(bhr.getBlockPos());
                        }
                        case ENTITY -> {
                            EntityHitResult ehr = (EntityHitResult) hr;
                            data = ClientAccessor.retrieveByEntity(ehr.getEntity());
                        }
                        default -> {
                            return 0f;
                        }
                    }

        //            boolean doFound = false;
        //            for (ClueObject obj : data) {
        //                InteractEventHolder compo = obj.getComponent(ComponentType.INTERACT_EVENT_HOLDER);
        //                if (compo == null) continue;
        //                for (InteractEvent event : compo.getImmutable()) {
        //                    InteractEvent.Context context = new InteractEvent.Context(InteractEventRegistry.EntryType.RAY_TRACE, player, obj);
        //                    if (event.getPredicate() instanceof FinderHit doHit && event.getHandler() instanceof PlaySound play && doHit.test(context)) {
        //                        play.handle(context);
        //                        doFound = true;
        //                        return (inHand && doFound) ? 1f : 0f;
        //                    }
        //                }
        //            }
        //
        //            return (inHand && doFound) ? 1f : 0f;
                    if (inHand && !data.isEmpty()) {
                        if (!prevDoFound) player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        prevDoFound = true;
                        return 1f;
                    } else {
                        prevDoFound = false;
                        return 0f;
                    }
                });
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
//        RectOverlayRenderer.ACTIVE_RECTS = Arrays.asList(new RectOverlay(0f, -60f, 0f, 30, 2, 1, 0.8f));
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) {
            // client simply return success (server will actually open)
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // server side
        // Make sure container exists
        if (!ClueContainerAttachmentHelper.containsKey(level, clickedPos)) {
            player.displayClientMessage(Component.literal("No inventory attachment at this block."), true);  // TODO: lang
            return InteractionResult.SUCCESS;
        }

        // Obtain or create the attachment instance
        ClueContainer container = ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
        LevelChunk chunk = level.getChunkAt(clickedPos);
        // Create a MenuProvider which will be used server-side to create the container
        MenuProvider provider;
        if (container.isInfinite()) {
            provider = new SimpleMenuProvider(
                (id, playerInv, p) -> new ClueInventoryInfiniteMenu(id, playerInv, container.getInv(chunk), clickedPos, player.isCreative()),
                Component.literal(container.getName().isEmpty() ? "Clue Inventory (Infinite)" : container.getName())  // TODO: lang
            );
        } else {
            provider = new SimpleMenuProvider(
                (id, playerInv, p) -> new ClueInventoryMenu(id, playerInv, container.getInv(chunk), clickedPos, player.isCreative()),
                Component.literal(container.getName().isEmpty() ? "Clue Inventory" : container.getName())
            );
        }

        // Open screen for server player and write initial sync data to the buffer for the client constructor:
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.openMenu(provider);

        return InteractionResult.SUCCESS;
    }
}
