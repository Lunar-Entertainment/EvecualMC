package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.RcDroneEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RcDroneEntityModel extends EntityModel<RcDroneEntity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier("evecualmc", "rc_drone"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart prop_fl;
    private final ModelPart prop_fr;
    private final ModelPart prop_rl;
    private final ModelPart prop_rr;
    private final ModelPart blur_disc_fl;
    private final ModelPart blur_disc_fr;
    private final ModelPart blur_disc_rl;
    private final ModelPart blur_disc_rr;

    public RcDroneEntityModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.prop_fl = root.getChild("prop_fl");
        this.prop_fr = root.getChild("prop_fr");
        this.prop_rl = root.getChild("prop_rl");
        this.prop_rr = root.getChild("prop_rr");
        this.blur_disc_fl = root.getChild("blur_disc_fl");
        this.blur_disc_fr = root.getChild("blur_disc_fr");
        this.blur_disc_rl = root.getChild("blur_disc_rl");
        this.blur_disc_rr = root.getChild("blur_disc_rr");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // 1. Central Aerodynamic Pod & Chassis (uv in 64x64)
        ModelPartBuilder bodyBuilder = ModelPartBuilder.create()
                // Main chassis body pod
                .uv(0, 0).cuboid(-4.0F, 19.0F, -5.0F, 8.0F, 3.0F, 10.0F)
                // Top aerodynamic battery canopy (flush on top of main body)
                .uv(0, 14).cuboid(-3.5F, 17.5F, -4.0F, 7.0F, 1.5F, 8.0F)
                // Bottom cargo bay / payload trunk (flush on bottom)
                .uv(27, 14).cuboid(-3.0F, 22.0F, -3.5F, 6.0F, 1.5F, 7.0F)
                // Front FPV camera gimbal
                .uv(0, 24).cuboid(-1.5F, 19.5F, -6.5F, 3.0F, 2.0F, 2.0F)
                // Landing strut vertical legs (anchored from fuselage Y=21.5 to skids Y=24)
                .uv(0, 29).cuboid(-3.5F, 21.5F, -3.0F, 1.0F, 2.5F, 1.0F)
                .uv(0, 29).cuboid(-3.5F, 21.5F, 2.0F, 1.0F, 2.5F, 1.0F)
                .uv(0, 29).cuboid(2.5F, 21.5F, -3.0F, 1.0F, 2.5F, 1.0F)
                .uv(0, 29).cuboid(2.5F, 21.5F, 2.0F, 1.0F, 2.5F, 1.0F)
                // Landing skid longitudinal rails
                .uv(11, 24).cuboid(-4.0F, 23.5F, -5.5F, 1.5F, 1.0F, 11.0F)
                .uv(11, 24).cuboid(2.5F, 23.5F, -5.5F, 1.5F, 1.0F, 11.0F)
                // Landing skid upturned nose & tail tips
                .uv(11, 24).cuboid(-4.0F, 22.5F, -6.5F, 1.5F, 1.0F, 1.0F)
                .uv(11, 24).cuboid(2.5F, 22.5F, -6.5F, 1.5F, 1.0F, 1.0F)
                .uv(11, 24).cuboid(-4.0F, 22.5F, 5.5F, 1.5F, 1.0F, 1.0F)
                .uv(11, 24).cuboid(2.5F, 22.5F, 5.5F, 1.0F, 1.0F, 1.0F)
                // Structural carbon fiber motor booms (firmly integrated into main fuselage)
                // Front-Left Boom
                .uv(27, 0).cuboid(-8.0F, 19.5F, -7.5F, 5.0F, 1.0F, 1.5F)
                .uv(27, 0).cuboid(-7.5F, 19.5F, -8.0F, 1.5F, 1.0F, 4.0F)
                .uv(27, 0).cuboid(-4.5F, 19.5F, -4.5F, 2.0F, 1.0F, 2.0F)
                // Front-Right Boom
                .uv(27, 0).cuboid(3.0F, 19.5F, -7.5F, 5.0F, 1.0F, 1.5F)
                .uv(27, 0).cuboid(6.0F, 19.5F, -8.0F, 1.5F, 1.0F, 4.0F)
                .uv(27, 0).cuboid(2.5F, 19.5F, -4.5F, 2.0F, 1.0F, 2.0F)
                // Rear-Left Boom
                .uv(27, 0).cuboid(-8.0F, 19.5F, 6.0F, 5.0F, 1.0F, 1.5F)
                .uv(27, 0).cuboid(-7.5F, 19.5F, 4.0F, 1.5F, 1.0F, 4.0F)
                .uv(27, 0).cuboid(-4.5F, 19.5F, 2.5F, 2.0F, 1.0F, 2.0F)
                // Rear-Right Boom
                .uv(27, 0).cuboid(3.0F, 19.5F, 6.0F, 5.0F, 1.0F, 1.5F)
                .uv(27, 0).cuboid(6.0F, 19.5F, 4.0F, 1.5F, 1.0F, 4.0F)
                .uv(27, 0).cuboid(2.5F, 19.5F, 2.5F, 2.0F, 1.0F, 2.0F)
                // Motor Pods at the arm tips (encapsulating the motor shafts)
                .uv(44, 0).cuboid(-8.5F, 18.0F, -8.5F, 3.0F, 3.0F, 3.0F)
                .uv(44, 0).cuboid(5.5F, 18.0F, -8.5F, 3.0F, 3.0F, 3.0F)
                .uv(44, 0).cuboid(-8.5F, 18.0F, 5.5F, 3.0F, 3.0F, 3.0F)
                .uv(44, 0).cuboid(5.5F, 18.0F, 5.5F, 3.0F, 3.0F, 3.0F);
        root.addChild("body", bodyBuilder, ModelTransform.NONE);

        // 2. Propellers (Twin-blade aerofoil rotors centered on motor shafts)
        ModelPartBuilder propBlade = ModelPartBuilder.create()
                // Central hub
                .uv(44, 7).cuboid(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F)
                // Blade 1
                .uv(0, 36).cuboid(-0.5F, -0.4F, -5.0F, 1.0F, 0.4F, 4.0F)
                // Blade 2
                .uv(0, 36).cuboid(-0.5F, -0.4F, 1.0F, 1.0F, 0.4F, 4.0F);

        root.addChild("prop_fl", propBlade, ModelTransform.pivot(-7.0F, 17.5F, -7.0F));
        root.addChild("prop_fr", propBlade, ModelTransform.pivot(7.0F, 17.5F, -7.0F));
        root.addChild("prop_rl", propBlade, ModelTransform.pivot(-7.0F, 17.5F, 7.0F));
        root.addChild("prop_rr", propBlade, ModelTransform.pivot(7.0F, 17.5F, 7.0F));

        // 3. Motion Blur Rotor Discs (Rendered when spinning at high RPM)
        ModelPartBuilder blurDisc = ModelPartBuilder.create()
                .uv(0, 42).cuboid(-5.0F, -0.2F, -5.0F, 10.0F, 0.2F, 10.0F);

        root.addChild("blur_disc_fl", blurDisc, ModelTransform.pivot(-7.0F, 17.5F, -7.0F));
        root.addChild("blur_disc_fr", blurDisc, ModelTransform.pivot(7.0F, 17.5F, -7.0F));
        root.addChild("blur_disc_rl", blurDisc, ModelTransform.pivot(-7.0F, 17.5F, 7.0F));
        root.addChild("blur_disc_rr", blurDisc, ModelTransform.pivot(7.0F, 17.5F, 7.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(RcDroneEntity drone, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        boolean active = drone.getEnergy() > 0 && (drone.isFlying() || !drone.isOnGround() || drone.isAutoReturning()
                || Math.abs(drone.getVelocity().y) > 0.01 || Math.abs(drone.getVelocity().horizontalLength()) > 0.01);

        float angle = active ? animationProgress * 1.85F : 0.0F;

        // Counter-rotating quadcopter propellers (FL & RR clockwise, FR & RL counter-clockwise)
        this.prop_fl.yaw = angle;
        this.prop_rr.yaw = angle;
        this.prop_fr.yaw = -angle;
        this.prop_rl.yaw = -angle;

        // Motion blur discs rotate with slight precession
        float blurAngle = angle * 0.5F;
        this.blur_disc_fl.yaw = blurAngle;
        this.blur_disc_fr.yaw = -blurAngle;
        this.blur_disc_rl.yaw = -blurAngle;
        this.blur_disc_rr.yaw = blurAngle;
    }

    public void renderPropellerGhosts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g, float b, float propSpeed) {
        if (propSpeed > 0.4F) {
            // Render 3 motion-blur ghost blade trails at fractional angles behind the main blade
            float[] offsets = { -0.22F, -0.45F, -0.70F };
            float[] alphas = { 0.45F, 0.25F, 0.12F };

            for (int i = 0; i < offsets.length; i++) {
                float off = offsets[i];
                float a = alphas[i];

                matrices.push();
                this.prop_fl.yaw += off;
                this.prop_rr.yaw += off;
                this.prop_fr.yaw -= off;
                this.prop_rl.yaw -= off;

                this.prop_fl.render(matrices, vertices, light, overlay, r, g, b, a);
                this.prop_fr.render(matrices, vertices, light, overlay, r, g, b, a);
                this.prop_rl.render(matrices, vertices, light, overlay, r, g, b, a);
                this.prop_rr.render(matrices, vertices, light, overlay, r, g, b, a);

                this.prop_fl.yaw -= off;
                this.prop_rr.yaw -= off;
                this.prop_fr.yaw += off;
                this.prop_rl.yaw += off;
                matrices.pop();
            }
        }
    }

    public void renderBlurDiscs(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float alpha) {
        this.blur_disc_fl.render(matrices, vertices, light, overlay, 0.9F, 0.95F, 1.0F, alpha);
        this.blur_disc_fr.render(matrices, vertices, light, overlay, 0.9F, 0.95F, 1.0F, alpha);
        this.blur_disc_rl.render(matrices, vertices, light, overlay, 0.9F, 0.95F, 1.0F, alpha);
        this.blur_disc_rr.render(matrices, vertices, light, overlay, 0.9F, 0.95F, 1.0F, alpha);
    }

    public void renderBodyAndProps(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.prop_fl.render(matrices, vertices, light, overlay, 0.2F, 0.2F, 0.22F, alpha);
        this.prop_fr.render(matrices, vertices, light, overlay, 0.2F, 0.2F, 0.22F, alpha);
        this.prop_rl.render(matrices, vertices, light, overlay, 0.2F, 0.2F, 0.22F, alpha);
        this.prop_rr.render(matrices, vertices, light, overlay, 0.2F, 0.2F, 0.22F, alpha);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        renderBodyAndProps(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
