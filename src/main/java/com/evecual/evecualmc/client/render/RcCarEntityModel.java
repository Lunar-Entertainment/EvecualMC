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
                // Main chassis plate
                .uv(0, 0).cuboid(-5.0F, 18.0F, -8.0F, 10.0F, 2.0F, 16.0F)
                // Front hood / nose cone
                .uv(0, 18).cuboid(-4.0F, 15.0F, -7.0F, 8.0F, 3.0F, 6.0F)
                // Cockpit cabin & glass
                .uv(32, 0).cuboid(-3.0F, 12.0F, -2.0F, 6.0F, 3.0F, 6.0F)
                // Rear wing spoiler
                .uv(32, 32).cuboid(-5.0F, 11.0F, 5.0F, 10.0F, 1.0F, 3.0F)
                // Wing struts
                .uv(32, 36).cuboid(-4.0F, 12.0F, 6.0F, 1.0F, 3.0F, 1.0F)
                .uv(32, 36).cuboid(3.0F, 12.0F, 6.0F, 1.0F, 3.0F, 1.0F);
        root.addChild("body", body, ModelTransform.NONE);

        // Antenna
        ModelPartBuilder ant = ModelPartBuilder.create()
                .uv(34, 46).cuboid(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F);
        root.addChild("antenna", ant, ModelTransform.pivot(2.0F, 12.0F, 3.0F));

        // Wheels
        ModelPartBuilder wheel = ModelPartBuilder.create()
                .uv(0, 32).cuboid(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F);

        root.addChild("wheel_fl", wheel, ModelTransform.pivot(-5.5F, 20.0F, -5.0F));
        root.addChild("wheel_fr", wheel, ModelTransform.pivot(5.5F, 20.0F, -5.0F));
        root.addChild("wheel_rl", wheel, ModelTransform.pivot(-5.5F, 20.0F, 5.0F));
        root.addChild("wheel_rr", wheel, ModelTransform.pivot(5.5F, 20.0F, 5.0F));

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

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
