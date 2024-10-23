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
import net.shirojr.simplenutrition.util.NbtKeys;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Nutrition {
    @Shadow
    public abstract void remove(RemovalReason reason);

    @Unique
    private static final TrackedData<List<ItemStack>> NUTRITION_BUFFER =
            DataTracker.registerData(PlayerEntityMixin.class, TrackedDataUtil.ITEM_QUEUE);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void addNutritionTrackedData(CallbackInfo ci) {
        this.dataTracker.startTracking(NUTRITION_BUFFER, new ArrayList<>());
    }

    @Override
    public void simple_nutrition$addNutritionStack(ItemStack stack) {
        List<ItemStack> nutritionStacks = this.dataTracker.get(NUTRITION_BUFFER);
        nutritionStacks.add(stack);
        if (nutritionStacks.size() > TrackedDataUtil.NUTRITION_BUFFER_SIZE) {
            nutritionStacks.remove(nutritionStacks.size() - 1);
        }
        this.dataTracker.set(NUTRITION_BUFFER, nutritionStacks);
        NutritionHandler.applyNutritionEffects((PlayerEntity) (Object) this, stack);
    }

    @Override
    public List<ItemStack> simple_nutrition$getNutritionStacks() {
        return this.dataTracker.get(NUTRITION_BUFFER);
    }

    @Override
    public @Nullable ItemStack simple_nutrition$getNutritionStack(int index) {
        List<ItemStack> nutritionStacks = this.dataTracker.get(NUTRITION_BUFFER);
        if (index > nutritionStacks.size() - 1) return null;
        return nutritionStacks.get(index);
    }

    @Override
    public void simple_nutrition$clear() {
        this.dataTracker.set(NUTRITION_BUFFER, new ArrayList<>());
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void modifyNutritionState(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (world.isClient()) return;
        if (world.getGameRules().getBoolean(NutritionGamerules.APPLY_NUTRITION_FATIGUE)) {
            simple_nutrition$addNutritionStack(stack.copy());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void nutritionFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(NbtKeys.NUTRITION_BUFFER)) {
            List<ItemStack> nutritionItems = new ArrayList<>();
            NbtCompound listNbt = nbt.getCompound(NbtKeys.NUTRITION_BUFFER);
            for (int i = 0; i < listNbt.getKeys().size(); i++) {
                nutritionItems.add(ItemStack.fromNbt(listNbt.getCompound(String.valueOf(i))));
            }
            this.dataTracker.set(NUTRITION_BUFFER, nutritionItems);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void nutritionToNbt(NbtCompound nbt, CallbackInfo ci) {
        List<ItemStack> nutritionList = simple_nutrition$getNutritionStacks();
        NbtCompound listNbt = new NbtCompound();
        for (int i = 0; i < nutritionList.size(); i++) {
            ItemStack stack = nutritionList.get(i);
            NbtCompound entryNbt = new NbtCompound();
            stack.writeNbt(entryNbt);
            listNbt.put(String.valueOf(i), entryNbt);
        }
        nbt.put(NbtKeys.NUTRITION_BUFFER, listNbt);
    }
}
