package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.block.entity.StationaryTurretBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class StationaryTurretBlockEntityRenderer implements BlockEntityRenderer<StationaryTurretBlockEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/block/stationary_turret.png");

    public StationaryTurretBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(StationaryTurretBlockEntity turret, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(TEXTURE));
        MatrixStack.Entry entry = matrices.peek();

        // 1. Static Base Plate (y: 0.0 to 0.25)
        drawBox(consumer, entry, -0.45F, 0.0F, -0.45F, 0.45F, 0.25F, 0.45F, 0.2F, 0.25F, 0.3F, light);

        // 2. Swiveling Turret Head (Rotates on Y axis)
        matrices.translate(0.0, 0.25, 0.0);
        float yaw = MathHelper.lerpAngleDegrees(tickDelta, turret.curYaw, turret.curYaw);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));

        entry = matrices.peek();
        // Turret Center Housing
        drawBox(consumer, entry, -0.3F, 0.0F, -0.3F, 0.3F, 0.35F, 0.3F, 0.15F, 0.2F, 0.25F, light);
        // Cyan Cyber Core Eye
        drawBox(consumer, entry, -0.1F, 0.1F, 0.29F, 0.1F, 0.25F, 0.31F, 0.0F, 0.9F, 1.0F, light);

        // 3. Elevating Dual Barrels (Rotates on X axis)
        matrices.translate(0.0, 0.2, 0.0);
        float pitch = MathHelper.lerp(tickDelta, turret.curPitch, turret.curPitch);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

        entry = matrices.peek();
        // Left Barrel
        drawBox(consumer, entry, -0.22F, -0.06F, 0.1F, -0.10F, 0.06F, 0.75F, 0.1F, 0.12F, 0.15F, light);
        // Left Muzzle Brake
        drawBox(consumer, entry, -0.24F, -0.08F, 0.70F, -0.08F, 0.08F, 0.82F, 0.0F, 0.8F, 0.9F, light);

        // Right Barrel
        drawBox(consumer, entry, 0.10F, -0.06F, 0.1F, 0.22F, 0.06F, 0.75F, 0.1F, 0.12F, 0.15F, light);
        // Right Muzzle Brake
        drawBox(consumer, entry, 0.08F, -0.08F, 0.70F, 0.24F, 0.08F, 0.82F, 0.0F, 0.8F, 0.9F, light);

        matrices.pop();
    }

    private void drawBox(VertexConsumer consumer, MatrixStack.Entry entry, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, int light) {
        Matrix4f pos = entry.getPositionMatrix();

        // Down face (Y-)
        consumer.vertex(pos, minX, minY, minZ).color(r * 0.6f, g * 0.6f, b * 0.6f, 1.0f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();
        consumer.vertex(pos, maxX, minY, minZ).color(r * 0.6f, g * 0.6f, b * 0.6f, 1.0f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();
        consumer.vertex(pos, maxX, minY, maxZ).color(r * 0.6f, g * 0.6f, b * 0.6f, 1.0f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();
        consumer.vertex(pos, minX, minY, maxZ).color(r * 0.6f, g * 0.6f, b * 0.6f, 1.0f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();

        // Up face (Y+)
        consumer.vertex(pos, minX, maxY, maxZ).color(r, g, b, 1.0f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, maxX, maxY, maxZ).color(r, g, b, 1.0f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, maxX, maxY, minZ).color(r, g, b, 1.0f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, minX, maxY, minZ).color(r, g, b, 1.0f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();

        // North face (Z-)
        consumer.vertex(pos, minX, maxY, minZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        consumer.vertex(pos, maxX, maxY, minZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        consumer.vertex(pos, maxX, minY, minZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        consumer.vertex(pos, minX, minY, minZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();

        // South face (Z+)
        consumer.vertex(pos, minX, minY, maxZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
        consumer.vertex(pos, maxX, minY, maxZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
        consumer.vertex(pos, maxX, maxY, maxZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
        consumer.vertex(pos, minX, maxY, maxZ).color(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();

        // West face (X-)
        consumer.vertex(pos, minX, minY, minZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(-1, 0, 0).next();
        consumer.vertex(pos, minX, minY, maxZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(-1, 0, 0).next();
        consumer.vertex(pos, minX, maxY, maxZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(-1, 0, 0).next();
        consumer.vertex(pos, minX, maxY, minZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(-1, 0, 0).next();

        // East face (X+)
        consumer.vertex(pos, maxX, minY, maxZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 0, 0).next();
        consumer.vertex(pos, maxX, minY, minZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 0, 0).next();
        consumer.vertex(pos, maxX, maxY, minZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 0, 0).next();
        consumer.vertex(pos, maxX, maxY, maxZ).color(r * 0.7f, g * 0.7f, b * 0.7f, 1.0f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 0, 0).next();
    }
}
