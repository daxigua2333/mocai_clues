//package io.github.daxigua2333.mocai_clues.footprints;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//public class FootprintManager {
//    private final List<Footprint> footprints = new ArrayList<>();
//
//    public void addFootprint(Footprint fp) {
//        footprints.add(fp);
//    }
//
//    public List<Footprint> getFootprints() {
//        return footprints;
//    }
//
//    public void tick() {
//        // e.g. fade out alpha & remove when done
//        Iterator<Footprint> it = footprints.iterator();
//        while (it.hasNext()) {
//            Footprint fp = it.next();
//            fp.alpha -= 0.01f;   // fade speed example
//            if (fp.alpha <= 0f) {
//                it.remove();
//            }
//        }
//    }
//}
