package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.CarEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CarEntityRenderer extends EntityRenderer<CarEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/car.png");
    private final CarEntityModel model;

    public CarEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.model = new CarEntityModel(context.getPart(CarEntityModel.MODEL_LAYER));
    }

    @Override
    public void render(CarEntity car, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Smooth yaw rotation
        float carYaw = MathHelper.lerpAngleDegrees(tickDelta, car.prevYaw, car.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - carYaw));

        // Invert Y axis for Minecraft entity model orientation
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0, -1.5, 0.0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(car)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
        super.render(car, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(CarEntity entity) {
        return TEXTURE;
    }
}
