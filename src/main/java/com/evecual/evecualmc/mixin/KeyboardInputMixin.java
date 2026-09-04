package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.client.EvecualMCClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void evecual$disableMovementDuringRc(boolean slowDown, float f, CallbackInfo ci) {
        if (EvecualMCClient.isRcLinkActive()) {
            this.movementForward = 0.0F;
            this.movementSideways = 0.0F;
            this.jumping = false;
            this.sneaking = false;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                client.player.setVelocity(0.0, Math.min(client.player.getVelocity().y, 0.0), 0.0);
                client.player.setSprinting(false);
            }
        }
    }
}
