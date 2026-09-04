package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.RcRobotEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class RcRobotEntityRenderer extends EntityRenderer<RcRobotEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/rc_robot.png");

    private final RcRobotEntityModel model;

    public RcRobotEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.35F;
        this.model = new RcRobotEntityModel(context.getPart(RcRobotEntityModel.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(RcRobotEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RcRobotEntity robot, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 1. Heading Rotation
        float robotYaw = MathHelper.lerpAngleDegrees(tickDelta, robot.prevYaw, robot.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - robotYaw));

        // 2. Coordinate orientation for Minecraft entity models
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0, -1.5, 0.0);

        this.model.setAngles(robot, 0.0F, 0.0F, robot.age + tickDelta, 0.0F, 0.0F);

        // 3. Render Robot Model Body
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture(robot)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        // 4. Render Equipped 3D Tool in Right Hand Gripper
        ItemStack tool = robot.getEquippedTool();
        if (!tool.isEmpty()) {
            matrices.push();

            this.model.getBody().rotate(matrices);
            this.model.getRightArm().rotate(matrices);

            // Center directly into hand gripper clamp
            matrices.translate(-0.5 / 16.0, 7.0 / 16.0, -4.0 / 16.0);
            matrices.scale(0.68F, 0.68F, 0.68F);

            // Natural tool holding orientation pointing forward and upright
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

            MinecraftClient.getInstance().getItemRenderer().renderItem(
                    tool,
                    ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    robot.getWorld(),
                    robot.getId()
            );

            matrices.pop();
        }

        // 5. Render Work Light Beam when Light is ON
        if (robot.isLightOn()) {
            matrices.push();
            this.model.getBody().rotate(matrices);
            this.model.getHead().rotate(matrices);

            renderLightCone(matrices, vertexConsumers, 0.0F, -3.2F / 16.0F, -3.5F / 16.0F, 5.0F, 0.9F, 1.0F, 0.95F, 0.7F, 0.35F);
            matrices.pop();
        }

        matrices.pop();
        super.render(robot, yaw, tickDelta, matrices, vertexConsumers, light);
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
