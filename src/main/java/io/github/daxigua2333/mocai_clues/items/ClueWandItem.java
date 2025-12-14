package io.github.daxigua2333.mocai_clues.items;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainer;
import io.github.daxigua2333.mocai_clues.data_attachments.statics.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.guis.WandScreen;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class ClueWandItem extends Item {
    public ClueWandItem(Properties props) {
        super(props);
    }

//    // LEFT-CLICK: cycle modes when player swings (left click)
//    @Override
//    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
//        if (!(entity instanceof Player player)) return false;
//        Level level = player.level();
//        // Only modify state on server side (avoid double-cycling)
//        if (!level.isClientSide) {
////            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1f, 1f);
//
//            // update(...) will set the component (creates it if needed) and returns the old/updated value
//            stack.update(ModDataComponents.WAND_MODE.get(), WandMode.CREATE, current -> {
//                // If the component was missing current could be null - guard
//                WandMode cur = current == null ? WandMode.CREATE : current;
//                return cur.next();
//            });
//
//            WandMode newMode = stack.getOrDefault(ModDataComponents.WAND_MODE.get(), WandMode.CREATE);
//
//            // show simple chat feedback (server -> client message to the player)
//            player.displayClientMessage(Component.literal("Wand mode: " + newMode.toString()), true);
//        }
//        // return true to cancel further processing if you don't want the swing to hit; adjust as needed
//        return false;
//    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        WandMode mode = stack.getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE);
        switch (mode) {
            case CREATE:
                if (!level.isClientSide()) {
                    if (ClueContainerAttachmentHelper.containsKey(level, clickedPos)) {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(clickedPos.toShortString() + " already has data"));  // TODO: lang
                        }
                    } else {
                        ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal("Attached data at " + clickedPos.toShortString()));
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            case DELETE:
                if (!level.isClientSide()) {
                    ClueContainerAttachmentHelper.remove(level, clickedPos);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal("Removed data at " + clickedPos.toShortString()));  // TODO: lang
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
//            case QUERY:
//                if (!level.isClientSide()) {
//                    // TODO: the nomi gui
//                }
            case CREATE_INFINITY:
                if (!level.isClientSide()) {
                    if (ClueContainerAttachmentHelper.containsKey(level, clickedPos)) {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(clickedPos.toShortString() + " already has data"));  // TODO: lang
                        }
                    } else {
                        ClueContainer container = ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
                        container.setInfinite(true);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal("Attached data at " + clickedPos.toShortString()));
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            case EDITOR:
                if (level.isClientSide) {
//                    Minecraft.getInstance().setScreen(new ExampleListScreen());
//                    List<DataSelectionScreen.ListEntryData> list = new ArrayList<>();
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("地下室1"), Component.literal("subtitle"), Component.literal("details\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("诺亚的尸体"), Component.literal("subtitle2"), Component.literal("details2\n1\n1"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    list.add(new DataSelectionScreen.ListEntryData(Component.literal("title2"), Component.literal("subtitle2"), Component.literal("details2\n222"), ItemStack.EMPTY));
//                    DataSelectionScreen.open(list);
                    WandScreen.open(  // TODO
                            () -> List.of(ClueType.MANUAL, ClueType.FOOTPRINT, ClueType.FOOTPRINT1, ClueType.FOOTPRINT2, ClueType.FOOTPRINT3, ClueType.FOOTPRINT4, ClueType.FOOTPRINT6, ClueType.FOOTPRINT7),
                            (type) -> ClientAccessor.queryClueObjectByClueType(type)
                    );
                }
            case null, default:
                return InteractionResult.PASS;
        }
    }

}
