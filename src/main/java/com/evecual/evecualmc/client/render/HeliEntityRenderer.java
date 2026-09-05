package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.HeliEntity;
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

public class HeliEntityRenderer extends EntityRenderer<HeliEntity> {
    private static final Identifier[] TEXTURES = new Identifier[]{
            new Identifier("evecualmc", "textures/entity/heli_red.png"),    // 0: Sport Crimson Red
            new Identifier("evecualmc", "textures/entity/heli_blue.png"),   // 1: Cyber Electric Blue
            new Identifier("evecualmc", "textures/entity/heli_black.png"),  // 2: Stealth Midnight Black
            new Identifier("evecualmc", "textures/entity/heli_lime.png"),   // 3: Neon Lime
            new Identifier("evecualmc", "textures/entity/heli_white.png"),  // 4: Pearl White
            new Identifier("evecualmc", "textures/entity/heli_yellow.png")  // 5: Racing Yellow
    };

    private final HeliEntityModel model;

    public HeliEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 1.2F;
        this.model = new HeliEntityModel(context.getPart(HeliEntityModel.MODEL_LAYER));
    }

    @Override
    public void render(HeliEntity heli, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 1. Smooth Yaw Heading
        float heliYaw = MathHelper.lerpAngleDegrees(tickDelta, heli.prevYaw, heli.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - heliYaw));

        // 2. Aerodynamic Pitch Tilt & Roll Banking
        float pitch = heli.getPitchTilt();
        float roll = heli.getRollTilt();
        if (Math.abs(pitch) > 0.01F) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        }
        if (Math.abs(roll) > 0.01F) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-roll));
        }

        // Invert Y axis for Minecraft entity model orientation
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0, -1.5, 0.0);

        // Update rotor rotation angles
        this.model.setAngles(heli, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // 3. Render Solid Fuselage, Skids, Tail Boom & Rotors
        Identifier texture = this.getTexture(heli);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(texture));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        // 4. Render Transparent Colored Glass Bubble Cockpit Canopy!
        float[] glassRgba = CarEntityRenderer.getGlassColorRgba(heli.getGlassColor());
        VertexConsumer glassConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
        this.model.renderGlass(matrices, glassConsumer, light, OverlayTexture.DEFAULT_UV, glassRgba[0], glassRgba[1], glassRgba[2], glassRgba[3]);

        // 5. Main Rotor Motion Blur when spooling up / flying
        float rotorSpeed = heli.getRotorSpeed();
        if (rotorSpeed > 0.3F) {
            VertexConsumer blurConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
            this.model.renderRotorMotionBlur(matrices, blurConsumer, light, OverlayTexture.DEFAULT_UV, rotorSpeed);
        }

        matrices.pop();
        super.render(heli, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(HeliEntity heli) {
        int variant = heli.getColorVariant();
        if (variant >= 0 && variant < TEXTURES.length) {
            return TEXTURES[variant];
        }
        return TEXTURES[1];
    }
}
