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

        // 1. Aerodynamic Fuselage & Airframe (Primary Body Paint, uv: 0, 0 in 256x256)
        ModelPartBuilder fuselage = ModelPartBuilder.create()
                // Cabin Floor: 28 wide, 2 tall, 38 long (X: -14 to 14, Y: 13 to 15, Z: -16 to 22)
                .uv(0, 0).cuboid(-14.0F, 13.0F, -16.0F, 28.0F, 2.0F, 38.0F)
                // Lower Battery Belly (Carbon, uv: 128, 0): 24 wide, 3 tall, 34 long (Y: 15 to 18)
                .uv(128, 0).cuboid(-12.0F, 15.0F, -15.0F, 24.0F, 3.0F, 34.0F)
                // Front Lower Aerodynamic Chin Wedge: X: -12 to 12, Y: 11 to 14, Z: -26 to -16
                .uv(0, 0).cuboid(-12.0F, 11.0F, -26.0F, 24.0F, 3.0F, 10.0F)
                // Aerodynamic Nose Tip (Carbon): X: -8 to 8, Y: 10 to 13, Z: -31 to -26
                .uv(128, 0).cuboid(-8.0F, 10.0F, -31.0F, 16.0F, 3.0F, 5.0F)
                // Left Lower Side Sill: X: -15.5 to -13.5, Y: 8 to 14, Z: -15 to 22
                .uv(0, 0).cuboid(-15.5F, 8.0F, -15.0F, 2.0F, 6.0F, 37.0F)
                // Right Lower Side Sill: X: 13.5 to 15.5, Y: 8 to 14, Z: -15 to 22
                .uv(0, 0).cuboid(13.5F, 8.0F, -15.0F, 2.0F, 6.0F, 37.0F)
                // Main Upper Roof Cowling: X: -12 to 12, Y: -6 to -3, Z: -10 to 22
                .uv(0, 0).cuboid(-12.0F, -6.0F, -10.0F, 24.0F, 3.0F, 32.0F)
                // Front Roof Taper Fairing: X: -10 to 10, Y: -5 to -3, Z: -16 to -10
                .uv(0, 0).cuboid(-10.0F, -5.0F, -16.0F, 20.0F, 2.0F, 6.0F)
                // Rear Bulkhead Wall: X: -14 to 14, Y: -3 to 13, Z: 22 to 24
                .uv(0, 0).cuboid(-14.0F, -3.0F, 22.0F, 28.0F, 16.0F, 2.0F)
                // Left A-Pillar (Frame): X: -14 to -12.5, Y: -5 to 11, Z: -17 to -15.5
                .uv(0, 0).cuboid(-14.0F, -5.0F, -17.0F, 1.5F, 16.0F, 1.5F)
                // Right A-Pillar (Frame): X: 12.5 to 14, Y: -5 to 11, Z: -17 to -15.5
                .uv(0, 0).cuboid(12.5F, -5.0F, -17.0F, 1.5F, 16.0F, 1.5F)
                // Left Turbine Pod (Carbon): X: -15 to -11, Y: -7 to -3, Z: -4 to 18
                .uv(128, 0).cuboid(-15.0F, -7.0F, -4.0F, 4.0F, 4.0F, 22.0F)
                // Right Turbine Pod (Carbon): X: 11 to 15, Y: -7 to -3, Z: -4 to 18
                .uv(128, 0).cuboid(11.0F, -7.0F, -4.0F, 4.0F, 4.0F, 22.0F)
                // Left Turbine Air Scoop (Carbon)
                .uv(128, 0).cuboid(-14.5F, -6.5F, -7.0F, 3.0F, 3.0F, 3.0F)
                // Right Turbine Air Scoop (Carbon)
                .uv(128, 0).cuboid(11.5F, -6.5F, -7.0F, 3.0F, 3.0F, 3.0F)
                // Left Titanium Exhaust Nozzle (Carbon)
                .uv(128, 0).cuboid(-14.0F, -6.0F, 18.0F, 3.0F, 3.0F, 5.0F)
                // Right Titanium Exhaust Nozzle (Carbon)
                .uv(128, 0).cuboid(11.0F, -6.0F, 18.0F, 3.0F, 3.0F, 5.0F);

        root.addChild("fuselage", fuselage, ModelTransform.NONE);

        // 2. Cockpit Interior (Low-profile Console & Ergonomic Seats, uv: 128, 0)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Low-profile Avionics Instrument Dashboard (Unobstructed forward horizon!)
                .uv(128, 0).cuboid(-11.0F, 7.0F, -17.0F, 22.0F, 4.0F, 3.0F)
                // Center Flight Display Screen
                .uv(128, 0).cuboid(-4.0F, 4.5F, -16.5F, 8.0F, 3.0F, 1.0F)
                // Left Pilot Cyclic Stick
                .uv(128, 0).cuboid(-6.0F, 7.0F, -10.0F, 1.5F, 5.0F, 1.5F)
                // Right Co-Pilot Cyclic Stick
                .uv(128, 0).cuboid(4.5F, 7.0F, -10.0F, 1.5F, 5.0F, 1.5F)
                // Left Captain Pilot Seat
                .uv(128, 0).cuboid(-10.0F, 11.0F, -6.0F, 8.0F, 2.0F, 9.0F)
                .uv(128, 0).cuboid(-10.0F, 2.0F, 3.0F, 8.0F, 9.0F, 2.0F)
                // Right Co-Pilot Seat
                .uv(128, 0).cuboid(2.0F, 11.0F, -6.0F, 8.0F, 2.0F, 9.0F)
                .uv(128, 0).cuboid(2.0F, 2.0F, 3.0F, 8.0F, 9.0F, 2.0F)
                // Rear Cargo Deck Floor
                .uv(128, 0).cuboid(-13.0F, 12.0F, 6.0F, 26.0F, 1.0F, 15.0F);

        root.addChild("interior", interior, ModelTransform.NONE);

        // 3. Tail Boom, Tail Fin & Horizontal Stabilizer Wings
        ModelPartBuilder tailBoom = ModelPartBuilder.create()
                // Tapered Forward Boom (Body Paint): X: -4.5 to 4.5, Y: -1 to 7, Z: 24 to 48
                .uv(0, 0).cuboid(-4.5F, -1.0F, 24.0F, 9.0F, 8.0F, 24.0F)
                // Tapered Aft Boom: X: -3 to 3, Y: 0 to 6, Z: 48 to 76
                .uv(0, 0).cuboid(-3.0F, 0.0F, 48.0F, 6.0F, 6.0F, 28.0F)
                // Tall Swept Vertical Tail Fin: X: -1.5 to 1.5, Y: -20 to 6, Z: 66 to 78
                .uv(0, 0).cuboid(-1.5F, -20.0F, 66.0F, 3.0F, 26.0F, 12.0F)
                // Tail Strobe Navigation Light (LED, uv: 0, 128)
                .uv(0, 128).cuboid(-1.5F, -22.0F, 72.0F, 3.0F, 2.0F, 3.0F)
                // Left Horizontal Stabilizer Wing (Carbon): X: -18 to -3, Y: 1.5 to 3.0, Z: 52 to 62
                .uv(128, 0).cuboid(-18.0F, 1.5F, 52.0F, 15.0F, 1.5F, 10.0F)
                // Left Winglet Endplate (Carbon)
                .uv(128, 0).cuboid(-18.5F, -1.5F, 52.0F, 1.5F, 6.0F, 10.0F)
                // Right Horizontal Stabilizer Wing (Carbon): X: 3 to 18, Y: 1.5 to 3.0, Z: 52 to 62
                .uv(128, 0).cuboid(3.0F, 1.5F, 52.0F, 15.0F, 1.5F, 10.0F)
                // Right Winglet Endplate (Carbon)
                .uv(128, 0).cuboid(17.0F, -1.5F, 52.0F, 1.5F, 6.0F, 10.0F);

        root.addChild("tail_boom", tailBoom, ModelTransform.NONE);

        // 4. Heavy Tubular Landing Skids & Induction Charging Plates
        ModelPartBuilder skids = ModelPartBuilder.create()
                // Left Tubular Skid: X: -17 to -14, Y: 20 to 23, Z: -30 to 26
                .uv(128, 0).cuboid(-17.0F, 20.0F, -30.0F, 3.0F, 3.0F, 56.0F)
                // Left Curved Front Tip
                .uv(128, 0).cuboid(-17.0F, 16.0F, -34.0F, 3.0F, 4.0F, 4.0F)
                // Right Tubular Skid: X: 14 to 17, Y: 20 to 23, Z: -30 to 26
                .uv(128, 0).cuboid(14.0F, 20.0F, -30.0F, 3.0F, 3.0F, 56.0F)
                // Right Curved Front Tip
                .uv(128, 0).cuboid(14.0F, 16.0F, -34.0F, 3.0F, 4.0F, 4.0F)
                // Front Left Strut
                .uv(128, 0).cuboid(-16.0F, 14.0F, -12.0F, 2.0F, 6.0F, 3.0F)
                // Front Right Strut
                .uv(128, 0).cuboid(14.0F, 14.0F, -12.0F, 2.0F, 6.0F, 3.0F)
                // Rear Left Strut
                .uv(128, 0).cuboid(-16.0F, 14.0F, 12.0F, 2.0F, 6.0F, 3.0F)
                // Rear Right Strut
                .uv(128, 0).cuboid(14.0F, 14.0F, 12.0F, 2.0F, 6.0F, 3.0F)
                // High-Voltage Induction Charge Contact Shoes (Gold, uv: 0, 128)
                .uv(0, 128).cuboid(-17.5F, 22.5F, -6.0F, 4.0F, 1.0F, 12.0F)
                .uv(0, 128).cuboid(13.5F, 22.5F, -6.0F, 4.0F, 1.0F, 12.0F);

        root.addChild("skids", skids, ModelTransform.NONE);

        // 5. High-Lift 4-Blade Main Rotor Assembly (Span: 120 units = 7.5 blocks)
        ModelPartBuilder mainRotor = ModelPartBuilder.create()
                // Rotor Mast Column
                .uv(128, 0).cuboid(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F)
                // Central Rotor Swashplate Hub
                .uv(128, 0).cuboid(-5.0F, -9.0F, -5.0F, 10.0F, 3.0F, 10.0F)
                // Blade 1 (Forward): 5 wide, 1 tall, 52 long
                .uv(128, 0).cuboid(-2.5F, -8.5F, -56.0F, 5.0F, 1.0F, 51.0F)
                // Blade 2 (Aft): 5 wide, 1 tall, 52 long
                .uv(128, 0).cuboid(-2.5F, -8.5F, 5.0F, 5.0F, 1.0F, 51.0F)
                // Blade 3 (Left): 52 wide, 1 tall, 5 long
                .uv(128, 0).cuboid(-56.0F, -8.5F, -2.5F, 51.0F, 1.0F, 5.0F)
                // Blade 4 (Right): 52 wide, 1 tall, 5 long
                .uv(128, 0).cuboid(5.0F, -8.5F, -2.5F, 51.0F, 1.0F, 5.0F);

        root.addChild("main_rotor", mainRotor, ModelTransform.pivot(0.0F, -5.0F, 4.0F));

        // 6. Tail Anti-Torque Rotor Assembly
        ModelPartBuilder tailRotor = ModelPartBuilder.create()
                // Tail Rotor Hub
                .uv(128, 0).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)
                // Upper Blade
                .uv(128, 0).cuboid(-1.0F, -14.0F, -1.5F, 2.0F, 13.0F, 3.0F)
                // Lower Blade
                .uv(128, 0).cuboid(-1.0F, 1.5F, -1.5F, 2.0F, 13.0F, 3.0F);

        root.addChild("tail_rotor", tailRotor, ModelTransform.pivot(2.5F, -10.0F, 74.0F));

        // 7. Crystal-Clear Panoramic Glass Canopy (Thin 0.5-1.0 unit panes, uv: 128, 128)
        ModelPartBuilder glass = ModelPartBuilder.create()
                // 1) Front Upper Windshield: Thin 1-unit pane
                .uv(128, 128).cuboid(-12.5F, -4.5F, -16.5F, 25.0F, 11.5F, 1.0F)
                // 2) Lower Chin Bubble Glass: Thin 1-unit pane (view down to landing pad!)
                .uv(128, 128).cuboid(-11.0F, 7.0F, -25.5F, 22.0F, 4.0F, 1.0F)
                // 3) Nose Slope Transition Glass: Thin 0.8-unit pane
                .uv(128, 128).cuboid(-11.5F, 6.0F, -24.5F, 23.0F, 1.0F, 8.5F)
                // 4) Left Cockpit Door Glass: Thin 0.5-unit pane
                .uv(128, 128).cuboid(-14.2F, -3.0F, -15.0F, 0.5F, 11.0F, 17.0F)
                // 5) Right Cockpit Door Glass: Thin 0.5-unit pane
                .uv(128, 128).cuboid(13.7F, -3.0F, -15.0F, 0.5F, 11.0F, 17.0F)
                // 6) Left Aft Passenger Window: Thin 0.5-unit pane
                .uv(128, 128).cuboid(-14.2F, -2.0F, 3.0F, 0.5F, 9.0F, 18.0F)
                // 7) Right Aft Passenger Window: Thin 0.5-unit pane
                .uv(128, 128).cuboid(13.7F, -2.0F, 3.0F, 0.5F, 9.0F, 18.0F)
                // 8) Overhead Skylight: Thin 0.5-unit pane
                .uv(128, 128).cuboid(-9.5F, -5.8F, -14.0F, 19.0F, 0.5F, 14.0F);

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
