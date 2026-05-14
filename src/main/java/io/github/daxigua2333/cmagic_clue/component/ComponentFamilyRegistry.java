package io.github.daxigua2333.cmagic_clue.component;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Register:
 * - Family -> Set of CompoType
 * - map of ClueType -> Family list and Family -> ClueType list
 * For now, because the component assembly is static according to ClueType,
 * we don't make multi index on Family (and then composite index on BlockPos+Family...),
 * instead we register type->family here and index by type.
 * USAGE:
 * - Register family -> [CompoType], ClueType -> [Family]
 * - Used in System#process, ensure by Object#hasFamily
 */
public final class ComponentFamilyRegistry {

    public enum SystemFamily {
        INTERACT_SYSTEM(EnumSet.of(ComponentType.INTERACT_ENTRY, ComponentType.INTERACT_STATE, ComponentType.INTERACT_RESULT)),
        INTERACT_PASSIVE_SYSTEM(EnumSet.of(ComponentType.INTERACT_STATE, ComponentType.INTERACT_PASSIVE_BEHAVIOR, ComponentType.BLOCK_POS_WITH_FACE)),
        CHUNK_RENDER_SYSTEM(EnumSet.of(ComponentType.RENDERER_HOLDER)),
        ;

        private final EnumSet<ComponentType> family;

        SystemFamily(EnumSet<ComponentType> family) {
            this.family = family;
        }

        public EnumSet<ComponentType> getFamily() {
            return family;
        }
    }

    public static EnumSet<SystemFamily> getFamily(ClueType type) {
        switch (type) {
            case MANUAL, ITEM -> {
                return EnumSet.of(
                        SystemFamily.INTERACT_SYSTEM,
                        SystemFamily.INTERACT_PASSIVE_SYSTEM,
                        SystemFamily.CHUNK_RENDER_SYSTEM
                );
            }
            case null, default -> {
                return EnumSet.noneOf(SystemFamily.class);
                // TODO: finish default behavior
//                EnumSet<SystemFamily> result = EnumSet.noneOf(SystemFamily.class);
//                EnumSet<ComponentType> full = Assembler.getFamilyByClueType(type);
//                for (SystemFamily family : SystemFamily.values()) {
//                    if (full.containsAll(family.getFamily())){
//                        result.add(family);
//                    }
//                }
//                return result;
            }
        }
    }

    private static EnumMap<SystemFamily, EnumSet<ClueType>> familyToClueType;

    public static EnumSet<ClueType> getClueType(SystemFamily family) {
        return familyToClueType.getOrDefault(family, EnumSet.noneOf(ClueType.class));
    }

    static {
        for (ClueType type : ClueType.values()) {
            for (SystemFamily family : getFamily(type)) {
                familyToClueType.computeIfAbsent(family, k -> EnumSet.noneOf(ClueType.class)).add(type);
            }
        }
    }
}
