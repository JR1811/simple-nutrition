package net.shirojr.simplenutrition.compat.cca.impl;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.simplenutrition.compat.cca.SimpleNutritionComponents;
import net.shirojr.simplenutrition.compat.cca.components.NutritionComponent;
import net.shirojr.simplenutrition.compat.config.NutritionData;
import net.shirojr.simplenutrition.util.NbtKeys;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class NutritionComponentImpl implements NutritionComponent, AutoSyncedComponent {
    private final PlayerEntity provider;

    private final LinkedHashMap<ItemStack, Long> nutritionBuffer;
    private int nutritionBufferSize;
    private long digestionDuration;

    public NutritionComponentImpl(PlayerEntity provider) {
        this.provider = provider;
        this.nutritionBuffer = new LinkedHashMap<>();
        this.nutritionBufferSize = NutritionData.baseNutritionBufferSize();
        this.digestionDuration = NutritionData.baseDigestionTime();
    }

    @Override
    public PlayerEntity getProvider() {
        return provider;
    }

    @Override
    public LinkedHashMap<ItemStack, Long> getNutritionBuffer() {
        return new LinkedHashMap<>(this.nutritionBuffer);
    }

    @Override
    public void modifyNutritionBuffer(Consumer<LinkedHashMap<ItemStack, Long>> consumer, boolean shouldSync) {
        consumer.accept(this.nutritionBuffer);
        if (shouldSync) {
            this.sync();
        }
    }

    @Override
    public int getNutritionBufferSize() {
        return nutritionBufferSize;
    }

    @Override
    public void setNutritionBufferSize(int size) {
        this.nutritionBufferSize = size;
    }

    @Override
    public long getDigestionDuration() {
        return digestionDuration;
    }

    @Override
    public void setDigestionDuration(long duration, boolean shouldSync) {
        this.digestionDuration = duration;
        if (shouldSync) {
            this.sync();
        }
    }

    @Override
    public void serverTick() {
        if (!(provider instanceof ServerPlayerEntity serverPlayer)) return;
        long currentTime = serverPlayer.getWorld().getTime();
        getNutritionBuffer().entrySet().removeIf(entry -> entry.getValue() + getDigestionDuration() > currentTime);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains(NbtKeys.DIGESTION_DURATION)) {
            this.setDigestionDuration(nbt.getLong(NbtKeys.DIGESTION_DURATION), false);
        }
        if (nbt.contains(NbtKeys.NUTRITION_BUFFER)) {
            modifyNutritionBuffer(LinkedHashMap::clear, false);
            for (NbtElement nbtElement : nbt.getList(NbtKeys.NUTRITION_BUFFER, NbtElement.COMPOUND_TYPE)) {
                NbtCompound entryNbt = (NbtCompound) nbtElement;
                ItemStack stack = ItemStack.fromNbt(entryNbt.getCompound(NbtKeys.ITEM_STACK));
                long time = entryNbt.getLong(NbtKeys.TIME);
                modifyNutritionBuffer(buffer -> buffer.put(stack, time), false);
            }
        }

        if (provider instanceof ServerPlayerEntity) {
            this.sync();
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putLong(NbtKeys.DIGESTION_DURATION, this.getDigestionDuration());
        NbtList nbtList = new NbtList();
        for (var entry : this.getNutritionBuffer().entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.put(NbtKeys.ITEM_STACK, entry.getKey().writeNbt(new NbtCompound()));
            nbtList.add(entryNbt);
        }
        nbt.put(NbtKeys.NUTRITION_BUFFER, nbtList);
    }

    @SuppressWarnings("unused")
    public static void onRespawn(NutritionComponentImpl from, NutritionComponentImpl to, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        // players wont keep data over deaths
    }

    public void sync() {
        SimpleNutritionComponents.NUTRITION.sync(this.provider);
    }
}
