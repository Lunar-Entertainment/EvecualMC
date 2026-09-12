package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.PickupDroneEntity;
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

public class PickupDroneEntityModel extends EntityModel<PickupDroneEntity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier("evecualmc", "pickup_drone"), "main");

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

    public PickupDroneEntityModel(ModelPart root) {
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

        // 1. Central Armored Defense Fuselage & Vacuum Harvester
        ModelPartBuilder bodyBuilder = ModelPartBuilder.create()
                // Main armored stealth fuselage
                .uv(0, 0).cuboid(-4.5F, 19.0F, -5.5F, 9.0F, 3.0F, 11.0F)
                // Top composite armor canopy / upper deck
                .uv(0, 14).cuboid(-3.5F, 17.2F, -4.5F, 7.0F, 1.8F, 9.0F)
                // Tactical Sensor Radome & Comms Dome on top
                .uv(32, 14).cuboid(-2.0F, 15.7F, -2.0F, 4.0F, 1.5F, 4.0F)
                .uv(32, 14).cuboid(-0.5F, 14.5F, -0.5F, 1.0F, 1.2F, 1.0F) // Mast
                // Front Tactical FLIR Targeting Gimbal & Sensor Aperture
                .uv(0, 25).cuboid(-2.0F, 19.2F, -7.5F, 4.0F, 2.4F, 2.0F)
                .uv(12, 25).cuboid(-1.0F, 19.7F, -8.0F, 2.0F, 1.4F, 0.5F) // Lens shroud

                // Underslung Automated Vacuum Funnel & Magnetic Harvester
                // Upper vacuum plenum
                .uv(24, 20).cuboid(-3.5F, 22.0F, -3.5F, 7.0F, 1.5F, 7.0F)
                // Suction Cowl / Tractor Emitter Ring
                .uv(18, 28).cuboid(-2.5F, 23.5F, -2.5F, 5.0F, 1.2F, 5.0F)
                // Magnetic Vortex Intake Core
                .uv(0, 29).cuboid(-1.5F, 24.7F, -1.5F, 3.0F, 0.5F, 3.0F)

                // Dual Heavy Defensive Sensor / Conduit Side Pods
                // Left Pod
                .uv(38, 28).cuboid(-6.0F, 19.3F, -4.0F, 1.5F, 2.2F, 8.0F)
                .uv(38, 28).cuboid(-6.5F, 20.0F, -4.8F, 1.0F, 1.0F, 1.0F) // Forward sensor
                // Right Pod
                .uv(38, 28).cuboid(4.5F, 19.3F, -4.0F, 1.5F, 2.2F, 8.0F)
                .uv(38, 28).cuboid(5.5F, 20.0F, -4.8F, 1.0F, 1.0F, 1.0F) // Forward sensor

                // Heavy Structural Carbon Motor Booms (reinforced continuous trusses without holes)
                // Front-Left Boom
                .uv(0, 34).cuboid(-5.5F, 19.2F, -5.5F, 2.0F, 1.4F, 2.0F)
                .uv(0, 34).cuboid(-8.0F, 19.2F, -7.5F, 4.0F, 1.2F, 2.5F)
                .uv(0, 34).cuboid(-8.5F, 19.2F, -8.5F, 2.5F, 1.2F, 3.5F)
                // Front-Right Boom
                .uv(0, 34).cuboid(3.5F, 19.2F, -5.5F, 2.0F, 1.4F, 2.0F)
                .uv(0, 34).cuboid(4.0F, 19.2F, -7.5F, 4.0F, 1.2F, 2.5F)
                .uv(0, 34).cuboid(6.0F, 19.2F, -8.5F, 2.5F, 1.2F, 3.5F)
                // Rear-Left Boom
                .uv(0, 34).cuboid(-5.5F, 19.2F, 3.5F, 2.0F, 1.4F, 2.0F)
                .uv(0, 34).cuboid(-8.0F, 19.2F, 5.0F, 4.0F, 1.2F, 2.5F)
                .uv(0, 34).cuboid(-8.5F, 19.2F, 5.0F, 2.5F, 1.2F, 3.5F)
                // Rear-Right Boom
                .uv(0, 34).cuboid(3.5F, 19.2F, 3.5F, 2.0F, 1.4F, 2.0F)
                .uv(0, 34).cuboid(4.0F, 19.2F, 5.0F, 4.0F, 1.2F, 2.5F)
                .uv(0, 34).cuboid(6.0F, 19.2F, 5.0F, 2.5F, 1.2F, 3.5F)

                // Armored Heavy Brushless Motor Housings
                .uv(44, 0).cuboid(-9.5F, 18.0F, -9.5F, 3.0F, 3.0F, 3.0F)
                .uv(44, 0).cuboid(6.5F, 18.0F, -9.5F, 3.0F, 3.0F, 3.0F)
                .uv(44, 0).cuboid(-9.5F, 18.0F, 6.5F, 3.0F, 3.0F, 3.0F)
                .uv(44, 0).cuboid(6.5F, 18.0F, 6.5F, 3.0F, 3.0F, 3.0F)

                // Tactical Shock-Absorbing Landing Struts and Skid Rails
                .uv(0, 42).cuboid(-4.0F, 21.5F, -3.5F, 1.5F, 3.0F, 1.5F)
                .uv(0, 42).cuboid(-4.0F, 21.5F, 2.0F, 1.5F, 3.0F, 1.5F)
                .uv(0, 42).cuboid(2.5F, 21.5F, -3.5F, 1.5F, 3.0F, 1.5F)
                .uv(0, 42).cuboid(2.5F, 21.5F, 2.0F, 1.5F, 3.0F, 1.5F)
                // Skid Rails
                .uv(14, 38).cuboid(-4.5F, 24.2F, -6.5F, 1.8F, 1.0F, 13.0F)
                .uv(14, 38).cuboid(2.7F, 24.2F, -6.5F, 1.8F, 1.0F, 13.0F)
                // Upturned tips
                .uv(14, 38).cuboid(-4.5F, 23.4F, -7.5F, 1.8F, 1.0F, 1.2F)
                .uv(14, 38).cuboid(2.7F, 23.4F, -7.5F, 1.8F, 1.0F, 1.2F)
                .uv(14, 38).cuboid(-4.5F, 23.4F, 6.3F, 1.8F, 1.0F, 1.2F)
                .uv(14, 38).cuboid(2.7F, 23.4F, 6.3F, 1.8F, 1.0F, 1.2F);

        root.addChild("body", bodyBuilder, ModelTransform.NONE);

        // 2. Propellers
        ModelPartBuilder propBlade = ModelPartBuilder.create()
                .uv(44, 7).cuboid(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F)
                .uv(0, 46).cuboid(-0.5F, -0.4F, -5.0F, 1.0F, 0.4F, 4.0F)
                .uv(0, 46).cuboid(-0.5F, -0.4F, 1.0F, 1.0F, 0.4F, 4.0F);

        root.addChild("prop_fl", propBlade, ModelTransform.pivot(-8.0F, 17.5F, -8.0F));
        root.addChild("prop_fr", propBlade, ModelTransform.pivot(8.0F, 17.5F, -8.0F));
        root.addChild("prop_rl", propBlade, ModelTransform.pivot(-8.0F, 17.5F, 8.0F));
        root.addChild("prop_rr", propBlade, ModelTransform.pivot(8.0F, 17.5F, 8.0F));

        // 3. Motion Blur Rotor Discs
        ModelPartBuilder blurDisc = ModelPartBuilder.create()
                .uv(0, 52).cuboid(-5.0F, -0.2F, -5.0F, 10.0F, 0.2F, 10.0F);

        root.addChild("blur_disc_fl", blurDisc, ModelTransform.pivot(-8.0F, 17.5F, -8.0F));
        root.addChild("blur_disc_fr", blurDisc, ModelTransform.pivot(8.0F, 17.5F, -8.0F));
        root.addChild("blur_disc_rl", blurDisc, ModelTransform.pivot(-8.0F, 17.5F, 8.0F));
        root.addChild("blur_disc_rr", blurDisc, ModelTransform.pivot(8.0F, 17.5F, 8.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(PickupDroneEntity drone, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float angle = animationProgress * 2.2F;

        // Quadcopter counter-rotating propellers
        this.prop_fl.yaw = angle;
        this.prop_rr.yaw = angle;
        this.prop_fr.yaw = -angle;
        this.prop_rl.yaw = -angle;

        // Motion blur discs
        float blurAngle = angle * 0.5F;
        this.blur_disc_fl.yaw = blurAngle;
        this.blur_disc_fr.yaw = -blurAngle;
        this.blur_disc_rl.yaw = -blurAngle;
        this.blur_disc_rr.yaw = blurAngle;
    }

    public void renderPropellerGhosts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g, float b, float alpha) {
        float[] offsets = { -0.22F, -0.45F, -0.70F };
        float[] alphas = { 0.45F, 0.25F, 0.12F };

        for (int i = 0; i < offsets.length; i++) {
            float off = offsets[i];
            float a = alphas[i] * alpha;

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
            this.prop_rr.yaw += off;
            matrices.pop();
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
