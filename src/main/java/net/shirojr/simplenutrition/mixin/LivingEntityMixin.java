package net.shirojr.simplenutrition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UseAction;
import net.shirojr.simplenutrition.nutrition.Nutrition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;spawnConsumptionEffects(Lnet/minecraft/item/ItemStack;I)V"))
    private void modifyNutritionForConsumption(LivingEntity instance, ItemStack activeStack, int particleCount, Operation<Void> original) {
        if (activeStack.getUseAction().equals(UseAction.DRINK) && instance instanceof ServerPlayerEntity player) {
            ((Nutrition) player).simple_nutrition$removeOldest();
        }
        original.call(instance, activeStack, particleCount);
    }
}
