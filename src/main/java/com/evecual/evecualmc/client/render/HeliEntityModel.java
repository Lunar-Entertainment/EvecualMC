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

        // 1. Fuselage & Main Airframe (Primary Paint, uv: 0, 0 in 256x256)
        ModelPartBuilder fuselage = ModelPartBuilder.create()
                // Cabin Floor: 32 wide, 3 tall, 48 long (X: -16 to 16, Y: 13 to 16, Z: -24 to 24)
                .uv(0, 0).cuboid(-16.0F, 13.0F, -24.0F, 32.0F, 3.0F, 48.0F)
                // Lower Belly / High-Volt Battery Bay (carbon): 26 wide, 4 tall, 38 long (Y: 16 to 20)
                .uv(128, 0).cuboid(-13.0F, 16.0F, -18.0F, 26.0F, 4.0F, 38.0F)
                // Left Lower Side Wall: X: -17 to -15, Y: 4 to 14, Z: -22 to 24
                .uv(0, 0).cuboid(-17.0F, 4.0F, -22.0F, 2.0F, 10.0F, 46.0F)
                // Right Lower Side Wall: X: 15 to 17, Y: 4 to 14, Z: -22 to 24
                .uv(0, 0).cuboid(15.0F, 4.0F, -22.0F, 2.0F, 10.0F, 46.0F)
                // Front Lower Nose Cone: X: -14 to 14, Y: 7 to 14, Z: -30 to -24
                .uv(0, 0).cuboid(-14.0F, 7.0F, -30.0F, 28.0F, 7.0F, 6.0F)
                // Nose Tip Aerodynamic Wedge (carbon): X: -11 to 11, Y: 9 to 14, Z: -36 to -30
                .uv(128, 0).cuboid(-11.0F, 9.0F, -36.0F, 22.0F, 5.0F, 6.0F)
                // Upper Roof & Engine Cowling: X: -13 to 13, Y: -4 to 2, Z: -14 to 22
                .uv(0, 0).cuboid(-13.0F, -4.0F, -14.0F, 26.0F, 6.0F, 36.0F)
                // Rear Bulkhead / Cargo Door: X: -15 to 15, Y: 0 to 14, Z: 22 to 25
                .uv(0, 0).cuboid(-15.0F, 0.0F, 22.0F, 30.0F, 14.0F, 3.0F)
                // Left Turbine Intake Pod (carbon)
                .uv(128, 0).cuboid(-16.0F, -3.0F, -8.0F, 3.0F, 5.0F, 16.0F)
                // Right Turbine Intake Pod (carbon)
                .uv(128, 0).cuboid(13.0F, -3.0F, -8.0F, 3.0F, 5.0F, 16.0F)
                // Left Turbine Exhaust Nozzle (carbon)
                .uv(128, 0).cuboid(-10.0F, -2.0F, 22.0F, 6.0F, 4.0F, 5.0F)
                // Right Turbine Exhaust Nozzle (carbon)
                .uv(128, 0).cuboid(4.0F, -2.0F, 22.0F, 6.0F, 4.0F, 5.0F);

        root.addChild("fuselage", fuselage, ModelTransform.NONE);

        // 2. Cockpit Interior & Ergonomic Pilot Seat (uv: 128, 0)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Avionics Dashboard Console
                .uv(128, 0).cuboid(-12.0F, 6.0F, -24.0F, 24.0F, 8.0F, 4.0F)
                // Dual Flight Control Sticks (Cyclics)
                .uv(128, 0).cuboid(-6.0F, 8.0F, -16.0F, 2.0F, 6.0F, 2.0F)
                .uv(128, 0).cuboid(4.0F, 8.0F, -16.0F, 2.0F, 6.0F, 2.0F)
                // Pilot Captain Seat (Center-Left)
                .uv(128, 0).cuboid(-9.0F, 10.0F, -10.0F, 8.0F, 3.0F, 12.0F)
                .uv(128, 0).cuboid(-9.0F, 0.0F, 2.0F, 8.0F, 11.0F, 2.0F)
                // Co-Pilot / Passenger Seat (Center-Right)
                .uv(128, 0).cuboid(1.0F, 10.0F, -10.0F, 8.0F, 3.0F, 12.0F)
                .uv(128, 0).cuboid(1.0F, 0.0F, 2.0F, 8.0F, 11.0F, 2.0F)
                // Rear Cargo Deck Floor Liner
                .uv(128, 0).cuboid(-13.0F, 12.5F, 5.0F, 26.0F, 1.0F, 16.0F);

        root.addChild("interior", interior, ModelTransform.NONE);

        // 3. Tail Boom, Tall Vertical Fin & Stabilizer Wings
        ModelPartBuilder tailBoom = ModelPartBuilder.create()
                // Tapered Tail Boom: X: -4 to 4, Y: 2 to 9, Z: 24 to 74
                .uv(0, 0).cuboid(-4.0F, 2.0F, 24.0F, 8.0F, 7.0F, 50.0F)
                // Vertical Tail Fin: X: -1.5 to 1.5, Y: -16 to 10, Z: 66 to 78
                .uv(0, 0).cuboid(-1.5F, -16.0F, 66.0F, 3.0F, 26.0F, 12.0F)
                // Left Horizontal Stabilizer Wing: X: -18 to -4, Y: 3.5 to 5.0, Z: 50 to 60
                .uv(128, 0).cuboid(-18.0F, 3.5F, 50.0F, 14.0F, 1.5F, 10.0F)
                // Right Horizontal Stabilizer Wing: X: 4 to 18, Y: 3.5 to 5.0, Z: 50 to 60
                .uv(128, 0).cuboid(4.0F, 3.5F, 50.0F, 14.0F, 1.5F, 10.0F)
                // Top Fin Navigation Strobe Light (LED, uv: 0, 128)
                .uv(0, 128).cuboid(-1.5F, -18.0F, 72.0F, 3.0F, 2.0F, 3.0F);

        root.addChild("tail_boom", tailBoom, ModelTransform.NONE);

        // 4. Heavy Tubular Landing Skids (Carbon / Titanium, uv: 128, 0)
        ModelPartBuilder skids = ModelPartBuilder.create()
                // Left Heavy Tubular Skid: X: -18 to -15, Y: 22 to 25, Z: -32 to 26
                .uv(128, 0).cuboid(-18.0F, 22.0F, -32.0F, 3.0F, 3.0F, 58.0F)
                // Left Curved Front Tip
                .uv(128, 0).cuboid(-18.0F, 18.0F, -36.0F, 3.0F, 4.0F, 4.0F)
                // Right Heavy Tubular Skid: X: 15 to 18, Y: 22 to 25, Z: -32 to 26
                .uv(128, 0).cuboid(15.0F, 22.0F, -32.0F, 3.0F, 3.0F, 58.0F)
                // Right Curved Front Tip
                .uv(128, 0).cuboid(15.0F, 18.0F, -36.0F, 3.0F, 4.0F, 4.0F)
                // Front Left Strut
                .uv(128, 0).cuboid(-17.0F, 16.0F, -14.0F, 2.0F, 6.0F, 3.0F)
                // Front Right Strut
                .uv(128, 0).cuboid(15.0F, 16.0F, -14.0F, 2.0F, 6.0F, 3.0F)
                // Rear Left Strut
                .uv(128, 0).cuboid(-17.0F, 16.0F, 14.0F, 2.0F, 6.0F, 3.0F)
                // Rear Right Strut
                .uv(128, 0).cuboid(15.0F, 16.0F, 14.0F, 2.0F, 6.0F, 3.0F)
                // Heavy Charging Induction Contact Plates (Gold, uv: 0, 128)
                .uv(0, 128).cuboid(-18.5F, 24.5F, -6.0F, 4.0F, 1.0F, 12.0F)
                .uv(0, 128).cuboid(14.5F, 24.5F, -6.0F, 4.0F, 1.0F, 12.0F);

        root.addChild("skids", skids, ModelTransform.NONE);

        // 5. Giant Main Rotor Assembly (7.5 Block Blade Span!)
        ModelPartBuilder mainRotor = ModelPartBuilder.create()
                // Rotor Mast Column
                .uv(128, 0).cuboid(-3.0F, -9.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                // Central Rotor Swashplate Hub
                .uv(128, 0).cuboid(-6.0F, -11.0F, -6.0F, 12.0F, 3.0F, 12.0F)
                // Blade 1 (Forward / North): 5 wide, 1 tall, 54 long
                .uv(128, 0).cuboid(-2.5F, -10.5F, -60.0F, 5.0F, 1.0F, 54.0F)
                // Blade 2 (Aft / South): 5 wide, 1 tall, 54 long
                .uv(128, 0).cuboid(-2.5F, -10.5F, 6.0F, 5.0F, 1.0F, 54.0F)
                // Blade 3 (Left / West): 54 wide, 1 tall, 5 long
                .uv(128, 0).cuboid(-60.0F, -10.5F, -2.5F, 54.0F, 1.0F, 5.0F)
                // Blade 4 (Right / East): 54 wide, 1 tall, 5 long
                .uv(128, 0).cuboid(6.0F, -10.5F, -2.5F, 54.0F, 1.0F, 5.0F);

        root.addChild("main_rotor", mainRotor, ModelTransform.pivot(0.0F, -3.0F, 2.0F));

        // 6. Tail Anti-Torque Rotor Assembly
        ModelPartBuilder tailRotor = ModelPartBuilder.create()
                // Tail Rotor Hub
                .uv(128, 0).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)
                // Blade 1 (Upper)
                .uv(128, 0).cuboid(-1.0F, -14.0F, -1.5F, 2.0F, 13.0F, 3.0F)
                // Blade 2 (Lower)
                .uv(128, 0).cuboid(-1.0F, 1.5F, -1.5F, 2.0F, 13.0F, 3.0F);

        root.addChild("tail_rotor", tailRotor, ModelTransform.pivot(2.5F, -4.0F, 74.0F));

        // 7. Glass Cockpit Bubble Canopy (Translucent Tinted Glass, uv: 128, 128)
        ModelPartBuilder glass = ModelPartBuilder.create()
                // Front Panoramic Windshield: X: -14 to 14, Y: -2 to 10, Z: -28 to -14
                .uv(128, 128).cuboid(-14.0F, -2.0F, -28.0F, 28.0F, 11.0F, 14.0F)
                // Lower Sloped Nose Glass: X: -13 to 13, Y: 6 to 11, Z: -31 to -27
                .uv(128, 128).cuboid(-13.0F, 6.0F, -31.0F, 26.0F, 5.0F, 4.0F)
                // Left Cockpit Side Windows: X: -16.5 to -15.5, Y: -1 to 8, Z: -20 to 12
                .uv(128, 128).cuboid(-16.5F, -1.0F, -20.0F, 1.0F, 9.0F, 32.0F)
                // Right Cockpit Side Windows: X: 15.5 to 16.5, Y: -1 to 8, Z: -20 to 12
                .uv(128, 128).cuboid(15.5F, -1.0F, -20.0F, 1.0F, 9.0F, 32.0F)
                // Skylight Overhead Glass Roof
                .uv(128, 128).cuboid(-11.0F, -4.2F, -20.0F, 22.0F, 1.0F, 12.0F);

        root.addChild("glass", glass, ModelTransform.NONE);

        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public void setAngles(HeliEntity heli, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Main Rotor high-speed spin
        this.main_rotor.yaw = (float) Math.toRadians(heli.getRotorAngle());

        // Tail Anti-Torque Rotor spin
        this.tail_rotor.pitch = (float) Math.toRadians(heli.getTailRotorAngle());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
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
        if (rotorSpeed < 0.25F) return;
        matrices.push();
        matrices.translate(0.0, 0.05, 0.0);
        float alpha = Math.min(0.45F, rotorSpeed * 0.35F);

        for (int i = 1; i <= 3; i++) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 15.0F));
            this.main_rotor.render(matrices, vertices, light, overlay, 0.9F, 0.95F, 1.0F, alpha * (1.0F - i * 0.25F));
        }
        matrices.pop();
    }
}
