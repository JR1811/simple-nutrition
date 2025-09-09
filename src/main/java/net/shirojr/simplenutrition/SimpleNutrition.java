package net.shirojr.simplenutrition;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.shirojr.simplenutrition.commands.NutritionCommands;
import net.shirojr.simplenutrition.compat.config.NutritionData;
import net.shirojr.simplenutrition.gamerules.NutritionGamerules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNutrition implements ModInitializer {
    public static final String MOD_ID = "simple-nutrition";
    public static final String MOD_ID_CLOTH_CONFIG = "cloth-config";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        NutritionCommands.initialize();
        NutritionGamerules.initialize();
        NutritionData.initialize();

        LOGGER.info("FEED ME MORE!");
    }

    public static Identifier getId(String path) {
        return Identifier.of(MOD_ID, path);
    }
}