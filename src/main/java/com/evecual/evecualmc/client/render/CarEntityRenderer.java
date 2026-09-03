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
    private static final Identifier[] TEXTURES = new Identifier[]{
            new Identifier("evecualmc", "textures/entity/car_red.png"),    // 0: Sport Crimson & Carbon
            new Identifier("evecualmc", "textures/entity/car_blue.png"),   // 1: Cyber Electric Blue
            new Identifier("evecualmc", "textures/entity/car_black.png"),  // 2: Stealth Midnight & Cyan
            new Identifier("evecualmc", "textures/entity/car_lime.png"),   // 3: Neon Lime & Carbon
            new Identifier("evecualmc", "textures/entity/car_white.png"),  // 4: Luxury Pearl White & Slate
            new Identifier("evecualmc", "textures/entity/car_yellow.png")  // 5: Racing Yellow & Carbon
    };

    private final CarEntityModel model;

    public CarEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.9F;
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

        // Update wheel steering angle
        this.model.setAngles(car, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(car)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
        super.render(car, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(CarEntity car) {
        int variant = car.getColorVariant();
        if (variant >= 0 && variant < TEXTURES.length) {
            return TEXTURES[variant];
        }
        return TEXTURES[0];
    }
}
