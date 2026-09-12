package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.client.EvecualMCClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void evecual$cancelItemUseDuringRc(CallbackInfo ci) {
        if (EvecualMCClient.isRcCameraActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void evecual$cancelAttackDuringRc(CallbackInfoReturnable<Boolean> cir) {
        if (EvecualMCClient.isRcCameraActive() || EvecualMCClient.isRcLinkActive()) {
            cir.setReturnValue(false);
        }
    }
}
