package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.client.EvecualMCClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void evecual$applyRcZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (EvecualMCClient.isRcCameraActive()) {
            float zoom = EvecualMCClient.getRcFpZoom();
            if (zoom > 1.0F) {
                cir.setReturnValue(cir.getReturnValueD() / zoom);
            }
        }
    }

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void evecual$applyRcHeadlightVision(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (EvecualMCClient.isRcCameraActive() && EvecualMCClient.isRcVehicleLightOn()) {
            cir.setReturnValue(1.0F);
        }
    }
}
