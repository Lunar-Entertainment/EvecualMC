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
        ModelPartData modelPartData = modelData.getRoot();

        // Main chassis body & cabin
        modelPartData.addChild("body", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-10.0F, -6.0F, -14.0F, 20.0F, 6.0F, 28.0F) // Lower body
                .uv(0, 34).cuboid(-8.0F, -12.0F, -6.0F, 16.0F, 6.0F, 14.0F) // Cabin roof
                .uv(0, 0).cuboid(-9.0F, -14.0F, 10.0F, 18.0F, 2.0F, 4.0F)   // Rear Spoiler
                .uv(46, 34).cuboid(-8.0F, -12.0F, 11.0F, 2.0F, 6.0F, 2.0F)  // Spoiler Left Post
                .uv(46, 34).cuboid(6.0F, -12.0F, 11.0F, 2.0F, 6.0F, 2.0F),  // Spoiler Right Post
                ModelTransform.pivot(0.0F, 22.0F, 0.0F));

        // Front Left Wheel
        modelPartData.addChild("wheel_fl", ModelPartBuilder.create()
                .uv(0, 54).cuboid(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F),
                ModelTransform.pivot(-11.0F, 21.0F, -8.0F));

        // Front Right Wheel
        modelPartData.addChild("wheel_fr", ModelPartBuilder.create()
                .uv(0, 54).cuboid(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F),
                ModelTransform.pivot(11.0F, 21.0F, -8.0F));

        // Rear Left Wheel
        modelPartData.addChild("wheel_rl", ModelPartBuilder.create()
                .uv(0, 54).cuboid(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F),
                ModelTransform.pivot(-11.0F, 21.0F, 8.0F));

        // Rear Right Wheel
        modelPartData.addChild("wheel_rr", ModelPartBuilder.create()
                .uv(0, 54).cuboid(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F),
                ModelTransform.pivot(11.0F, 21.0F, 8.0F));

        return TexturedModelData.of(modelData, 96, 64);
    }

    @Override
    public void setAngles(CarEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
