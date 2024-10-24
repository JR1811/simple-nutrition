package net.shirojr.simplenutrition.nutrition;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.shirojr.simplenutrition.gamerules.NutritionGamerules;

import java.util.LinkedHashMap;
import java.util.Optional;

public class NutritionHandler {
    public static void applyNutritionEffects(PlayerEntity player, ItemStack stack) {
        Nutrition nutrition = (Nutrition) player;
        LinkedHashMap<ItemStack, Long> nutritionStacks = nutrition.simple_nutrition$getNutritionStacks();
        long nutritionScore = nutritionStacks.entrySet().stream().filter(entry -> entry.getKey().getItem().equals(stack.getItem())).count();
        int nutritionBufferSize = player.getWorld().getGameRules().getInt(NutritionGamerules.STORED_NUTRITION_BUFFER_SIZE);
        float normalizedNutrition = (float) nutritionScore / nutritionBufferSize;
        if (nutritionStacks.size() < nutritionBufferSize) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        applyEffects(serverPlayer, normalizedNutrition);
        trainDigestion(serverPlayer, stack);
    }

    private static void applyEffects(ServerPlayerEntity player, float normalizedNutrition) {
        // TODO: rethink / balance effect application
        if (normalizedNutrition >= 1) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 1, true, false, true), player);
        } else if (normalizedNutrition > 0.8) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 2, true, false, true), player);
        } else if (normalizedNutrition > 0.4) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 1, true, false, true), player);
        }
    }

    private static void trainDigestion(ServerPlayerEntity player, ItemStack stack) {
        int minTrainableTime = 300;
        int minTimeReduction = 20, maxTimeReduction = 100;

        float normalizedItemHunger = Optional.ofNullable(stack.getItem().getFoodComponent()).map(FoodComponent::getHunger).orElse(0) / 20f;
        if (player.getRandom().nextFloat() < normalizedItemHunger) {
            Nutrition nutrition = (Nutrition) player;
            long currentDigestionTime = nutrition.simple_nutrition$getDigestionDuration();
            if (currentDigestionTime <= minTrainableTime) return;
            long lerpedHungerValue = (long) (currentDigestionTime - MathHelper.lerp(normalizedItemHunger, minTimeReduction, maxTimeReduction));
            nutrition.simple_nutrition$setDigestionDuration(lerpedHungerValue);
        }
    }
}
