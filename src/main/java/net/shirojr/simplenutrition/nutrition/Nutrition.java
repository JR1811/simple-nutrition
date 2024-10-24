package net.shirojr.simplenutrition.nutrition;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public interface Nutrition {
    void simple_nutrition$addNutritionStack(World world, ItemStack stack);

    @Nullable
    Map.Entry<ItemStack, Long> simple_nutrition$getNutritionStack(int index);

    LinkedHashMap<ItemStack, Long> simple_nutrition$getNutritionStacks();

    void simple_nutrition$clear();

    void simple_nutrition$removeLast();

    @Nullable
    Map.Entry<ItemStack, Long> simple_nutrition$getLastEntry();

    long simple_nutrition$getDigestionDuration();

    void simple_nutrition$setDigestionDuration(long duration);
}
