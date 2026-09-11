package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.FlyingTurretEntity;
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
import org.joml.Matrix4f;

public class FlyingTurretEntityRenderer extends EntityRenderer<FlyingTurretEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/flying_turret.png");

    public FlyingTurretEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.45F;
    }

    @Override
    public Identifier getTexture(FlyingTurretEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(FlyingTurretEntity drone, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 1. Heading Rotation
        float droneYaw = MathHelper.lerpAngleDegrees(tickDelta, drone.prevYaw, drone.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - droneYaw));

        // 2. Flight Pitch & Bobbing
        float pitch = MathHelper.lerp(tickDelta, drone.prevPitch, drone.getPitch());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

        matrices.translate(0.0, 0.5, 0.0);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(TEXTURE));
        MatrixStack.Entry entry = matrices.peek();

        // 3. Central Drone Chassis (x: -0.35..0.35, y: -0.15..0.15, z: -0.35..0.35)
        drawBox(consumer, entry, -0.35F, -0.15F, -0.35F, 0.35F, 0.15F, 0.35F, 0.15F, 0.22F, 0.3F, light);

        // Front Cyan Sensor Visor
        drawBox(consumer, entry, -0.2F, -0.05F, -0.37F, 0.2F, 0.08F, -0.34F, 0.0F, 0.9F, 1.0F, light);

        // 4. Quad-Rotor Arms
        // Front-Left Arm & Rotor
        drawBox(consumer, entry, 0.3F, 0.0F, -0.6F, 0.55F, 0.06F, -0.3F, 0.2F, 0.25F, 0.35F, light);
        drawRotorDisc(consumer, entry, 0.55F, 0.1F, -0.6F, drone.rotorAngle, light);

        // Front-Right Arm & Rotor
        drawBox(consumer, entry, -0.55F, 0.0F, -0.6F, -0.3F, 0.06F, -0.3F, 0.2F, 0.25F, 0.35F, light);
        drawRotorDisc(consumer, entry, -0.55F, 0.1F, -0.6F, -drone.rotorAngle, light);

        // Rear-Left Arm & Rotor
        drawBox(consumer, entry, 0.3F, 0.0F, 0.3F, 0.55F, 0.06F, 0.6F, 0.2F, 0.25F, 0.35F, light);
        drawRotorDisc(consumer, entry, 0.55F, 0.1F, 0.6F, -drone.rotorAngle, light);

        // Rear-Right Arm & Rotor
        drawBox(consumer, entry, -0.55F, 0.0F, 0.3F, -0.3F, 0.06F, 0.6F, 0.2F, 0.25F, 0.35F, light);
        drawRotorDisc(consumer, entry, -0.55F, 0.1F, 0.6F, drone.rotorAngle, light);

        // 5. Underslung Gimbal Gun Pod
        drawBox(consumer, entry, -0.12F, -0.35F, -0.12F, 0.12F, -0.15F, 0.12F, 0.1F, 0.15F, 0.2F, light);
        // Barrel
        drawBox(consumer, entry, -0.05F, -0.30F, -0.55F, 0.05F, -0.22F, 0.0F, 0.08F, 0.10F, 0.12F, light);
        // Cyan Barrel Tip
        drawBox(consumer, entry, -0.06F, -0.31F, -0.60F, 0.06F, -0.21F, -0.52F, 0.0F, 0.9F, 1.0F, light);

        matrices.pop();
        super.render(drone, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void drawRotorDisc(VertexConsumer consumer, MatrixStack.Entry entry, float cx, float cy, float cz, float angle, int light) {
        Matrix4f pos = entry.getPositionMatrix();
        float rad = 0.28F;
        float cos = (float) Math.cos(Math.toRadians(angle)) * rad;
        float sin = (float) Math.sin(Math.toRadians(angle)) * rad;

        // Blade 1
        consumer.vertex(pos, cx - cos, cy, cz - sin).color(0.3F, 0.8F, 1.0F, 0.8F).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, cx + cos, cy, cz + sin).color(0.3F, 0.8F, 1.0F, 0.8F).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, cx + cos, cy + 0.02F, cz + sin).color(0.3F, 0.8F, 1.0F, 0.8F).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, cx - cos, cy + 0.02F, cz - sin).color(0.3F, 0.8F, 1.0F, 0.8F).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();

        // Blade 2
        consumer.vertex(pos, cx - sin, cy, cz + cos).color(0.3F, 0.8F, 1.0F, 0.8F).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, cx + sin, cy, cz - cos).color(0.3F, 0.8F, 1.0F, 0.8F).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, cx + sin, cy + 0.02F, cz - cos).color(0.3F, 0.8F, 1.0F, 0.8F).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        consumer.vertex(pos, cx - sin, cy + 0.02F, cz + cos).color(0.3F, 0.8F, 1.0F, 0.8F).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
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
