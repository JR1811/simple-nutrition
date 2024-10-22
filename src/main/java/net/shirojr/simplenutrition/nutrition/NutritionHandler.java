package net.shirojr.simplenutrition.nutrition;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.simplenutrition.data.TrackedDataUtil;

import java.util.List;

public class NutritionHandler {
    public static void applyNutritionEffects(PlayerEntity player, ItemStack stack) {
        Nutrition nutrition = (Nutrition) player;
        List<ItemStack> nutritionStacks = nutrition.simple_nutrition$getNutritionStacks();
        long nutritionScore = nutritionStacks.stream().filter(entry -> entry.getItem().equals(stack.getItem())).count();
        float normalizedNutrition = (float) nutritionScore / TrackedDataUtil.NUTRITION_BUFFER_SIZE;
        if (nutritionStacks.size() < TrackedDataUtil.NUTRITION_BUFFER_SIZE) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        applyEffects(normalizedNutrition, serverPlayer);
    }

    private static void applyEffects(float normalizedNutrition, ServerPlayerEntity player) {
        if (normalizedNutrition >= 1) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 1, true, false, true), player);
        } else if (normalizedNutrition > 0.8) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 2, true, false, true), player);
        } else if (normalizedNutrition > 0.4) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 1, true, false, true), player);
        }
    }
}
