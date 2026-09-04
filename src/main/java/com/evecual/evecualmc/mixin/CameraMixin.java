package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.client.EvecualMCClient;
import com.evecual.evecualmc.entity.RcCarEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private Entity focusedEntity;

    @ModifyArgs(
        method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0)
    )
    private void evecual$rotateRcCamera(Args args) {
        if (this.focusedEntity instanceof RcCarEntity ||
            this.focusedEntity instanceof com.evecual.evecualmc.entity.RcDroneEntity ||
            this.focusedEntity instanceof com.evecual.evecualmc.entity.RcRobotEntity) {
            float yaw = args.get(0);
            float pitch = args.get(1);
            args.set(0, yaw + EvecualMCClient.getRcCameraYaw());
            args.set(1, MathHelper.clamp(pitch + EvecualMCClient.getRcCameraPitch(), -85.0F, 85.0F));
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(
        method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D")
    )
    private double evecual$modifyRcCameraDistance(double distance) {
        if (this.focusedEntity instanceof RcCarEntity ||
            this.focusedEntity instanceof com.evecual.evecualmc.entity.RcDroneEntity ||
            this.focusedEntity instanceof com.evecual.evecualmc.entity.RcRobotEntity) {
            return EvecualMCClient.getRcCameraDistance();
        }
        return distance;
    }
}
