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

        // 1. Main Chassis & Exterior Body (Red Metallic & Carbon)
        ModelPartBuilder body = ModelPartBuilder.create()
                // Floor base: 32 wide, 2 thick, 52 long
                .uv(0, 46).cuboid(-16.0F, -2.0F, -26.0F, 32.0F, 2.0F, 52.0F)
                // Front Hood / Engine Bay: 30 wide, 7 tall, 18 long
                .uv(0, 0).cuboid(-15.0F, -9.0F, -25.0F, 30.0F, 7.0F, 18.0F)
                // Front Bumper / Splitter: 32 wide, 4 tall, 3 long
                .uv(0, 0).cuboid(-16.0F, -5.0F, -28.0F, 32.0F, 4.0F, 3.0F)
                // Rear Trunk / Deck: 30 wide, 7 tall, 12 long
                .uv(0, 0).cuboid(-15.0F, -9.0F, 14.0F, 30.0F, 7.0F, 12.0F)
                // Rear Bumper: 32 wide, 4 tall, 3 long
                .uv(0, 0).cuboid(-16.0F, -5.0F, 26.0F, 32.0F, 4.0F, 3.0F)
                // Left Door / Side Panel: 3 wide, 7 tall, 21 long
                .uv(0, 0).cuboid(-16.0F, -9.0F, -7.0F, 3.0F, 7.0F, 21.0F)
                // Right Door / Side Panel: 3 wide, 7 tall, 21 long
                .uv(0, 0).cuboid(13.0F, -9.0F, -7.0F, 3.0F, 7.0F, 21.0F)
                // Rear Spoiler Wing: 30 wide, 2 tall, 5 long
                .uv(0, 0).cuboid(-15.0F, -14.0F, 20.0F, 30.0F, 2.0F, 5.0F)
                // Spoiler Left Post
                .uv(0, 46).cuboid(-12.0F, -12.0F, 21.0F, 2.0F, 4.0F, 2.0F)
                // Spoiler Right Post
                .uv(0, 46).cuboid(10.0F, -12.0F, 21.0F, 2.0F, 4.0F, 2.0F);

        root.addChild("body", body, ModelTransform.pivot(0.0F, 17.0F, 0.0F));

        // 2. Interior Elements (Dashboard, Seats, Steering Wheel, Console)
        ModelPartBuilder interior = ModelPartBuilder.create()
                // Dashboard with screens: 26 wide, 6 tall, 4 deep
                .uv(0, 66).cuboid(-13.0F, -10.0F, -7.0F, 26.0F, 6.0F, 4.0F)
                // Steering Wheel: 6 wide, 6 tall, 1 deep
                .uv(0, 66).cuboid(-8.0F, -11.0F, -3.0F, 6.0F, 6.0F, 1.0F)
                // Steering Column
                .uv(0, 46).cuboid(-6.0F, -8.0F, -5.0F, 2.0F, 2.0F, 3.0F)
                // Center Console: 4 wide, 4 tall, 16 long
                .uv(0, 46).cuboid(-2.0F, -5.0F, -4.0F, 4.0F, 4.0F, 16.0F)

                // Driver Seat Cushion (Left side): 10 wide, 3 tall, 10 long
                .uv(64, 46).cuboid(-13.0F, -4.0F, -2.0F, 10.0F, 3.0F, 10.0F)
                // Driver Seat Backrest: 10 wide, 11 tall, 3 deep
                .uv(64, 46).cuboid(-13.0F, -15.0F, 8.0F, 10.0F, 11.0F, 3.0F)
                // Driver Headrest: 6 wide, 3 tall, 2 deep
                .uv(64, 46).cuboid(-11.0F, -18.0F, 8.0F, 6.0F, 3.0F, 2.0F)

                // Passenger Seat Cushion (Right side): 10 wide, 3 tall, 10 long
                .uv(64, 46).cuboid(3.0F, -4.0F, -2.0F, 10.0F, 3.0F, 10.0F)
                // Passenger Seat Backrest: 10 wide, 11 tall, 3 deep
                .uv(64, 46).cuboid(3.0F, -15.0F, 8.0F, 10.0F, 11.0F, 3.0F)
                // Passenger Headrest: 6 wide, 3 tall, 2 deep
                .uv(64, 46).cuboid(5.0F, -18.0F, 8.0F, 6.0F, 3.0F, 2.0F);

        root.addChild("interior", interior, ModelTransform.pivot(0.0F, 17.0F, 0.0F));

        // 3. Cabin Canopy (Roof, A-Pillars, C-Pillars, Windshield Frame)
        ModelPartBuilder canopy = ModelPartBuilder.create()
                // Left A-Pillar (windshield strut)
                .uv(0, 0).cuboid(-15.0F, -18.0F, -4.0F, 2.0F, 10.0F, 2.0F)
                // Right A-Pillar (windshield strut)
                .uv(0, 0).cuboid(13.0F, -18.0F, -4.0F, 2.0F, 10.0F, 2.0F)
                // Left C-Pillar (rear window strut)
                .uv(0, 0).cuboid(-15.0F, -18.0F, 12.0F, 2.0F, 10.0F, 2.0F)
                // Right C-Pillar (rear window strut)
                .uv(0, 0).cuboid(13.0F, -18.0F, 12.0F, 2.0F, 10.0F, 2.0F)
                // Hardtop Roof: 28 wide, 2 tall, 16 long
                .uv(0, 0).cuboid(-14.0F, -19.0F, -4.0F, 28.0F, 2.0F, 17.0F)
                // Front Windshield Glass Pane: 26 wide, 8 tall, 1 deep
                .uv(96, 86).cuboid(-13.0F, -17.0F, -4.0F, 26.0F, 8.0F, 1.0F);

        root.addChild("canopy", canopy, ModelTransform.pivot(0.0F, 17.0F, 0.0F));

        // 4. Wheels (4 large chunky tires with rims, radius 5)
        // Front Left Wheel
        root.addChild("wheel_fl", ModelPartBuilder.create()
                .uv(0, 86).cuboid(-3.0F, -5.0F, -5.0F, 6.0F, 10.0F, 10.0F),
                ModelTransform.pivot(-17.0F, 19.0F, -17.0F));

        // Front Right Wheel
        root.addChild("wheel_fr", ModelPartBuilder.create()
                .uv(0, 86).cuboid(-3.0F, -5.0F, -5.0F, 6.0F, 10.0F, 10.0F),
                ModelTransform.pivot(17.0F, 19.0F, -17.0F));

        // Rear Left Wheel
        root.addChild("wheel_rl", ModelPartBuilder.create()
                .uv(0, 86).cuboid(-3.0F, -5.0F, -5.0F, 6.0F, 10.0F, 10.0F),
                ModelTransform.pivot(-17.0F, 19.0F, 18.0F));

        // Rear Right Wheel
        root.addChild("wheel_rr", ModelPartBuilder.create()
                .uv(0, 86).cuboid(-3.0F, -5.0F, -5.0F, 6.0F, 10.0F, 10.0F),
                ModelTransform.pivot(17.0F, 19.0F, 18.0F));

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
