package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.CarEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
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

    public static float[] getGlassColorRgba(int glassColorId) {
        return switch (glassColorId) {
            case 1 -> new float[]{0.15F, 0.15F, 0.18F, 0.75F}; // Smoked Tinted Black
            case 2 -> new float[]{0.95F, 0.95F, 0.95F, 0.55F}; // White
            case 3 -> new float[]{0.55F, 0.55F, 0.60F, 0.60F}; // Gray
            case 4 -> new float[]{0.95F, 0.20F, 0.20F, 0.65F}; // Red
            case 5 -> new float[]{0.95F, 0.55F, 0.15F, 0.65F}; // Orange
            case 6 -> new float[]{0.95F, 0.90F, 0.20F, 0.65F}; // Yellow
            case 7 -> new float[]{0.35F, 0.90F, 0.25F, 0.65F}; // Lime / Green
            case 8 -> new float[]{0.25F, 0.85F, 0.95F, 0.65F}; // Cyan / Light Blue
            case 9 -> new float[]{0.15F, 0.35F, 0.95F, 0.65F}; // Blue
            case 10 -> new float[]{0.75F, 0.20F, 0.85F, 0.65F}; // Purple / Magenta
            case 11 -> new float[]{0.95F, 0.50F, 0.70F, 0.65F}; // Pink
            default -> new float[]{0.85F, 0.95F, 1.0F, 0.45F}; // 0: Clear Glass
        };
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

        // 1. Render Solid Car Body, Wheels, and Chassis
        Identifier texture = this.getTexture(car);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(texture));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        // 2. Render Real Transparent Colored Glass Canopy!
        float[] glassRgba = getGlassColorRgba(car.getGlassColor());
        VertexConsumer glassConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
        this.model.renderGlass(matrices, glassConsumer, light, OverlayTexture.DEFAULT_UV, glassRgba[0], glassRgba[1], glassRgba[2], glassRgba[3]);

        // 3. Wheel rotational motion blur when cruising at speed
        float speed = (float) car.getCurrentSpeed();
        if (Math.abs(speed) > 0.08F) {
            VertexConsumer blurConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
            this.model.renderWheelMotionBlur(matrices, blurConsumer, light, OverlayTexture.DEFAULT_UV, speed);
        }

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
