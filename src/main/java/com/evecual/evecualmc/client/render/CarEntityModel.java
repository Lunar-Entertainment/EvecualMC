package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.CarEntity;
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

public class CarEntityModel extends EntityModel<CarEntity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier("evecualmc", "car"), "main");
    private final ModelPart root;

    public CarEntityModel(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // 1. Chassis & Body - Large, proportional, grounded
        ModelPartBuilder body = ModelPartBuilder.create()
                // Main floor plate: 28 wide, 2 thick, 48 long (Z: -24 to +24, Y: 16 to 18)
                .uv(0, 0).cuboid(-14.0F, 16.0F, -24.0F, 28.0F, 2.0F, 48.0F)
                // Left side skirt (between wheels): X: -15.5 to -13.5, Y: 15 to 19, Z: -10 to +10
                .uv(64, 0).cuboid(-15.5F, 15.0F, -10.0F, 2.0F, 4.0F, 20.0F)
                // Right side skirt (between wheels): X: +13.5 to +15.5, Y: 15 to 19, Z: -10 to +10
                .uv(64, 0).cuboid(13.5F, 15.0F, -10.0F, 2.0F, 4.0F, 20.0F)
                // Front bumper: 30 wide, 4 tall, 4 long (Z: -26 to -22, Y: 14 to 18)
                .uv(0, 32).cuboid(-15.0F, 14.0F, -26.0F, 30.0F, 4.0F, 4.0F)
                // Front hood (sloped down towards front): 28 wide, 6 tall, 14 long (Z: -22 to -8, Y: 10 to 16)
                .uv(0, 32).cuboid(-14.0F, 10.0F, -22.0F, 28.0F, 6.0F, 14.0F)
                // Left front fender / wheel arch: X: -16 to -14, Y: 10 to 17, Z: -21 to -11
                .uv(0, 0).cuboid(-16.0F, 10.0F, -21.0F, 2.0F, 7.0F, 10.0F)
                // Right front fender / wheel arch: X: +14 to +16, Y: 10 to 17, Z: -21 to -11
                .uv(0, 0).cuboid(14.0F, 10.0F, -21.0F, 2.0F, 7.0F, 10.0F)
                // Left door / side panel: X: -16 to -13, Y: 8 to 16, Z: -8 to +10
                .uv(0, 0).cuboid(-15.5F, 8.0F, -8.0F, 2.5F, 8.0F, 18.0F)
                // Right door / side panel: X: +13 to +16, Y: 8 to 16, Z: -8 to +10
                .uv(0, 0).cuboid(13.0F, 8.0F, -8.0F, 2.5F, 8.0F, 18.0F)
                // Rear trunk deck: 28 wide, 6 tall, 12 long (Z: +10 to +22, Y: 10 to 16)
                .uv(0, 32).cuboid(-14.0F, 10.0F, 10.0F, 28.0F, 6.0F, 12.0F)
                // Rear bumper: 30 wide, 4 tall, 3 long (Z: +22 to +25, Y: 14 to 18)
                .uv(64, 96).cuboid(-15.0F, 14.0F, 22.0F, 30.0F, 4.0F, 3.0F)
                // Left rear fender / wheel arch: X: -16 to -14, Y: 10 to 17, Z: +10 to +21
                .uv(0, 0).cuboid(-16.0F, 10.0F, 10.0F, 2.0F, 7.0F, 11.0F)
                // Right rear fender / wheel arch: X: +14 to +16, Y: 10 to 17, Z: +10 to +21
                .uv(0, 0).cuboid(14.0F, 10.0F, 10.0F, 2.0F, 7.0F, 11.0F)
                // Rear spoiler: 30 wide, 2 tall, 5 long (Z: +20 to +25, Y: 4 to 6)
                .uv(64, 0).cuboid(-15.0F, 4.0F, 20.0F, 30.0F, 2.0F, 5.0F)
                // Spoiler mounts
                .uv(64, 0).cuboid(-10.0F, 6.0F, 21.0F, 2.0F, 4.0F, 2.0F)
                .uv(64, 0).cuboid(8.0F, 6.0F, 21.0F, 2.0F, 4.0F, 2.0F);

        root.addChild("body", body, ModelTransform.NONE);

        // 2. Interior (Cockpit, Center Driver Seat, Dashboard, Steering Wheel)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Front dashboard: 26 wide, 6 tall, 4 deep (Z: -8 to -4, Y: 9 to 15)
                .uv(0, 64).cuboid(-13.0F, 9.0F, -8.0F, 26.0F, 6.0F, 4.0F)
                // Center Steering wheel column: Z: -4 to -2, X: -1 to +1, Y: 10 to 12
                .uv(0, 64).cuboid(-1.0F, 10.0F, -4.0F, 2.0F, 2.0F, 2.0F)
                // Center Steering wheel: 8 wide, 8 tall, 1 deep (X: -4 to +4, Y: 7 to 15, Z: -2)
                .uv(0, 64).cuboid(-4.0F, 7.0F, -2.0F, 8.0F, 8.0F, 1.0F)

                // Wide Center Driver Bucket Seat:
                // Seat Cushion: 14 wide, 2 tall, 10 long (X: -7 to +7, Y: 14 to 16, Z: -1 to +9)
                .uv(64, 32).cuboid(-7.0F, 14.0F, -1.0F, 14.0F, 2.0F, 10.0F)
                // Seat Backrest: 14 wide, 14 tall, 3 deep (X: -7 to +7, Y: 0 to 14, Z: 8 to 11)
                .uv(64, 32).cuboid(-7.0F, 0.0F, 8.0F, 14.0F, 14.0F, 3.0F)
                // Seat Headrest: 8 wide, 4 tall, 3 deep (X: -4 to +4, Y: -4 to 0, Z: 8 to 11)
                .uv(64, 32).cuboid(-4.0F, -4.0F, 8.0F, 8.0F, 4.0F, 3.0F);

        root.addChild("interior", interior, ModelTransform.NONE);

        // 3. High-Clearance Canopy (High Roof & Tall Windshield - Total Headroom!)
        ModelPartBuilder canopy = ModelPartBuilder.create()
                // Left A-pillar (strut from Y: 8 up to Y: -4, Z: -7 to -5, X: -14.5 to -13)
                .uv(64, 0).cuboid(-14.5F, -4.0F, -7.0F, 1.5F, 12.0F, 2.0F)
                // Right A-pillar (strut from Y: 8 up to Y: -4, Z: -7 to -5, X: +13 to +14.5)
                .uv(64, 0).cuboid(13.0F, -4.0F, -7.0F, 1.5F, 12.0F, 2.0F)
                // Windshield top header frame: 28 wide, 2 tall, 2 deep (Y: -4 to -2, Z: -7 to -5)
                .uv(64, 0).cuboid(-14.0F, -4.0F, -7.0F, 28.0F, 2.0F, 2.0F)
                // Transparent cutout windshield: 26 wide, 10 tall, 1 deep (Y: -2 to 8, Z: -6.5)
                .uv(0, 96).cuboid(-13.0F, -2.0F, -6.5F, 26.0F, 10.0F, 1.0F)
                // Left C-pillar (rear strut from Y: 8 up to Y: -4, Z: +9 to +11)
                .uv(64, 0).cuboid(-14.5F, -4.0F, 9.0F, 1.5F, 12.0F, 2.0F)
                // Right C-pillar (rear strut from Y: 8 up to Y: -4, Z: +9 to +11)
                .uv(64, 0).cuboid(13.0F, -4.0F, 9.0F, 1.5F, 12.0F, 2.0F)
                // Hardtop roof: 28 wide, 2 tall, 18 long (Y: -6 to -4, Z: -7 to +11)
                .uv(0, 0).cuboid(-14.0F, -6.0F, -7.0F, 28.0F, 2.0F, 18.0F);

        root.addChild("canopy", canopy, ModelTransform.NONE);

        // 4. Wheels (Tires & Silver Alloy Rims) - Resting on ground at Y = 24
        // Front Left Wheel
        root.addChild("wheel_fl", ModelPartBuilder.create()
                .uv(64, 64).cuboid(-4.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.pivot(-12.5F, 19.0F, -16.0F));

        // Front Right Wheel
        root.addChild("wheel_fr", ModelPartBuilder.create()
                .uv(64, 64).cuboid(0.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.pivot(12.5F, 19.0F, -16.0F));

        // Rear Left Wheel
        root.addChild("wheel_rl", ModelPartBuilder.create()
                .uv(64, 64).cuboid(-4.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.pivot(-12.5F, 19.0F, 16.0F));

        // Rear Right Wheel
        root.addChild("wheel_rr", ModelPartBuilder.create()
                .uv(64, 64).cuboid(0.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.pivot(12.5F, 19.0F, 16.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(CarEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
