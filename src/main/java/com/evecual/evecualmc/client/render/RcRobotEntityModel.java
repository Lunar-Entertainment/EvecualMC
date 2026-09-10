package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.RcRobotEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RcRobotEntityModel extends EntityModel<RcRobotEntity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier("evecualmc", "rc_robot"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart leftTrack;
    private final ModelPart rightTrack;
    private final ModelPart[] leftWheels = new ModelPart[4];
    private final ModelPart[] rightWheels = new ModelPart[4];
    private final ModelPart[] leftCleatsTop = new ModelPart[6];
    private final ModelPart[] leftCleatsBottom = new ModelPart[6];
    private final ModelPart[] rightCleatsTop = new ModelPart[6];
    private final ModelPart[] rightCleatsBottom = new ModelPart[6];

    public RcRobotEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.leftTrack = root.getChild("left_track");
        this.rightTrack = root.getChild("right_track");

        for (int i = 0; i < 4; i++) {
            this.leftWheels[i] = this.leftTrack.getChild("wheel_" + i);
            this.rightWheels[i] = this.rightTrack.getChild("wheel_" + i);
        }
        for (int i = 0; i < 6; i++) {
            this.leftCleatsTop[i] = this.leftTrack.getChild("cleat_top_" + i);
            this.leftCleatsBottom[i] = this.leftTrack.getChild("cleat_bot_" + i);
            this.rightCleatsTop[i] = this.rightTrack.getChild("cleat_top_" + i);
            this.rightCleatsBottom[i] = this.rightTrack.getChild("cleat_bot_" + i);
        }
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData rootData = modelData.getRoot();

        // 1. Dual Tracked Drive Units (Treads)
        ModelPartData leftTrackData = rootData.addChild("left_track",
                ModelPartBuilder.create()
                        .uv(0, 36).cuboid(4.5F, -3.0F, -7.0F, 3.0F, 6.0F, 14.0F)
                        .uv(0, 56).cuboid(4.2F, -2.5F, -6.5F, 0.4F, 5.0F, 13.0F),
                ModelTransform.pivot(0.0F, 21.0F, 0.0F));

        ModelPartData rightTrackData = rootData.addChild("right_track",
                ModelPartBuilder.create()
                        .uv(0, 36).cuboid(-7.5F, -3.0F, -7.0F, 3.0F, 6.0F, 14.0F)
                        .uv(0, 56).cuboid(-4.6F, -2.5F, -6.5F, 0.4F, 5.0F, 13.0F),
                ModelTransform.pivot(0.0F, 21.0F, 0.0F));

        // Animated Road Wheels inside tracks
        float[] wheelZ = { -4.6F, -1.5F, 1.5F, 4.6F };
        for (int i = 0; i < 4; i++) {
            leftTrackData.addChild("wheel_" + i,
                    ModelPartBuilder.create().uv(0, 36).cuboid(-1.4F, -2.0F, -2.0F, 2.8F, 4.0F, 4.0F),
                    ModelTransform.pivot(6.0F, 0.0F, wheelZ[i]));

            rightTrackData.addChild("wheel_" + i,
                    ModelPartBuilder.create().uv(0, 36).cuboid(-1.4F, -2.0F, -2.0F, 2.8F, 4.0F, 4.0F),
                    ModelTransform.pivot(-6.0F, 0.0F, wheelZ[i]));
        }

        // Animated rolling tread cleats/ribs along the band
        for (int i = 0; i < 6; i++) {
            leftTrackData.addChild("cleat_top_" + i,
                    ModelPartBuilder.create().uv(0, 56).cuboid(-1.6F, -3.3F, -0.5F, 3.2F, 0.4F, 1.0F),
                    ModelTransform.pivot(6.0F, 0.0F, -5.0F + i * 2.0F));
            leftTrackData.addChild("cleat_bot_" + i,
                    ModelPartBuilder.create().uv(0, 56).cuboid(-1.6F, 2.9F, -0.5F, 3.2F, 0.4F, 1.0F),
                    ModelTransform.pivot(6.0F, 0.0F, 5.0F - i * 2.0F));

            rightTrackData.addChild("cleat_top_" + i,
                    ModelPartBuilder.create().uv(0, 56).cuboid(-1.6F, -3.3F, -0.5F, 3.2F, 0.4F, 1.0F),
                    ModelTransform.pivot(-6.0F, 0.0F, -5.0F + i * 2.0F));
            rightTrackData.addChild("cleat_bot_" + i,
                    ModelPartBuilder.create().uv(0, 56).cuboid(-1.6F, 2.9F, -0.5F, 3.2F, 0.4F, 1.0F),
                    ModelTransform.pivot(-6.0F, 0.0F, 5.0F - i * 2.0F));
        }

        // 2. Central Body Chassis & Battery Core
        ModelPartData bodyData = rootData.addChild("body",
                ModelPartBuilder.create()
                        // Lower axle bridge
                        .uv(34, 40).cuboid(-4.5F, 1.0F, -4.0F, 9.0F, 3.0F, 8.0F)
                        // Armored core chassis
                        .uv(0, 0).cuboid(-4.0F, -6.0F, -4.5F, 8.0F, 7.0F, 9.0F)
                        // Front cyber-vent radiator
                        .uv(34, 0).cuboid(-3.0F, -4.0F, -5.2F, 6.0F, 4.0F, 1.0F)
                        // Top armor plate
                        .uv(0, 16).cuboid(-3.5F, -7.0F, -3.5F, 7.0F, 1.0F, 7.0F),
                ModelTransform.pivot(0.0F, 18.0F, 0.0F));

        // 3. Sensor Head (Swiveling dome + glowing optic lens)
        bodyData.addChild("head",
                ModelPartBuilder.create()
                        // Neck swivel mount
                        .uv(34, 10).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F)
                        // Ocular sensor turret
                        .uv(34, 16).cuboid(-2.5F, -5.0F, -2.5F, 5.0F, 4.0F, 5.0F)
                        // Front camera lens visor
                        .uv(34, 26).cuboid(-1.5F, -4.2F, -3.4F, 3.0F, 2.0F, 1.0F),
                ModelTransform.pivot(0.0F, -7.0F, 0.0F));

        // 4. Right Tool Arm (Articulated hydraulic arm holding the tool)
        bodyData.addChild("right_arm",
                ModelPartBuilder.create()
                        // Shoulder hinge
                        .uv(25, 16).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                        // Upper arm hydraulic piston
                        .uv(25, 20).cuboid(-1.5F, 1.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                        // Elbow joint & forearm
                        .uv(48, 0).cuboid(-1.5F, 6.0F, -3.0F, 2.0F, 2.0F, 4.0F)
                        // Tool Gripper Clamp
                        .uv(48, 7).cuboid(-1.0F, 6.5F, -5.5F, 1.0F, 1.0F, 3.0F),
                ModelTransform.pivot(-4.0F, -4.0F, 0.0F));

        // 5. Left Auxiliary Claw Arm
        bodyData.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(25, 16).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                        .uv(25, 20).cuboid(-0.5F, 1.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                        .uv(48, 0).cuboid(-0.5F, 6.0F, -2.0F, 2.0F, 2.0F, 3.0F)
                        .uv(48, 12).cuboid(0.0F, 5.5F, -3.5F, 1.0F, 3.0F, 2.0F), // 2-prong claw
                ModelTransform.pivot(4.0F, -4.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(RcRobotEntity robot, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Optical sensor head tilt based on aim/pitch
        this.head.pitch = robot.getHeadPitch() * 0.017453292F;
        this.head.yaw = 0.0F;

        // Arm swing animation when LMB is used
        float swing = robot.getArmSwing();
        if (swing > 0.0F) {
            this.rightArm.pitch = -0.6F - swing * 1.6F;
            this.rightArm.yaw = -0.3F + swing * 0.4F;
            this.rightArm.roll = -0.1F;
        } else {
            this.rightArm.pitch = -0.25F;
            this.rightArm.yaw = 0.0F;
            this.rightArm.roll = 0.0F;
        }

        // Left arm swing animation when RMB is used (Block placement)
        float leftSwing = robot.getLeftArmSwing();
        if (leftSwing > 0.0F) {
            this.leftArm.pitch = -0.6F - leftSwing * 1.6F;
            this.leftArm.yaw = 0.3F - leftSwing * 0.4F;
            this.leftArm.roll = 0.1F;
        } else {
            this.leftArm.pitch = -0.25F;
            this.leftArm.yaw = 0.0F;
            this.leftArm.roll = 0.0F;
        }

        // Tank tread wheels rotation and moving band cleats (inverted to roll forward in sync with motion)
        float tickDelta = animationProgress - (int)animationProgress;
        float leftRoll = robot.getLeftTreadRoll(tickDelta);
        float rightRoll = robot.getRightTreadRoll(tickDelta);

        for (int i = 0; i < 4; i++) {
            this.leftWheels[i].pitch = -leftRoll;
            this.rightWheels[i].pitch = -rightRoll;
        }

        float trackLength = 12.0F;
        float spacing = trackLength / 6.0F;
        for (int i = 0; i < 6; i++) {
            float leftTopZ = 5.8F - floorMod(i * spacing + leftRoll * 0.45F, trackLength);
            float leftBotZ = -5.8F + floorMod(i * spacing + leftRoll * 0.45F, trackLength);
            this.leftCleatsTop[i].pivotZ = leftTopZ;
            this.leftCleatsBottom[i].pivotZ = leftBotZ;

            float rightTopZ = 5.8F - floorMod(i * spacing + rightRoll * 0.45F, trackLength);
            float rightBotZ = -5.8F + floorMod(i * spacing + rightRoll * 0.45F, trackLength);
            this.rightCleatsTop[i].pivotZ = rightTopZ;
            this.rightCleatsBottom[i].pivotZ = rightBotZ;
        }
    }

    private static float floorMod(float value, float mod) {
        float rem = value % mod;
        if (rem < 0.0F) rem += mod;
        return rem;
    }

    public ModelPart getRightArm() {
        return rightArm;
    }

    public ModelPart getLeftArm() {
        return leftArm;
    }

    public ModelPart getBody() {
        return body;
    }

    public ModelPart getHead() {
        return head;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
