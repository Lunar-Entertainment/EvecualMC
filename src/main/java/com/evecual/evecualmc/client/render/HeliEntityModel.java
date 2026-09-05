package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.HeliEntity;
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
import net.minecraft.util.math.RotationAxis;

public class HeliEntityModel extends EntityModel<HeliEntity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier("evecualmc", "ev_heli"), "main");

    private final ModelPart root;
    private final ModelPart main_rotor;
    private final ModelPart tail_rotor;
    private final ModelPart glass;

    public HeliEntityModel(ModelPart root) {
        this.root = root;
        this.main_rotor = root.getChild("main_rotor");
        this.tail_rotor = root.getChild("tail_rotor");
        this.glass = root.getChild("glass");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // 1. Fuselage & Cabin Body - Primary Paint (uv: 0, 0 in 256x256)
        ModelPartBuilder fuselage = ModelPartBuilder.create()
                // Cabin Floor / Base: 24 wide, 2 tall, 36 long (X: -12 to 12, Y: 14 to 16, Z: -18 to 18)
                .uv(0, 0).cuboid(-12.0F, 14.0F, -18.0F, 24.0F, 2.0F, 36.0F)
                // Lower Belly / Battery Bay (carbon): 20 wide, 3 tall, 28 long (Y: 16 to 19)
                .uv(128, 0).cuboid(-10.0F, 16.0F, -14.0F, 20.0F, 3.0F, 28.0F)
                // Left Lower Side Wall: X: -13 to -11, Y: 8 to 14, Z: -16 to 18
                .uv(0, 0).cuboid(-13.0F, 8.0F, -16.0F, 2.0F, 6.0F, 34.0F)
                // Right Lower Side Wall: X: 11 to 13, Y: 8 to 14, Z: -16 to 18
                .uv(0, 0).cuboid(11.0F, 8.0F, -16.0F, 2.0F, 6.0F, 34.0F)
                // Front Nose Lower Bumper: X: -10 to 10, Y: 10 to 15, Z: -22 to -18
                .uv(0, 0).cuboid(-10.0F, 10.0F, -22.0F, 20.0F, 5.0F, 4.0F)
                // Nose Tip Aerodynamic Wedge (carbon): X: -8 to 8, Y: 12 to 15, Z: -25 to -22
                .uv(128, 0).cuboid(-8.0F, 12.0F, -25.0F, 16.0F, 3.0F, 3.0F)
                // Roof & Engine Cowling: X: -10 to 10, Y: 2 to 6, Z: -10 to 16
                .uv(0, 0).cuboid(-10.0F, 2.0F, -10.0F, 20.0F, 4.0F, 26.0F)
                // Rear Bulkhead / Cargo Door: X: -11 to 11, Y: 4 to 14, Z: 16 to 18
                .uv(0, 0).cuboid(-11.0F, 4.0F, 16.0F, 22.0F, 10.0F, 2.0F)
                // Turbine Engine Intake Pods (Left & Right)
                .uv(128, 0).cuboid(-12.0F, 2.0F, -6.0F, 2.0F, 3.0F, 10.0F)
                .uv(128, 0).cuboid(10.0F, 2.0F, -6.0F, 2.0F, 3.0F, 10.0F)
                // Turbine Exhaust Nozzles (Rear)
                .uv(128, 0).cuboid(-7.0F, 3.0F, 16.0F, 4.0F, 3.0F, 3.0F)
                .uv(128, 0).cuboid(3.0F, 3.0F, 16.0F, 4.0F, 3.0F, 3.0F);

        root.addChild("fuselage", fuselage, ModelTransform.NONE);

        // 2. Interior Cockpit - Pilot Seat & Avionics Dash (uv: 128, 0)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Avionics Dashboard Panel
                .uv(128, 0).cuboid(-8.0F, 8.0F, -17.0F, 16.0F, 6.0F, 3.0F)
                // Center Flight Control Stick / Cyclic
                .uv(128, 0).cuboid(-1.0F, 10.0F, -11.0F, 2.0F, 5.0F, 2.0F)
                .uv(128, 0).cuboid(-2.0F, 9.0F, -11.0F, 4.0F, 1.0F, 2.0F)
                // Pilot Seat Cushion & Ergonomic Backrest
                .uv(128, 0).cuboid(-6.0F, 12.0F, -6.0F, 12.0F, 2.0F, 10.0F)
                .uv(128, 0).cuboid(-6.0F, 4.0F, 3.0F, 12.0F, 9.0F, 2.0F)
                // Cargo Compartment Floor Liner
                .uv(128, 0).cuboid(-9.0F, 13.5F, 5.0F, 18.0F, 1.0F, 10.0F);

        root.addChild("interior", interior, ModelTransform.NONE);

        // 3. Tail Boom, Fin & Horizontal Stabilizer
        ModelPartBuilder tailBoom = ModelPartBuilder.create()
                // Tapered Tail Boom: X: -3 to 3, Y: 4 to 8, Z: 18 to 52
                .uv(0, 0).cuboid(-3.0F, 4.0F, 18.0F, 6.0F, 4.0F, 34.0F)
                // Vertical Tail Fin: X: -1 to 1, Y: -8 to 8, Z: 46 to 54
                .uv(0, 0).cuboid(-1.0F, -8.0F, 46.0F, 2.0F, 16.0F, 8.0F)
                // Horizontal Stabilizer (Left Wing): X: -12 to -3, Y: 5.0 to 6.0, Z: 36 to 42
                .uv(128, 0).cuboid(-12.0F, 5.0F, 36.0F, 9.0F, 1.0F, 6.0F)
                // Horizontal Stabilizer (Right Wing): X: 3 to 12, Y: 5.0 to 6.0, Z: 36 to 42
                .uv(128, 0).cuboid(3.0F, 5.0F, 36.0F, 9.0F, 1.0F, 6.0F)
                // Tail Beacon Light (LED)
                .uv(0, 128).cuboid(-1.0F, -9.0F, 50.0F, 2.0F, 1.0F, 2.0F);

        root.addChild("tail_boom", tailBoom, ModelTransform.NONE);

        // 4. Landing Skids (Carbon / Titanium, uv: 128, 0)
        ModelPartBuilder skids = ModelPartBuilder.create()
                // Left Tubular Skid: X: -13.5 to -11.5, Y: 21 to 23, Z: -22 to 20
                .uv(128, 0).cuboid(-13.5F, 21.0F, -22.0F, 2.0F, 2.0F, 42.0F)
                // Left Skid Front Curved Tip
                .uv(128, 0).cuboid(-13.5F, 19.0F, -25.0F, 2.0F, 3.0F, 3.0F)
                // Right Tubular Skid: X: 11.5 to 13.5, Y: 21 to 23, Z: -22 to 20
                .uv(128, 0).cuboid(11.5F, 21.0F, -22.0F, 2.0F, 2.0F, 42.0F)
                // Right Skid Front Curved Tip
                .uv(128, 0).cuboid(11.5F, 19.0F, -25.0F, 2.0F, 3.0F, 3.0F)
                // Front Left Strut (Angled connection): Y: 16 to 22, X: -12 to -9
                .uv(128, 0).cuboid(-12.5F, 16.0F, -10.0F, 1.5F, 5.0F, 2.0F)
                // Front Right Strut
                .uv(128, 0).cuboid(11.0F, 16.0F, -10.0F, 1.5F, 5.0F, 2.0F)
                // Rear Left Strut
                .uv(128, 0).cuboid(-12.5F, 16.0F, 10.0F, 1.5F, 5.0F, 2.0F)
                // Rear Right Strut
                .uv(128, 0).cuboid(11.0F, 16.0F, 10.0F, 1.5F, 5.0F, 2.0F)
                // Charging Contact Plates (Gold, uv: 0, 128)
                .uv(0, 128).cuboid(-14.0F, 22.5F, -4.0F, 3.0F, 1.0F, 8.0F)
                .uv(0, 128).cuboid(11.0F, 22.5F, -4.0F, 3.0F, 1.0F, 8.0F);

        root.addChild("skids", skids, ModelTransform.NONE);

        // 5. Main Rotor Assembly - Rotates smoothly around Y (0, 1, 0)
        ModelPartBuilder mainRotor = ModelPartBuilder.create()
                // Rotor Mast Column
                .uv(128, 0).cuboid(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                // Rotor Central Hub
                .uv(128, 0).cuboid(-4.0F, -5.5F, -4.0F, 8.0F, 2.0F, 8.0F)
                // Blade 1 (North / Forward): 4 wide, 0.6 tall, 38 long
                .uv(128, 0).cuboid(-2.0F, -5.0F, -42.0F, 4.0F, 1.0F, 38.0F)
                // Blade 2 (South / Aft): 4 wide, 0.6 tall, 38 long
                .uv(128, 0).cuboid(-2.0F, -5.0F, 4.0F, 4.0F, 1.0F, 38.0F)
                // Blade 3 (West / Left): 38 wide, 0.6 tall, 4 long
                .uv(128, 0).cuboid(-42.0F, -5.0F, -2.0F, 38.0F, 1.0F, 4.0F)
                // Blade 4 (East / Right): 38 wide, 0.6 tall, 4 long
                .uv(128, 0).cuboid(4.0F, -5.0F, -2.0F, 38.0F, 1.0F, 4.0F);

        root.addChild("main_rotor", mainRotor, ModelTransform.pivot(0.0F, 1.0F, 0.0F));

        // 6. Tail Anti-Torque Rotor - Rotates vertically on side of tail fin
        ModelPartBuilder tailRotor = ModelPartBuilder.create()
                // Tail Rotor Hub
                .uv(128, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                // Blade 1 (Vertical Upper)
                .uv(128, 0).cuboid(-0.5F, -9.0F, -1.0F, 1.0F, 8.0F, 2.0F)
                // Blade 2 (Vertical Lower)
                .uv(128, 0).cuboid(-0.5F, 1.0F, -1.0F, 1.0F, 8.0F, 2.0F);

        root.addChild("tail_rotor", tailRotor, ModelTransform.pivot(1.8F, -2.0F, 50.0F));

        // 7. Glass Canopy (Translucent Tinted Bubble Canopy, uv: 128, 128)
        ModelPartBuilder glass = ModelPartBuilder.create()
                // Front Curved Windshield: X: -10 to 10, Y: 4 to 12, Z: -20 to -10
                .uv(128, 128).cuboid(-10.0F, 4.0F, -20.0F, 20.0F, 7.0F, 10.0F)
                // Front Lower Sloped Nose Glass
                .uv(128, 128).cuboid(-9.0F, 8.0F, -22.0F, 18.0F, 4.0F, 3.0F)
                // Left Cockpit Window: X: -12.5 to -11.5, Y: 4 to 10, Z: -14 to 8
                .uv(128, 128).cuboid(-12.5F, 4.0F, -14.0F, 1.0F, 6.0F, 22.0F)
                // Right Cockpit Window: X: 11.5 to 12.5, Y: 4 to 10, Z: -14 to 8
                .uv(128, 128).cuboid(11.5F, 4.0F, -14.0F, 1.0F, 6.0F, 22.0F)
                // Skylight / Overhead Canopy Window
                .uv(128, 128).cuboid(-8.0F, 1.8F, -14.0F, 16.0F, 1.0F, 8.0F);

        root.addChild("glass", glass, ModelTransform.NONE);

        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public void setAngles(HeliEntity heli, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Main Rotor high-speed smooth spin
        this.main_rotor.yaw = (float) Math.toRadians(heli.getRotorAngle());

        // Tail Anti-Torque Rotor spin on X axis
        this.tail_rotor.pitch = (float) Math.toRadians(heli.getTailRotorAngle());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        // Render solid fuselage, interior, skids, tail boom, and rotors
        this.root.getChild("fuselage").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.root.getChild("interior").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.root.getChild("tail_boom").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.root.getChild("skids").render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.main_rotor.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.tail_rotor.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    public void renderGlass(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g, float b, float a) {
        this.glass.render(matrices, vertices, light, overlay, r, g, b, a);
    }

    public void renderRotorMotionBlur(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float rotorSpeed) {
        if (rotorSpeed < 0.3F) return;
        matrices.push();
        matrices.translate(0.0, 0.05, 0.0);
        float alpha = Math.min(0.45F, rotorSpeed * 0.35F);

        // Ghost blur disc
        for (int i = 1; i <= 3; i++) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 18.0F));
            this.main_rotor.render(matrices, vertices, light, overlay, 0.9F, 0.95F, 1.0F, alpha * (1.0F - i * 0.25F));
        }
        matrices.pop();
    }
}
