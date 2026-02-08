//package io.github.daxigua2333.mocai_clues.data.location;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
//import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.world.level.Level;
//
//public class FromServerSavedData implements IRuntimeLocation {
//    private final MinecraftServer server;
//
//    public FromServerSavedData(MinecraftServer server) {
//        this.server = server;
//    }
//
//    public FromServerSavedData(Level level) {
//        this(level.getServer());
//    }
//
////    @Override
////    public Type type() {
////        return Type.SERVER_SD;
////    }
//
//    @Override
//    public ObjectHolder<ClueObject> getHolder() {
//        return ClueObjectHolderInSavedData.getInstance(server).holder();
//    }
//
//    @Override
//    public void markDirty() {
//        ClueObjectHolderInSavedData.getInstance(server).setDirty();
//    }
//}
