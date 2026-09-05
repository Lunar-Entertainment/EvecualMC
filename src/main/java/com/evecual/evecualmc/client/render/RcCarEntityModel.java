package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.RcCarEntity;
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

public class RcCarEntityModel extends EntityModel<RcCarEntity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier("evecualmc", "rc_car"), "main");

    private final ModelPart root;
    private final ModelPart wheel_fl;
    private final ModelPart wheel_fr;
    private final ModelPart wheel_rl;
    private final ModelPart wheel_rr;
    private final ModelPart antenna;

    public RcCarEntityModel(ModelPart root) {
        this.root = root;
        this.wheel_fl = root.getChild("wheel_fl");
        this.wheel_fr = root.getChild("wheel_fr");
        this.wheel_rl = root.getChild("wheel_rl");
        this.wheel_rr = root.getChild("wheel_rr");
        this.antenna = root.getChild("antenna");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Body & Chassis (uv 0, 0 in 64x64)
        ModelPartBuilder body = ModelPartBuilder.create()
                // Main chassis plate (solid lower foundation)
                .uv(0, 0).cuboid(-5.0F, 18.0F, -8.0F, 10.0F, 2.0F, 16.0F)
                // Front aerodynamic nose cone & bumper
                .uv(0, 18).cuboid(-4.5F, 15.0F, -7.5F, 9.0F, 3.0F, 6.5F)
                // Front low splitter
                .uv(0, 0).cuboid(-5.0F, 18.5F, -8.5F, 10.0F, 1.5F, 1.0F)
                // Cockpit cabin & tinted glass
                .uv(32, 0).cuboid(-3.5F, 12.0F, -1.5F, 7.0F, 3.0F, 5.5F)
                // Rear engine deck & trunk cover (closes the gap behind cockpit)
                .uv(0, 18).cuboid(-4.5F, 15.0F, 3.5F, 9.0F, 3.0F, 4.5F)
                // Rear aerodynamic spoiler wing
                .uv(32, 32).cuboid(-5.5F, 10.5F, 4.5F, 11.0F, 1.0F, 3.5F)
                // Wing struts (firmly anchored into rear engine deck at Y=15)
                .uv(32, 36).cuboid(-3.5F, 11.5F, 5.5F, 1.0F, 3.5F, 1.5F)
                .uv(32, 36).cuboid(2.5F, 11.5F, 5.5F, 1.0F, 3.5F, 1.5F)
                // Side skirts (connecting wheels and chassis)
                .uv(0, 0).cuboid(-5.2F, 18.0F, -3.0F, 0.4F, 1.5F, 6.0F)
                .uv(0, 0).cuboid(4.8F, 18.0F, -3.0F, 0.4F, 1.5F, 6.0F);
        root.addChild("body", body, ModelTransform.NONE);

        // Antenna (firmly mounted on cockpit roof)
        ModelPartBuilder ant = ModelPartBuilder.create()
                .uv(34, 46).cuboid(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F);
        root.addChild("antenna", ant, ModelTransform.pivot(2.0F, 12.0F, 2.5F));

        // Wheels (placed flush against chassis sides with 0 gaps)
        ModelPartBuilder wheelLeft = ModelPartBuilder.create()
                .uv(0, 32).cuboid(-1.5F, -2.5F, -2.5F, 1.5F, 5.0F, 5.0F);
        ModelPartBuilder wheelRight = ModelPartBuilder.create()
                .uv(0, 32).cuboid(0.0F, -2.5F, -2.5F, 1.5F, 5.0F, 5.0F);

        root.addChild("wheel_fl", wheelLeft, ModelTransform.pivot(-5.0F, 19.5F, -4.5F));
        root.addChild("wheel_fr", wheelRight, ModelTransform.pivot(5.0F, 19.5F, -4.5F));
        root.addChild("wheel_rl", wheelLeft, ModelTransform.pivot(-5.0F, 19.5F, 4.5F));
        root.addChild("wheel_rr", wheelRight, ModelTransform.pivot(5.0F, 19.5F, 4.5F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(RcCarEntity car, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float steer = (float) Math.toRadians(car.getSteeringAngle());
        this.wheel_fl.yaw = steer;
        this.wheel_fr.yaw = steer;

        float roll = car.getWheelRoll();
        this.wheel_fl.pitch = roll;
        this.wheel_fr.pitch = roll;
        this.wheel_rl.pitch = roll;
        this.wheel_rr.pitch = roll;

        // Dynamic flexible antenna swaying
        this.antenna.pitch = (float) (-car.getCurrentSpeed() * 1.2) + (float) Math.sin(animationProgress * 0.25F) * 0.05F;
        this.antenna.roll = (float) Math.sin(animationProgress * 0.2F) * 0.04F;
    }

    public void renderWheelMotionBlur(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float speed) {
        if (Math.abs(speed) > 0.08F) {
            float dir = speed > 0 ? -1.0F : 1.0F;
            float[] offsets = { dir * 0.18F, dir * 0.38F, dir * 0.60F };
            float[] alphas = { 0.40F, 0.22F, 0.10F };

            for (int i = 0; i < offsets.length; i++) {
                float off = offsets[i];
                float a = alphas[i];

                matrices.push();
                this.wheel_fl.pitch += off;
                this.wheel_fr.pitch += off;
                this.wheel_rl.pitch += off;
                this.wheel_rr.pitch += off;

                this.wheel_fl.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);
                this.wheel_fr.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);
                this.wheel_rl.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);
                this.wheel_rr.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);

                this.wheel_fl.pitch -= off;
                this.wheel_fr.pitch -= off;
                this.wheel_rl.pitch -= off;
                this.wheel_rr.pitch -= off;
                matrices.pop();
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
