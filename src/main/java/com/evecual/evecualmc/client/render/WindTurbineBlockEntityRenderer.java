package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.block.WindTurbineBlock;
import com.evecual.evecualmc.block.entity.WindTurbineBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class WindTurbineBlockEntityRenderer implements BlockEntityRenderer<WindTurbineBlockEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/block/wind_turbine_head_front.png");

    public WindTurbineBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(WindTurbineBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity == null || entity.getWorld() == null) return;
        BlockState state = entity.getCachedState();
        if (!state.isOf(com.evecual.evecualmc.EvecualMC.WIND_TURBINE_BLOCK)) return;
        if (state.get(WindTurbineBlock.SEGMENT) != 0) return;

        Direction facing = state.get(WindTurbineBlock.FACING);
        float time = entity.getWorld().getTime() + tickDelta;
        float angle = (time * 18.0F) % 360.0F; // Smooth high-speed rotation

        matrices.push();

        // Translate matrix to the center of the top Turbine Head (Segment 3)
        matrices.translate(0.5, 3.5, 0.5);

        // Rotate to match turbine horizontal facing direction
        switch (facing) {
            case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0.0F));
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0F));
            case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            default -> {}
        }

        // Position propeller assembly on the front face of the nacelle housing
        matrices.translate(0.0, 0.0, -0.51);

        // Continuous rotor spin animation around Z-axis
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));

        // 1. Draw Gold Central Propeller Hub
        drawBox(matrices, consumer, -0.15F, -0.15F, -0.12F, 0.15F, 0.15F, 0.0F, light, 245, 158, 11, 255);

        // 2. Draw 4 Real 3D Aerodynamic Propeller Blades (Extending 1.1m out)
        // Top Blade
        drawBox(matrices, consumer, -0.05F, 0.15F, -0.06F, 0.05F, 1.25F, -0.01F, light, 230, 242, 255, 250);
        // Bottom Blade
        drawBox(matrices, consumer, -0.05F, -1.25F, -0.06F, 0.05F, -0.15F, -0.01F, light, 230, 242, 255, 250);
        // Left Blade
        drawBox(matrices, consumer, -1.25F, -0.05F, -0.06F, -0.15F, 0.05F, -0.01F, light, 230, 242, 255, 250);
        // Right Blade
        drawBox(matrices, consumer, 0.15F, -0.05F, -0.06F, 1.25F, 0.05F, -0.01F, light, 230, 242, 255, 250);

        matrices.pop();
    }

    private void drawBox(MatrixStack matrices, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int light, int r, int g, int b, int a) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();
        Matrix3f norm = entry.getNormalMatrix();

        // Front Face (-Z)
        consumer.vertex(mat, minX, minY, minZ).color(r, g, b, a).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, -1.0F).next();
        consumer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, -1.0F).next();
        consumer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, -1.0F).next();
        consumer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, -1.0F).next();

        // Back Face (+Z)
        consumer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, 1.0F).next();
        consumer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, 1.0F).next();
        consumer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, 1.0F).next();
        consumer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(norm, 0.0F, 0.0F, 1.0F).next();
    }
}
