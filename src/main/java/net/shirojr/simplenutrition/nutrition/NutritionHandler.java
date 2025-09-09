package net.shirojr.simplenutrition.nutrition;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.shirojr.simplenutrition.compat.cca.components.NutritionComponent;

import java.util.Map;
import java.util.Optional;

public class NutritionHandler {
    public static void applyNutritionEffects(PlayerEntity player, ItemStack newDigestionStack) {
        NutritionComponent nutritionComponent = NutritionComponent.get(player);
        Map<ItemStack, Long> nutritionStacks = nutritionComponent.getNutritionBuffer();

        long nutritionScore = 0;
        for (ItemStack entry : nutritionStacks.keySet()) {
            if (!entry.getItem().equals(newDigestionStack.getItem())) continue;
            nutritionScore += 1;
        }
        int nutritionBufferSize = nutritionComponent.getNutritionBufferSize();
        if (nutritionStacks.size() < nutritionBufferSize) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        float normalizedNutrition = (float) nutritionScore / nutritionBufferSize;
        applyEffects(serverPlayer, normalizedNutrition);
        trainDigestion(serverPlayer, newDigestionStack);
    }

    private static void applyEffects(ServerPlayerEntity player, float normalizedNutrition) {
        // TODO: rethink / balance effect application
        if (normalizedNutrition >= 1) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 1, true, false, true), player);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 1, true, false, true), player);
        } else if (normalizedNutrition > 0.8) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 2, true, false, true), player);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 1, true, false, true), player);
        } else if (normalizedNutrition > 0.4) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 1, true, false, true), player);
        }
    }

    private static void trainDigestion(ServerPlayerEntity player, ItemStack stack) {
        int minTrainableTime = 300;
        int minTimeReduction = 20, maxTimeReduction = 100;

        float normalizedItemHunger = Optional.ofNullable(stack.getItem().getFoodComponent()).map(FoodComponent::getHunger).orElse(0) / 20f;
        if (player.getRandom().nextFloat() >= normalizedItemHunger) return;

        NutritionComponent nutritionComponent = NutritionComponent.get(player);

        long currentDigestionTime = nutritionComponent.getDigestionDuration();
        if (currentDigestionTime <= minTrainableTime) return;
        long lerpedHungerValue = currentDigestionTime - MathHelper.lerp(normalizedItemHunger, minTimeReduction, maxTimeReduction);
        nutritionComponent.setDigestionDuration(lerpedHungerValue, true);
    }
}
