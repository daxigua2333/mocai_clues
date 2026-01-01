//package io.github.daxigua2333.mocai_clues.component.world.interact.handler;
//
//import com.mojang.serialization.MapCodec;
//import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
//import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.world.entity.player.Player;
//
//public class PlaySound extends BaseHandler{
//    @Override
//    public InteractEventRegistry.HandlerType type() {
//        return InteractEventRegistry.HandlerType.PLAY_SOUND;
//    }
//
//    @Override
//    public void handle(InteractEvent.Context context) {
//        Player player = context.player();
//        if (!player.level().isClientSide) return;
//        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
//    }
//
//    public static final MapCodec<PlaySound> CODEC = MapCodec.unit(new PlaySound());
//}
