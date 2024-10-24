package net.shirojr.simplenutrition.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.shirojr.simplenutrition.data.TrackedDataUtil;
import net.shirojr.simplenutrition.gamerules.NutritionGamerules;
import net.shirojr.simplenutrition.nutrition.Nutrition;
import net.shirojr.simplenutrition.nutrition.NutritionHandler;
import net.shirojr.simplenutrition.util.LinkedHashMapUtil;
import net.shirojr.simplenutrition.util.NbtKeys;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Nutrition {
    @Shadow
    public abstract void remove(RemovalReason reason);

    @Unique
    private static final TrackedData<LinkedHashMap<ItemStack, Long>> NUTRITION_BUFFER =
            DataTracker.registerData(PlayerEntityMixin.class, TrackedDataUtil.ITEM_QUEUE);

    @Unique
    private static final TrackedData<Long> DIGESTION_DURATION =
            DataTracker.registerData(PlayerEntityMixin.class, TrackedDataUtil.LONG);


    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void nutritionFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(NbtKeys.NUTRITION_BUFFER)) {
            LinkedHashMap<ItemStack, Long> nutritionItems = new LinkedHashMap<>();
            NbtCompound listNbt = nbt.getCompound(NbtKeys.NUTRITION_BUFFER);
            for (int i = 0; i < listNbt.getKeys().size(); i++) {
                NbtCompound entryNbt = listNbt.getCompound(String.valueOf(i));
                ItemStack stack = ItemStack.fromNbt(entryNbt.getCompound(NbtKeys.ITEM_STACK));
                long time = entryNbt.getLong(NbtKeys.TIME);
                nutritionItems.put(stack, time);
            }
            this.dataTracker.set(NUTRITION_BUFFER, nutritionItems);
        }
        if (nbt.contains(NbtKeys.DIGESTION_DURATION)) {
            this.dataTracker.set(DIGESTION_DURATION, nbt.getLong(NbtKeys.DIGESTION_DURATION));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void nutritionToNbt(NbtCompound nbt, CallbackInfo ci) {
        LinkedHashMap<ItemStack, Long> nutritionList = simple_nutrition$getNutritionStacks();
        NbtCompound listNbt = new NbtCompound();
        int i = 0;
        for (var entry : nutritionList.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            NbtCompound itemStackNbt = new NbtCompound();
            entryNbt.put(NbtKeys.ITEM_STACK, entry.getKey().writeNbt(itemStackNbt));
            entryNbt.putLong(NbtKeys.TIME, entry.getValue());
            listNbt.put(String.valueOf(i), entryNbt);
            i++;
        }
        nbt.put(NbtKeys.NUTRITION_BUFFER, listNbt);
        nbt.putLong(NbtKeys.DIGESTION_DURATION, this.dataTracker.get(DIGESTION_DURATION));
    }

    //region DataTracker interaction
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void addNutritionTrackedData(CallbackInfo ci) {
        this.dataTracker.startTracking(NUTRITION_BUFFER, new LinkedHashMap<>());
        this.dataTracker.startTracking(DIGESTION_DURATION, 6000L);
    }

    @Override
    public void simple_nutrition$addNutritionStack(World world, ItemStack stack) {
        LinkedHashMap<ItemStack, Long> nutritionStacks = this.dataTracker.get(NUTRITION_BUFFER);
        nutritionStacks.put(stack, world.getTime());
        if (nutritionStacks.size() > TrackedDataUtil.NUTRITION_BUFFER_SIZE) {
            LinkedHashMapUtil.removeFirstEntry(nutritionStacks);
        }
        this.dataTracker.set(NUTRITION_BUFFER, nutritionStacks);
        NutritionHandler.applyNutritionEffects((PlayerEntity) (Object) this, stack);
    }

    @Override
    public LinkedHashMap<ItemStack, Long> simple_nutrition$getNutritionStacks() {
        return this.dataTracker.get(NUTRITION_BUFFER);
    }

    @Override
    public @Nullable Map.Entry<ItemStack, Long> simple_nutrition$getNutritionStack(int index) {
        return LinkedHashMapUtil.get(this.dataTracker.get(NUTRITION_BUFFER), index);
    }

    @Override
    public long simple_nutrition$getDigestionDuration() {
        return this.dataTracker.get(DIGESTION_DURATION);
    }

    @Override
    public void simple_nutrition$setDigestionDuration(long duration) {
        this.dataTracker.set(DIGESTION_DURATION, duration);
    }

    @Override
    public void simple_nutrition$clear() {
        this.dataTracker.set(NUTRITION_BUFFER, new LinkedHashMap<>());
    }

    @Override
    public void simple_nutrition$removeOldest() {
        LinkedHashMap<ItemStack, Long> nutritionStacks = this.dataTracker.get(NUTRITION_BUFFER);
        LinkedHashMapUtil.removeFirstEntry(nutritionStacks);
        this.dataTracker.set(NUTRITION_BUFFER, nutritionStacks);
    }

    @Override
    public @Nullable Map.Entry<ItemStack, Long> simple_nutrition$getOldestEntry() {
        LinkedHashMap<ItemStack, Long> nutritionStacks = this.dataTracker.get(NUTRITION_BUFFER);
        return LinkedHashMapUtil.getFirst(nutritionStacks);
    }
    //endregion


    @Inject(method = "eatFood", at = @At("HEAD"))
    private void modifyNutritionState(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (world.isClient()) return;
        if (world.getGameRules().getBoolean(NutritionGamerules.APPLY_NUTRITION_FATIGUE)) {
            simple_nutrition$addNutritionStack(world, stack.copy());
            //TODO: train digestion time to perform better
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickNutrition(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;
        Nutrition nutrition = (Nutrition) player;
        var optional = Optional.ofNullable(nutrition.simple_nutrition$getOldestEntry());
        long savedTimeOfLastEntry = optional.map(Map.Entry::getValue).orElse(-1L);
        if (savedTimeOfLastEntry == -1L) return;

        if (player.getWorld().getTime() > savedTimeOfLastEntry + nutrition.simple_nutrition$getDigestionDuration()) {
            nutrition.simple_nutrition$removeOldest();
        }

        //TODO: use itmestack food duration on top

        // full nutrition = 20
        // 1 min = 1200 ticks -> 5 min = 6000
    }
}
