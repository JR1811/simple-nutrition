package net.shirojr.simplenutrition.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.shirojr.simplenutrition.nutrition.Nutrition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "eatFood", at = @At("RETURN"))
    private void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        // if drinking and is server player
        if (stack.getUseAction() == UseAction.DRINK && (Object)this instanceof ServerPlayerEntity player) {
            ((Nutrition) player).simple_nutrition$removeOldest();
        }
    }
}
