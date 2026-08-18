package io.github.daxigua2333.cmagic_clue.integration;

import com.pltaube.cmagic.client.opera.OperaClient;
import com.pltaube.cmagic.data.opera.PerformState;
import com.pltaube.cmagic.opera.majo.Majo;
import com.pltaube.cmagic.opera.majo.Majos;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

public class CmagicCompat {

//    public static List<String> getOnlinePlayer(Player self) {
//        var decoNames = getAllMajoDecoName();
//        // exclude self
//        for (Character ch : Characters.getCharacters()) {
//            if (ch instanceof Majo) {
//
//            }
//        }
//    }
    @OnlyIn(Dist.CLIENT)
    public static boolean isPerforming() {
        return OperaClient.performState.equals(PerformState.PERFORM);
    }

    public static Set<MutableComponent> getAllMajoDecoName() {
        Set<MutableComponent> result = new HashSet<>(Majos.getMajos().size());
        for (Majo majo : Majos.getMajos()) {
            result.add(majo.getDecoName());
        }
        return result;
    }

}
