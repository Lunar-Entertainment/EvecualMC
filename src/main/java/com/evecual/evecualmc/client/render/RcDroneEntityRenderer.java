package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.RcDroneEntity;
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

public class RcDroneEntityRenderer extends EntityRenderer<RcDroneEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/rc_drone.png");

    private final RcDroneEntityModel model;

    public RcDroneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.30F;
        this.model = new RcDroneEntityModel(context.getPart(RcDroneEntityModel.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(RcDroneEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RcDroneEntity drone, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
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
        float r = 0.30F, g = 0.75F, b = 1.00F; // Default Cyber Blue
        switch (drone.getColorVariant()) {
            case 0 -> { r = 1.00F; g = 0.20F; b = 0.20F; } // Crimson Red
            case 2 -> { r = 0.35F; g = 0.35F; b = 0.38F; } // Stealth Black
            case 3 -> { r = 0.30F; g = 1.00F; b = 0.35F; } // Neon Lime
            case 4 -> { r = 1.15F; g = 1.15F; b = 1.15F; } // Pearl White
            case 5 -> { r = 1.00F; g = 0.90F; b = 0.20F; } // Racing Yellow
            default -> { r = 0.30F; g = 0.75F; b = 1.00F; } // Cyber Blue
        }

        // Render main body and propellers
        VertexConsumer solidVertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.renderBodyAndProps(matrices, solidVertices, light, OverlayTexture.DEFAULT_UV, r, g, b, 1.0F);

        // 5. High-speed Propeller Motion Blur
        float propSpeed = drone.getPropSpeed();
        if (propSpeed > 0.35F) {
            VertexConsumer transVertices = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));

            // Rotational ghost blades trailing behind the spinning propellers
            this.model.renderPropellerGhosts(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, 0.3F, 0.3F, 0.35F, propSpeed);

            // Translucent motion-blur rotor disc representing high RPM rotor sweep
            float discAlpha = MathHelper.clamp((propSpeed - 0.35F) * 0.4F, 0.0F, 0.55F);
            this.model.renderBlurDiscs(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, discAlpha);
        }

        matrices.pop();
        super.render(drone, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
