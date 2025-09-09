package net.shirojr.simplenutrition.compat.config;

import net.fabricmc.loader.api.FabricLoader;
import net.shirojr.simplenutrition.SimpleNutrition;

public class NutritionData {
    public static final boolean IS_CLOTH_CONFIG_LOADED = FabricLoader.getInstance().isModLoaded(SimpleNutrition.MOD_ID_CLOTH_CONFIG);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean shouldLoadGui() {
        if (IS_CLOTH_CONFIG_LOADED) return ConfigInit.CONFIG.loadGui;
        else return false;
    }

    public static long baseDigestionTime() {
        if (IS_CLOTH_CONFIG_LOADED) return ConfigInit.CONFIG.baseDigestionTime;
        else return 6000L;
    }

    public static int baseNutritionBufferSize() {
        if (IS_CLOTH_CONFIG_LOADED) return ConfigInit.CONFIG.baseNutritionBufferSize;
        else return 10;
    }

    public static void initialize() {
        if (IS_CLOTH_CONFIG_LOADED) {
            ConfigInit.initialize();
        }
    }
}
