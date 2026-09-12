package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.entity.TurretBulletEntity;
import com.evecual.evecualmc.turret.AmmoType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class TurretBulletEntityRenderer extends EntityRenderer<TurretBulletEntity> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/projectiles/arrow.png");

    public TurretBulletEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(TurretBulletEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(TurretBulletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));

        AmmoType type = entity.getAmmoType();
        float r = 0.9F, g = 0.9F, b = 0.9F;
        if (type == AmmoType.COPPER) {
            r = 0.95F; g = 0.55F; b = 0.15F;
        } else if (type == AmmoType.STEEL) {
            r = 0.40F; g = 0.50F; b = 0.65F;
        } else if (type == AmmoType.DIAMOND) {
            r = 0.00F; g = 0.90F; b = 1.00F;
        } else if (type == AmmoType.ELACTORITE) {
            r = 0.75F; g = 0.30F; b = 1.00F;
        }

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLightning());
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMatrix = entry.getPositionMatrix();

        float size = 0.15F;
        float length = 0.6F;

        // Glowing tracer projectile quad cross
        drawTracerQuad(consumer, posMatrix, -length, -size, 0, length, size, 0, r, g, b, 255);
        drawTracerQuad(consumer, posMatrix, -length, 0, -size, length, 0, size, r, g, b, 255);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void drawTracerQuad(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, int a) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a / 255f).next();
        consumer.vertex(matrix, x2, y1, z2).color(r, g, b, a / 255f).next();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a / 255f).next();
        consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a / 255f).next();
    }
}
