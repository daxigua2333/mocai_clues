//package io.github.daxigua2333.mocai_clues.footprints;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//public class FootprintClientManager {
//    private final List<Footprint> footprints = new CopyOnWriteArrayList<>();
//    public void addFootprint(Footprint fp) {
//        footprints.add(fp);
//    }
//
//    public void removeExpired(long gameTime) {
//        footprints.removeIf(fp -> fp.expirationTime > 0 && gameTime > fp.expirationTime);
//    }
//
//    public List<Footprint> getFootprints() {
//        return footprints;
//    }
//
//    public static FootprintClientManager get() {
////        MoCaiClues.LOGGER.debug("Getting FootprintClientManager");
//        var ret = new FootprintClientManager();
//        ret.addFootprint(new Footprint(0, 0, 0, 0,  5, ResourceLocation.fromNamespaceAndPath("", ""), 1000L));
//        return ret;
//    }
//}
