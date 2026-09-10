package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.client.EvecualMCClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
        )
    )
    private void evecual$redirectMouseLookDuringRc(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        if (EvecualMCClient.isRcCameraActive()) {
            EvecualMCClient.onRcMouseTurn(cursorDeltaX, cursorDeltaY);
        } else {
            player.changeLookDirection(cursorDeltaX, cursorDeltaY);
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void evecual$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (EvecualMCClient.isRcCameraActive() && net.minecraft.client.MinecraftClient.getInstance().currentScreen == null) {
            EvecualMCClient.onRcCameraScroll(vertical);
            ci.cancel();
        }
    }
}
