package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.RcCarEntity;
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

public class RcCarEntityRenderer extends EntityRenderer<RcCarEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/rc_car.png");

    private final RcCarEntityModel model;

    public RcCarEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.35F;
        this.model = new RcCarEntityModel(context.getPart(RcCarEntityModel.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(RcCarEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RcCarEntity car, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Smooth yaw rotation
        float carYaw = MathHelper.lerpAngleDegrees(tickDelta, car.prevYaw, car.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - carYaw));

        // Invert Y axis for Minecraft model coordinates
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0, -1.5, 0.0);

        this.model.setAngles(car, 0.0F, 0.0F, car.age + tickDelta, 0.0F, 0.0F);

        // Color tint based on color variant
        float r = 1.0F, g = 1.0F, b = 1.0F;
        switch (car.getColorVariant()) {
            case 1 -> { r = 0.30F; g = 0.75F; b = 1.00F; } // Cyber Blue
            case 2 -> { r = 0.38F; g = 0.38F; b = 0.42F; } // Stealth Black / Midnight
            case 3 -> { r = 0.35F; g = 1.00F; b = 0.40F; } // Neon Lime
            case 4 -> { r = 1.15F; g = 1.15F; b = 1.15F; } // Pearl White
            case 5 -> { r = 1.00F; g = 0.90F; b = 0.20F; } // Racing Yellow
            default -> { r = 1.00F; g = 1.00F; b = 1.00F; } // Crimson Red (native texture)
        }

        VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV, r, g, b, 1.0F);

        // Wheel rotational motion blur when driving at speed
        float speed = (float) car.getCurrentSpeed();
        if (Math.abs(speed) > 0.08F) {
            VertexConsumer transVertices = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
            this.model.renderWheelMotionBlur(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, speed);
        }

        // Dual Headlights when Light is ON
        if (car.isLightOn()) {
            renderLightCone(matrices, vertexConsumers, -3.0F / 16.0F, 17.2F / 16.0F, -8.1F / 16.0F, 6.0F, 0.8F, 1.0F, 0.95F, 0.7F, 0.30F);
            renderLightCone(matrices, vertexConsumers, 3.0F / 16.0F, 17.2F / 16.0F, -8.1F / 16.0F, 6.0F, 0.8F, 1.0F, 0.95F, 0.7F, 0.30F);
        }

        matrices.pop();
        super.render(car, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    public static void renderLightCone(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                       float startX, float startY, float startZ,
                                       float length, float endRadius,
                                       float r, float g, float b, float maxAlpha) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLightning());
        var matrix = matrices.peek().getPositionMatrix();

        float endZ = startZ - length;

        // Glowing core lens quad
        float core = 0.08F;
        consumer.vertex(matrix, startX - core, startY - core, startZ).color(r, g, b, 0.9F).next();
        consumer.vertex(matrix, startX + core, startY - core, startZ).color(r, g, b, 0.9F).next();
        consumer.vertex(matrix, startX + core, startY + core, startZ).color(r, g, b, 0.9F).next();
        consumer.vertex(matrix, startX - core, startY + core, startZ).color(r, g, b, 0.9F).next();

        // 4 cone frustum side quads
        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX + endRadius, startY + endRadius, endZ).color(r, g, b, 0.0F).next();
        consumer.vertex(matrix, startX - endRadius, startY + endRadius, endZ).color(r, g, b, 0.0F).next();

        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX - endRadius, startY - endRadius, endZ).color(r, g, b, 0.0F).next();
        consumer.vertex(matrix, startX + endRadius, startY - endRadius, endZ).color(r, g, b, 0.0F).next();

        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX - endRadius, startY + endRadius, endZ).color(r, g, b, 0.0F).next();
        consumer.vertex(matrix, startX - endRadius, startY - endRadius, endZ).color(r, g, b, 0.0F).next();

        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX, startY, startZ).color(r, g, b, maxAlpha).next();
        consumer.vertex(matrix, startX + endRadius, startY - endRadius, endZ).color(r, g, b, 0.0F).next();
        consumer.vertex(matrix, startX + endRadius, startY + endRadius, endZ).color(r, g, b, 0.0F).next();
    }
}
