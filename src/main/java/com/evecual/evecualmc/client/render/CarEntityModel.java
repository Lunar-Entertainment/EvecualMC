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

        // 1. Chassis & Body (Red Metallic & Dark Skirts) - Model -Z is Front, +Z is Rear
        ModelPartBuilder body = ModelPartBuilder.create()
                // Main chassis floor: 24 wide, 2 thick, 42 long (Z: -21 to +21, Y: 16 to 18)
                .uv(0, 0).cuboid(-12.0F, 16.0F, -21.0F, 24.0F, 2.0F, 42.0F)
                // Left side skirt (between wheels, seals gap): Z: -9 to +9, Y: 15 to 19, X: -13 to -11
                .uv(64, 0).cuboid(-13.5F, 15.0F, -9.0F, 2.0F, 4.0F, 18.0F)
                // Right side skirt (between wheels, seals gap): Z: -9 to +9, Y: 15 to 19, X: 11 to 13
                .uv(64, 0).cuboid(11.5F, 15.0F, -9.0F, 2.0F, 4.0F, 18.0F)
                // Front bumper: 26 wide, 4 tall, 3 long (Z: -23 to -20, Y: 14 to 18)
                .uv(0, 32).cuboid(-13.0F, 14.0F, -23.0F, 26.0F, 4.0F, 3.0F)
                // Front hood (sloped down towards front): 24 wide, 5 tall, 14 long (Z: -20 to -6, Y: 11 to 16)
                .uv(0, 32).cuboid(-12.0F, 11.0F, -20.0F, 24.0F, 5.0F, 14.0F)
                // Left front fender / wheel cover: Z: -19 to -9, Y: 11 to 17, X: -14 to -12
                .uv(0, 0).cuboid(-14.0F, 11.0F, -19.0F, 2.0F, 6.0F, 10.0F)
                // Right front fender / wheel cover: Z: -19 to -9, Y: 11 to 17, X: 12 to 14
                .uv(0, 0).cuboid(12.0F, 11.0F, -19.0F, 2.0F, 6.0F, 10.0F)
                // Left door / side panel: Z: -6 to +8, Y: 11 to 16, X: -14 to -11
                .uv(0, 0).cuboid(-13.5F, 11.0F, -6.0F, 2.0F, 5.0F, 14.0F)
                // Right door / side panel: Z: -6 to +8, Y: 11 to 16, X: 11 to 14
                .uv(0, 0).cuboid(11.5F, 11.0F, -6.0F, 2.0F, 5.0F, 14.0F)
                // Rear trunk deck: 24 wide, 5 tall, 12 long (Z: +8 to +20, Y: 11 to 16)
                .uv(0, 32).cuboid(-12.0F, 11.0F, 8.0F, 24.0F, 5.0F, 12.0F)
                // Rear bumper: 26 wide, 4 tall, 3 long (Z: +20 to +23, Y: 14 to 18)
                .uv(64, 96).cuboid(-13.0F, 14.0F, 20.0F, 26.0F, 4.0F, 3.0F)
                // Left rear fender / wheel cover: Z: +9 to +19, Y: 11 to 17, X: -14 to -12
                .uv(0, 0).cuboid(-14.0F, 11.0F, 9.0F, 2.0F, 6.0F, 10.0F)
                // Right rear fender / wheel cover: Z: +9 to +19, Y: 11 to 17, X: 12 to 14
                .uv(0, 0).cuboid(12.0F, 11.0F, 9.0F, 2.0F, 6.0F, 10.0F)
                // Rear spoiler wing: 26 wide, 2 tall, 4 long (Z: +18 to +22, Y: 6 to 8)
                .uv(64, 0).cuboid(-13.0F, 6.0F, 18.0F, 26.0F, 2.0F, 4.0F)
                // Spoiler left mount
                .uv(64, 0).cuboid(-9.0F, 8.0F, 19.0F, 2.0F, 3.0F, 2.0F)
                // Spoiler right mount
                .uv(64, 0).cuboid(7.0F, 8.0F, 19.0F, 2.0F, 3.0F, 2.0F);

        root.addChild("body", body, ModelTransform.NONE);

        // 2. Interior (Dashboard, Steering Wheel, Seats, Center Console)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Front dashboard: 22 wide, 5 tall, 3 deep (Z: -6 to -3, Y: 11 to 16)
                .uv(0, 64).cuboid(-11.0F, 11.0F, -6.0F, 22.0F, 5.0F, 3.0F)
                // Steering wheel column: Z: -3 to -1, X: -7 to -5, Y: 12 to 14
                .uv(0, 64).cuboid(-7.0F, 12.0F, -3.0F, 2.0F, 2.0F, 2.0F)
                // Steering wheel ring: 6 wide, 6 tall, 1 deep (X: -9 to -3, Y: 9 to 15, Z: -1)
                .uv(0, 64).cuboid(-9.0F, 9.0F, -1.0F, 6.0F, 6.0F, 1.0F)
                // Center console: 4 wide, 3 tall, 12 long (Z: -3 to +9, X: -2 to +2, Y: 14 to 17)
                .uv(64, 0).cuboid(-2.0F, 14.0F, -3.0F, 4.0F, 3.0F, 12.0F)

                // Driver Seat Cushion (Left side): 8 wide, 2 tall, 8 long (X: -11 to -3, Y: 14 to 16, Z: 0 to 8)
                .uv(64, 32).cuboid(-11.0F, 14.0F, 0.0F, 8.0F, 2.0F, 8.0F)
                // Driver Seat Backrest: 8 wide, 10 tall, 2 deep (X: -11 to -3, Y: 4 to 14, Z: 7 to 9)
                .uv(64, 32).cuboid(-11.0F, 4.0F, 7.0F, 8.0F, 10.0F, 2.0F)
                // Driver Headrest: 4 wide, 3 tall, 2 deep (X: -9 to -5, Y: 1 to 4, Z: 7 to 9)
                .uv(64, 32).cuboid(-9.0F, 1.0F, 7.0F, 4.0F, 3.0F, 2.0F)

                // Passenger Seat Cushion (Right side): 8 wide, 2 tall, 8 long (X: +3 to +11, Y: 14 to 16, Z: 0 to 8)
                .uv(64, 32).cuboid(3.0F, 14.0F, 0.0F, 8.0F, 2.0F, 8.0F)
                // Passenger Seat Backrest: 8 wide, 10 tall, 2 deep (X: +3 to +11, Y: 4 to 14, Z: 7 to 9)
                .uv(64, 32).cuboid(3.0F, 4.0F, 7.0F, 8.0F, 10.0F, 2.0F)
                // Passenger Headrest: 4 wide, 3 tall, 2 deep (X: +5 to +9, Y: 1 to 4, Z: 7 to 9)
                .uv(64, 32).cuboid(5.0F, 1.0F, 7.0F, 4.0F, 3.0F, 2.0F);

        root.addChild("interior", interior, ModelTransform.NONE);

        // 3. Cabin Canopy (Windshield Frame & Hardtop Roof with Open Windows)
        ModelPartBuilder canopy = ModelPartBuilder.create()
                // Left A-pillar (front left window strut): Y: 3 to 11, Z: -5 to -4, X: -12.5 to -11.5
                .uv(64, 0).cuboid(-12.5F, 3.0F, -5.0F, 1.0F, 8.0F, 1.0F)
                // Right A-pillar (front right window strut): Y: 3 to 11, Z: -5 to -4, X: 11.5 to 12.5
                .uv(64, 0).cuboid(11.5F, 3.0F, -5.0F, 1.0F, 8.0F, 1.0F)
                // Top windshield header frame: 24 wide, 1 tall, 1 deep (Y: 3 to 4, Z: -5 to -4, X: -12 to +12)
                .uv(64, 0).cuboid(-12.0F, 3.0F, -5.0F, 24.0F, 1.0F, 1.0F)
                // Transparent cutout windshield glass: 22 wide, 7 tall, 0.5 deep (Y: 4 to 11, Z: -4.5, X: -11 to +11)
                .uv(0, 96).cuboid(-11.0F, 4.0F, -4.5F, 22.0F, 7.0F, 1.0F)
                // Left C-pillar (rear left strut): Y: 3 to 11, Z: +8 to +9, X: -12.5 to -11.5
                .uv(64, 0).cuboid(-12.5F, 3.0F, 8.0F, 1.0F, 8.0F, 1.0F)
                // Right C-pillar (rear right strut): Y: 3 to 11, Z: +8 to +9, X: 11.5 to 12.5
                .uv(64, 0).cuboid(11.5F, 3.0F, 8.0F, 1.0F, 8.0F, 1.0F)
                // Hardtop roof: 24 wide, 1.5 tall, 14 long (Z: -5 to +9, Y: 1.5 to 3.0, X: -12 to +12)
                .uv(0, 0).cuboid(-12.0F, 1.5F, -5.0F, 24.0F, 2.0F, 14.0F);

        root.addChild("canopy", canopy, ModelTransform.NONE);

        // 4. Wheels (Tires & Alloy Rims) - Resting on ground at Y = 24
        // Front Left Wheel: X: -14 to -10, Y: 15 to 24, Z: -19 to -10
        root.addChild("wheel_fl", ModelPartBuilder.create()
                .uv(64, 64).cuboid(-3.5F, -4.5F, -4.5F, 3.5F, 9.0F, 9.0F),
                ModelTransform.pivot(-11.0F, 19.5F, -14.0F));

        // Front Right Wheel: X: 10 to 14, Y: 15 to 24, Z: -19 to -10
        root.addChild("wheel_fr", ModelPartBuilder.create()
                .uv(64, 64).cuboid(0.0F, -4.5F, -4.5F, 3.5F, 9.0F, 9.0F),
                ModelTransform.pivot(11.0F, 19.5F, -14.0F));

        // Rear Left Wheel: X: -14 to -10, Y: 15 to 24, Z: +9 to +19
        root.addChild("wheel_rl", ModelPartBuilder.create()
                .uv(64, 64).cuboid(-3.5F, -4.5F, -4.5F, 3.5F, 9.0F, 9.0F),
                ModelTransform.pivot(-11.0F, 19.5F, 14.0F));

        // Rear Right Wheel: X: 10 to 14, Y: 15 to 24, Z: +9 to +19
        root.addChild("wheel_rr", ModelPartBuilder.create()
                .uv(64, 64).cuboid(0.0F, -4.5F, -4.5F, 3.5F, 9.0F, 9.0F),
                ModelTransform.pivot(11.0F, 19.5F, 14.0F));

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
