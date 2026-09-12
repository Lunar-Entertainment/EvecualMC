package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.FlyingTurretEntity;
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

public class FlyingTurretEntityRenderer extends EntityRenderer<FlyingTurretEntity> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/entity/rc_drone.png");

    private final DefenseDroneEntityModel model;

    public FlyingTurretEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.35F;
        this.model = new DefenseDroneEntityModel(ctx.getPart(DefenseDroneEntityModel.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(FlyingTurretEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(FlyingTurretEntity drone, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 1. Heading Yaw Rotation
        float droneYaw = MathHelper.lerpAngleDegrees(tickDelta, drone.prevYaw, drone.getYaw());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - droneYaw));

        // 2. Flight Pitch & Roll Tilts
        float pitch = MathHelper.lerp(tickDelta, drone.getPitchTilt(), drone.getPitchTilt());
        float roll = MathHelper.lerp(tickDelta, drone.getRollTilt(), drone.getRollTilt());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));

        // 3. Minecraft model coordinate orientation (invert Y & Z)
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0, -1.5, 0.0);

        // Update animation angles
        this.model.setAngles(drone, 0.0F, 0.0F, drone.age + tickDelta, 0.0F, 0.0F);

        // 4. Render main fuselage, landing skids, motor arms, and propellers
        // Tint: Tactical Dark Stealth / Combat Cyan
        float r = 0.50F, g = 0.65F, b = 0.85F;
        VertexConsumer solidVertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.renderBodyAndProps(matrices, solidVertices, light, OverlayTexture.DEFAULT_UV, r, g, b, 1.0F);

        // 5. Continuous high-RPM rotor motion blur & ghost blades
        VertexConsumer transVertices = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        this.model.renderPropellerGhosts(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, 0.25F, 0.75F, 1.0F, 1.0F);
        this.model.renderBlurDiscs(matrices, transVertices, light, OverlayTexture.DEFAULT_UV, 0.35F);

        matrices.pop();
        super.render(drone, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
