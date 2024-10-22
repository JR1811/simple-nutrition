package net.shirojr.simplenutrition;

import net.fabricmc.api.ModInitializer;

import net.shirojr.simplenutrition.commands.NutritionCommands;
import net.shirojr.simplenutrition.gamerules.NutritionGamerules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNutrition implements ModInitializer {
	public static final String MOD_ID = "simple-nutrition";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NutritionCommands.initialize();
		NutritionGamerules.initialize();

		LOGGER.info("FEED ME MORE!");
	}
}