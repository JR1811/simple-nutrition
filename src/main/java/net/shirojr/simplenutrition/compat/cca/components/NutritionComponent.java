package net.shirojr.simplenutrition.compat.cca.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.shirojr.simplenutrition.SimpleNutrition;
import net.shirojr.simplenutrition.compat.cca.SimpleNutritionComponents;
import net.shirojr.simplenutrition.nutrition.NutritionHandler;
import net.shirojr.simplenutrition.util.LinkedHashMapUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface NutritionComponent extends Component, ServerTickingComponent {
    Identifier KEY = SimpleNutrition.getId("nutrition");

    static NutritionComponent get(LivingEntity entity) {
        return SimpleNutritionComponents.NUTRITION.get(entity);
    }

    PlayerEntity getProvider();

    LinkedHashMap<ItemStack, Long> getNutritionBuffer();

    void modifyNutritionBuffer(Consumer<LinkedHashMap<ItemStack, Long>> consumer, boolean shouldSync);

    int getNutritionBufferSize();

    void setNutritionBufferSize(int size);

    long getDigestionDuration();

    void setDigestionDuration(long duration, boolean shouldSync);

    @Nullable
    default Long getTimeOfConsumption(ItemStack stack) {
        return getNutritionBuffer().get(stack);
    }

    default void addConsumedStack(ServerWorld world, ItemStack stack) {
        ItemStack newStack = stack.copy();
        modifyNutritionBuffer(buffer -> {
            buffer.put(newStack, world.getTime());
            if (buffer.size() > getNutritionBufferSize()) {
                LinkedHashMapUtil.removeFirstEntry(buffer);
            }
        }, true);
        NutritionHandler.applyNutritionEffects(getProvider(), newStack);
    }

    default void clear() {
        modifyNutritionBuffer(LinkedHashMap::clear, true);
    }
}
