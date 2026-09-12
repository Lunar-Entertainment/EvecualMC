package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.PickupDroneEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class PickupDroneEntityRenderer extends EntityRenderer<PickupDroneEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/pickup_drone.png");

    private final PickupDroneEntityModel model;

    public PickupDroneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.35F;
        this.model = new PickupDroneEntityModel(context.getPart(PickupDroneEntityModel.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(PickupDroneEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(PickupDroneEntity drone, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 1. Heading Yaw Rotation
        float droneYaw = MathHelper.lerpAngleDegrees(tickDelta, drone.prevYaw, drone.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - droneYaw));

        // 2. Aerodynamic Flight Tilts (Pitch forward/back & Roll sideways)
        float pitch = MathHelper.lerp(tickDelta, drone.getPitchTilt(), drone.getPitchTilt());
        float roll = MathHelper.lerp(tickDelta, drone.getRollTilt(), drone.getRollTilt());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));

        // 3. Minecraft model coordinate orientation
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0, -1.5, 0.0);

        this.model.setAngles(drone, 0.0F, 0.0F, drone.age + tickDelta, 0.0F, 0.0F);

        // 4. Color tint based on drone color variant
        float r = 1.00F, g = 1.00F, b = 1.00F; // Default: Tactical Defense Gunmetal
        switch (drone.getColorVariant()) {
            case 0 -> { r = 1.00F; g = 0.25F; b = 0.25F; } // Crimson Red
            case 1 -> { r = 0.35F; g = 0.80F; b = 1.00F; } // Cyber Blue
            case 2 -> { r = 0.45F; g = 0.45F; b = 0.48F; } // Stealth Black
            case 3 -> { r = 0.35F; g = 1.00F; b = 0.40F; } // Neon Lime
            case 4 -> { r = 1.20F; g = 1.20F; b = 1.20F; } // Pearl White
            case 5 -> { r = 1.00F; g = 0.90F; b = 0.25F; } // Hazard Yellow
            default -> { r = 1.00F; g = 1.00F; b = 1.00F; } // Tactical Defense Gunmetal
        }

        // Render main body and propellers
        VertexConsumer solidVertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.renderBodyAndProps(matrices, solidVertices, light, OverlayTexture.DEFAULT_UV, r, g, b, 1.0F);

        // 5. High-speed Propeller Motion Blur
        boolean isMotorSpinning = drone.getEnergy() > 0 && (drone.isFlying() || !drone.isOnGround() || drone.getPropSpeed() > 0.35F);
        if (isMotorSpinning) {
            VertexConsumer transVertices = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));

            // Rotational ghost blades trailing behind the spinning propellers
            this.model.renderPropellerGhosts(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, 0.3F, 0.3F, 0.35F, 1.0F);

            // Translucent motion-blur rotor disc representing high RPM rotor sweep
            this.model.renderBlurDiscs(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, 0.35F);
        }

        // High-power aerial spotlight when Light is ON
        if (drone.isLightOn()) {
            RcDroneEntityRenderer.renderLightCone(matrices, vertexConsumers, 0.0F, 20.5F / 16.0F, -6.6F / 16.0F, 8.0F, 1.2F, 0.9F, 0.95F, 1.0F, 0.35F);
        }

        matrices.pop();
        super.render(drone, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
