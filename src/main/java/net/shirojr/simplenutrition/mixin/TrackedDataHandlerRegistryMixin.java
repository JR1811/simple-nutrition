package net.shirojr.simplenutrition.mixin;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.shirojr.simplenutrition.data.TrackedDataUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrackedDataHandlerRegistry.class)
public abstract class TrackedDataHandlerRegistryMixin {
    @Shadow
    public static void register(TrackedDataHandler<?> handler) {
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerCustomTrackedDataHandler(CallbackInfo ci) {
        register(TrackedDataUtil.ITEM_QUEUE);
    }
}
