package net.shirojr.simplenutrition.nutrition;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface Nutrition {
    void simple_nutrition$addNutritionStack(ItemStack stack);

    @Nullable
    ItemStack simple_nutrition$getNutritionStack(int index);

    List<ItemStack> simple_nutrition$getNutritionStacks();

    void simple_nutrition$clear();
}
