package io.github.daxigua2333.mocai_clues.component.system.discovery;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractState;
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class FinderTick {

    private static boolean prevDoFound = false;

    // texture reflection
    @OnlyIn(Dist.CLIENT)
    public static void registerTextureChange() {
        ItemProperties.register(
                ModItemsRegistry.CLUE_FINDER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "found"),
                (ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int id) -> {
                    if (!(entity instanceof LocalPlayer player))
                        return 0f;  // on multi-players this entity will be both LocalPlayer and RemotePlayer, thus cause unintended updates
                    if (player != Minecraft.getInstance().player) return returnWithUpdate(false);

                    // finder in hand
                    boolean inHand = ItemStack.isSameItem(player.getMainHandItem(), stack) ||
                            ItemStack.isSameItem(player.getOffhandItem(), stack);
                    if (!inHand) return returnWithUpdate(false);

                    // finder hit predicate
                    HitResult hr = Minecraft.getInstance().hitResult;  // TODO: performance issues: merge the rayTrace ticker
                    if (hr == null) return returnWithUpdate(false);
                    List<ClueObject> data;
                    switch (hr.getType()) {
                        case BLOCK -> {
                            BlockHitResult bhr = (BlockHitResult) hr;
                            data = DataManager.Client.retrieveByBlockPos(Minecraft.getInstance().level, bhr.getBlockPos()).objects();
                        }
                        case ENTITY -> {
                            EntityHitResult ehr = (EntityHitResult) hr;
                            data = DataManager.Client.retrieveByEntity(ehr.getEntity()).objects();
                        }
                        default -> {
                            return returnWithUpdate(false);
                        }
                    }

                    boolean doFound = false;
                    for (ClueObject obj : data) {
                        InteractState compo = obj.getComponent(ComponentType.INTERACT_STATE);
                        if (compo == null) continue;
                        if (compo.isAccessible(player)) {
                            doFound = true;
                            break;
                        }
                    }

//                    if (inHand && !data.isEmpty()) {
                    if (doFound) {
//                        MoCaiClues.LOGGER.debug("{}", prevDoFound);
                        if (!prevDoFound) player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        return returnWithUpdate(true);
                    } else {
                        return returnWithUpdate(false);
                    }
                });
    }

    private static float returnWithUpdate(boolean result) {
        if (result) {
            prevDoFound = true;
            return 1f;
        } else {
            prevDoFound = false;
            return 0f;
        }
    }


}
