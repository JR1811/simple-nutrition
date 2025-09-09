package net.shirojr.simplenutrition.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.shirojr.simplenutrition.compat.cca.components.NutritionComponent;
import net.shirojr.simplenutrition.gamerules.NutritionGamerules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void modifyNutritionState(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (!world.getGameRules().getBoolean(NutritionGamerules.APPLY_NUTRITION_FATIGUE)) return;
        NutritionComponent nutritionComponent = NutritionComponent.get(this);
        nutritionComponent.addConsumedStack(serverWorld, stack);
    }
}
