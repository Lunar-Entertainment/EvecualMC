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

            // Transform to align with robot body and arm
            matrices.translate(0.0, 18.0 / 16.0, 0.0); // body pivot
            matrices.translate(-4.0 / 16.0, -4.0 / 16.0, 0.0); // arm pivot

            float swing = robot.getArmSwing();
            if (swing > 0.0F) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(-0.6F - swing * 1.6F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-0.3F + swing * 0.4F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(-0.1F));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(-0.25F));
            }

            // Offset into hand gripper clamp
            matrices.translate(-1.0 / 16.0, 7.0 / 16.0, -4.5 / 16.0);
            matrices.scale(0.65F, 0.65F, 0.65F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45.0F));

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

        matrices.pop();
        super.render(robot, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
