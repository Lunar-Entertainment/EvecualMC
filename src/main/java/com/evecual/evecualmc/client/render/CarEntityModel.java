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
    private final ModelPart wheel_fl;
    private final ModelPart wheel_fr;
    private final ModelPart wheel_rl;
    private final ModelPart wheel_rr;
    private final ModelPart glass;

    public CarEntityModel(ModelPart root) {
        this.root = root;
        this.wheel_fl = root.getChild("wheel_fl");
        this.wheel_fr = root.getChild("wheel_fr");
        this.wheel_rl = root.getChild("wheel_rl");
        this.wheel_rr = root.getChild("wheel_rr");
        this.glass = root.getChild("glass");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // 1. Chassis & Body - Primary Car Paint (uv: 0, 0 in 256x256)
        ModelPartBuilder body = ModelPartBuilder.create()
                // Main floor plate: 28 wide, 2 thick, 48 long (Z: -24 to +24, Y: 16 to 18)
                .uv(0, 0).cuboid(-14.0F, 16.0F, -24.0F, 28.0F, 2.0F, 48.0F)
                // Left side skirt (carbon, uv: 128, 0): X: -15.5 to -13.5, Y: 15 to 19, Z: -10 to +10
                .uv(128, 0).cuboid(-15.5F, 15.0F, -10.0F, 2.0F, 4.0F, 20.0F)
                // Right side skirt (carbon, uv: 128, 0): X: +13.5 to +15.5, Y: 15 to 19, Z: -10 to +10
                .uv(128, 0).cuboid(13.5F, 15.0F, -10.0F, 2.0F, 4.0F, 20.0F)
                // Front bumper (body paint): 30 wide, 4 tall, 4 long (Z: -26 to -22, Y: 14 to 18)
                .uv(0, 0).cuboid(-15.0F, 14.0F, -26.0F, 30.0F, 4.0F, 4.0F)
                // Front carbon splitter lip (carbon, uv: 128, 0): 32 wide, 1 tall, 3 long (Z: -28 to -25, Y: 17 to 18)
                .uv(128, 0).cuboid(-16.0F, 17.0F, -28.0F, 32.0F, 1.0F, 3.0F)
                // Front hood (body paint): 28 wide, 6 tall, 14 long (Z: -22 to -8, Y: 10 to 16)
                .uv(0, 0).cuboid(-14.0F, 10.0F, -22.0F, 28.0F, 6.0F, 14.0F)
                // Left front fender: X: -16 to -14, Y: 10 to 17, Z: -21 to -11
                .uv(0, 0).cuboid(-16.0F, 10.0F, -21.0F, 2.0F, 7.0F, 10.0F)
                // Right front fender: X: +14 to +16, Y: 10 to 17, Z: -21 to -11
                .uv(0, 0).cuboid(14.0F, 10.0F, -21.0F, 2.0F, 7.0F, 10.0F)
                // Left door panel: X: -16 to -13, Y: 8 to 16, Z: -8 to +10
                .uv(0, 0).cuboid(-15.5F, 8.0F, -8.0F, 2.5F, 8.0F, 18.0F)
                // Right door panel: X: +13 to +16, Y: 8 to 16, Z: -8 to +10
                .uv(0, 0).cuboid(13.0F, 8.0F, -8.0F, 2.5F, 8.0F, 18.0F)
                // Left side view mirror (carbon): X: -17.5 to -15.5, Y: 7 to 9, Z: -7 to -5
                .uv(128, 0).cuboid(-18.0F, 7.0F, -7.0F, 2.5F, 2.0F, 2.0F)
                // Right side view mirror (carbon): X: +15.5 to +17.5, Y: 7 to 9, Z: -7 to -5
                .uv(128, 0).cuboid(15.5F, 7.0F, -7.0F, 2.5F, 2.0F, 2.0F)
                // Rear trunk deck (body paint): 28 wide, 6 tall, 12 long (Z: +10 to +22, Y: 10 to 16)
                .uv(0, 0).cuboid(-14.0F, 10.0F, 10.0F, 28.0F, 6.0F, 12.0F)
                // Rear bumper (body paint): 30 wide, 4 tall, 3 long (Z: +22 to +25, Y: 14 to 18)
                .uv(0, 0).cuboid(-15.0F, 14.0F, 22.0F, 30.0F, 4.0F, 3.0F)
                // Rear carbon diffuser fins (carbon, uv: 128, 0): 28 wide, 2 tall, 2 deep (Z: +24 to +26, Y: 17 to 19)
                .uv(128, 0).cuboid(-14.0F, 17.0F, 24.0F, 28.0F, 2.0F, 2.0F)
                // Left rear fender: X: -16 to -14, Y: 10 to 17, Z: +10 to +21
                .uv(0, 0).cuboid(-16.0F, 10.0F, 10.0F, 2.0F, 7.0F, 11.0F)
                // Right rear fender: X: +14 to +16, Y: 10 to 17, Z: +10 to +21
                .uv(0, 0).cuboid(14.0F, 10.0F, 10.0F, 2.0F, 7.0F, 11.0F)
                // Rear carbon spoiler wing (carbon, uv: 128, 0): 30 wide, 2 tall, 5 long (Z: +20 to +25, Y: 4 to 6)
                .uv(128, 0).cuboid(-15.0F, 4.0F, 20.0F, 30.0F, 2.0F, 5.0F)
                // Spoiler left mount (carbon)
                .uv(128, 0).cuboid(-10.0F, 6.0F, 21.0F, 2.0F, 4.0F, 2.0F)
                // Spoiler right mount (carbon)
                .uv(128, 0).cuboid(8.0F, 6.0F, 21.0F, 2.0F, 4.0F, 2.0F);

        root.addChild("body", body, ModelTransform.NONE);

        // 2. Interior - Carbon / Dark Trim (uv: 128, 0)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Front dashboard: 26 wide, 6 tall, 4 deep (Z: -8 to -4, Y: 9 to 15)
                .uv(128, 0).cuboid(-13.0F, 9.0F, -8.0F, 26.0F, 6.0F, 4.0F)
                // Center Steering wheel column: Z: -4 to -2, X: -1 to +1, Y: 10 to 12
                .uv(128, 0).cuboid(-1.0F, 10.0F, -4.0F, 2.0F, 2.0F, 2.0F)
                // Center Steering wheel: 8 wide, 8 tall, 1 deep (X: -4 to +4, Y: 7 to 15, Z: -2)
                .uv(128, 0).cuboid(-4.0F, 7.0F, -2.0F, 8.0F, 8.0F, 1.0F)
                // Low-profile ergonomic driver seat cushion and backrest
                .uv(128, 0).cuboid(-7.0F, 14.0F, -1.0F, 14.0F, 2.0F, 10.0F)
                .uv(128, 0).cuboid(-7.0F, 7.0F, 8.0F, 14.0F, 7.0F, 2.0F);

        root.addChild("interior", interior, ModelTransform.NONE);

        // 3. Canopy Pillars & Roof Rails (Clean Body Paint, uv: 0, 0)
        ModelPartBuilder canopyFrame = ModelPartBuilder.create()
                // Left A-Pillar
                .uv(0, 0).cuboid(-14.0F, -5.0F, -8.0F, 1.0F, 13.0F, 1.5F)
                // Right A-Pillar
                .uv(0, 0).cuboid(13.0F, -5.0F, -8.0F, 1.0F, 13.0F, 1.5F)
                // Left Roof Rail
                .uv(0, 0).cuboid(-14.0F, -6.0F, -8.0F, 1.0F, 1.0F, 18.0F)
                // Right Roof Rail
                .uv(0, 0).cuboid(13.0F, -6.0F, -8.0F, 1.0F, 1.0F, 18.0F);

        root.addChild("canopy_frame", canopyFrame, ModelTransform.NONE);

        // 4. Transparent Glass Canopy (Windshield & Panoramic Roof, uv: 128, 128)
        ModelPartBuilder glassCanopy = ModelPartBuilder.create()
                // 1) Front Windshield Glass
                .uv(128, 128).cuboid(-13.0F, -5.0F, -7.5F, 26.0F, 13.0F, 1.0F)
                // 2) Seamless Full Glass Panoramic Roof
                .uv(128, 128).cuboid(-13.0F, -6.0F, -7.0F, 26.0F, 1.0F, 17.0F);

        root.addChild("glass", glassCanopy, ModelTransform.NONE);

        // 5. Wheels (Symmetrical Alloy Rims, uv: 0, 128)
        // Cuboids centered on axle: (-2 to +2 in X, -5 to +5 in Y, -5 to +5 in Z)
        // Left wheels face -X (outwards)
        root.addChild("wheel_fl", ModelPartBuilder.create()
                .uv(0, 128).cuboid(-2.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.pivot(-12.5F, 19.0F, -16.0F));

        root.addChild("wheel_rl", ModelPartBuilder.create()
                .uv(0, 128).cuboid(-2.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.pivot(-12.5F, 19.0F, 16.0F));

        // Right wheels rotated 180° around Y so outer rim face (-X) also points outwards (+X)
        root.addChild("wheel_fr", ModelPartBuilder.create()
                .uv(0, 128).cuboid(-2.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.of(12.5F, 19.0F, -16.0F, 0.0F, (float) Math.PI, 0.0F));

        root.addChild("wheel_rr", ModelPartBuilder.create()
                .uv(0, 128).cuboid(-2.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F),
                ModelTransform.of(12.5F, 19.0F, 16.0F, 0.0F, (float) Math.PI, 0.0F));

        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public void setAngles(CarEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float steer = entity.getSteeringAngle(); // Steers up to 45 deg (0.785 rad)
        float roll = entity.getWheelRoll();      // Smooth axle rolling animation

        // Front Left Wheel
        this.wheel_fl.yaw = steer;
        this.wheel_fl.pitch = roll;

        // Front Right Wheel (180° base Y flip)
        this.wheel_fr.yaw = (float) Math.PI + steer;
        this.wheel_fr.pitch = -roll;

        // Rear Left Wheel
        this.wheel_rl.yaw = 0.0F;
        this.wheel_rl.pitch = roll;

        // Rear Right Wheel (180° base Y flip)
        this.wheel_rr.yaw = (float) Math.PI;
        this.wheel_rr.pitch = -roll;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        root.getChild("body").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        root.getChild("interior").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        root.getChild("canopy_frame").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        wheel_fl.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        wheel_fr.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        wheel_rl.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        wheel_rr.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    public void renderWheelMotionBlur(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float speed) {
        if (Math.abs(speed) > 0.08F) {
            float dir = speed > 0 ? -1.0F : 1.0F;
            float[] offsets = { dir * 0.15F, dir * 0.32F, dir * 0.50F };
            float[] alphas = { 0.40F, 0.22F, 0.10F };

            for (int i = 0; i < offsets.length; i++) {
                float off = offsets[i];
                float a = alphas[i];

                matrices.push();
                this.wheel_fl.pitch += off;
                this.wheel_fr.pitch -= off;
                this.wheel_rl.pitch += off;
                this.wheel_rr.pitch -= off;

                this.wheel_fl.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);
                this.wheel_fr.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);
                this.wheel_rl.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);
                this.wheel_rr.render(matrices, vertices, light, overlay, 0.3F, 0.3F, 0.3F, a);

                this.wheel_fl.pitch -= off;
                this.wheel_fr.pitch += off;
                this.wheel_rl.pitch -= off;
                this.wheel_rr.pitch += off;
                matrices.pop();
            }
        }
    }

    public void renderGlass(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        glass.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
