package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.EvecualMC;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void evecual$handleShiftFive(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS && (key == GLFW.GLFW_KEY_5 || key == GLFW.GLFW_KEY_KP_5)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null && client.currentScreen == null) {
                boolean shiftHeld = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0
                        || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                        || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
                        || (client.options != null && client.options.sneakKey.isPressed());
                boolean holdingRailgun = client.player.getMainHandStack().isOf(EvecualMC.RAILGUN_ITEM)
                        || client.player.getOffHandStack().isOf(EvecualMC.RAILGUN_ITEM);
                if (shiftHeld && holdingRailgun) {
                    ClientPlayNetworking.send(EvecualMC.TOGGLE_RAILGUN_IGNITE_PACKET_ID, PacketByteBufs.empty());
                    ci.cancel();
                }
            }
        }
    }
}
