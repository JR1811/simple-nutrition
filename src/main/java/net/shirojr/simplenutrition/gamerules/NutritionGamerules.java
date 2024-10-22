package net.shirojr.simplenutrition.gamerules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class NutritionGamerules {
    public static final GameRules.Key<GameRules.BooleanRule> APPLY_NUTRITION_FATIGUE =
            register("applyNutritionFatigue", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    public static final GameRules.Key<GameRules.IntRule> STORED_NUTRITION_BUFFER_SIZE =
            register("storedNutritionBufferSize", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(10, 1));


    @SuppressWarnings("SameParameterValue")
    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> test) {
        return GameRuleRegistry.register(name, category, test);
    }

    public static void initialize() {
        // static initialisation
    }
}
