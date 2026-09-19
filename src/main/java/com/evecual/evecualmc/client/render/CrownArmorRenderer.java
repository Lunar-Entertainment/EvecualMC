package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.EvecualMC;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Custom 3D ArmorRenderer for the three Legendary Crowns.
 * Renders true 3D models firmly positioned on top of the head/forehead (Y = -6.5F to -14.5F relative to head pivot),
 * eliminating any neck/throat overlap and synchronizing seamlessly with player head rotations.
 */
public class CrownArmorRenderer implements ArmorRenderer {
    private static final Identifier MECHANICALS_TEXTURE = new Identifier(EvecualMC.MOD_ID, "textures/models/armor/crown_mechanicals_3d.png");
    private static final Identifier ELECTRICIANS_TEXTURE = new Identifier(EvecualMC.MOD_ID, "textures/models/armor/crown_electricians_3d.png");
    private static final Identifier CASTLES_TEXTURE = new Identifier(EvecualMC.MOD_ID, "textures/models/armor/crown_castles_3d.png");

    private static ModelPart mechanicalsCrown;
    private static ModelPart electriciansCrown;
    private static ModelPart castlesCrown;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int light,
                       BipedEntityModel<LivingEntity> contextModel) {
        if (slot != EquipmentSlot.HEAD) {
            return;
        }
        renderCrownDirectly(matrices, vertexConsumers, stack, light, contextModel);
    }

    public static void renderCrownDirectly(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                                          int light, BipedEntityModel<?> contextModel) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (mechanicalsCrown == null) {
            mechanicalsCrown = createMechanicalsModel();
            electriciansCrown = createElectriciansModel();
            castlesCrown = createCastlesModel();
        }

        ModelPart model;
        Identifier texture;
        if (stack.isOf(EvecualMC.THE_MECHANICALS_CROWN)) {
            model = mechanicalsCrown;
            texture = MECHANICALS_TEXTURE;
        } else if (stack.isOf(EvecualMC.THE_ELECTRICIANS_CROWN)) {
            model = electriciansCrown;
            texture = ELECTRICIANS_TEXTURE;
        } else if (stack.isOf(EvecualMC.THE_CASTLES_CROWN)) {
            model = castlesCrown;
            texture = CASTLES_TEXTURE;
        } else {
            return;
        }

        model.copyTransform(contextModel.head);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static ModelPart createMechanicalsModel() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartBuilder crown = ModelPartBuilder.create()
                // Base circlet band around upper forehead (Y: -8.5 to -6.5)
                .uv(0, 0).cuboid(-4.8F, -8.5F, -4.8F, 9.6F, 2.0F, 0.8F)   // Front
                .uv(0, 4).cuboid(-4.8F, -8.5F, 4.0F, 9.6F, 2.0F, 0.8F)    // Back
                .uv(0, 8).cuboid(4.0F, -8.5F, -4.0F, 0.8F, 2.0F, 8.0F)    // Left
                .uv(0, 8).cuboid(-4.8F, -8.5F, -4.0F, 0.8F, 2.0F, 8.0F)   // Right

                // Perimeter Cog Teeth / Spires (Y: -10.5 to -8.5)
                .uv(24, 0).cuboid(-4.8F, -10.5F, -4.8F, 1.5F, 2.0F, 1.5F) // Front-Left Corner Cog
                .uv(24, 0).cuboid(3.3F, -10.5F, -4.8F, 1.5F, 2.0F, 1.5F)  // Front-Right Corner Cog
                .uv(24, 0).cuboid(-4.8F, -10.5F, 3.3F, 1.5F, 2.0F, 1.5F)  // Back-Left Corner Cog
                .uv(24, 0).cuboid(3.3F, -10.5F, 3.3F, 1.5F, 2.0F, 1.5F)   // Back-Right Corner Cog
                .uv(24, 0).cuboid(-1.0F, -10.0F, -4.8F, 2.0F, 1.5F, 0.8F) // Front-Mid Spire
                .uv(24, 0).cuboid(-1.0F, -10.0F, 4.0F, 2.0F, 1.5F, 0.8F)  // Back-Mid Spire
                .uv(24, 0).cuboid(4.0F, -10.0F, -1.0F, 0.8F, 1.5F, 2.0F)  // Left-Mid Spire
                .uv(24, 0).cuboid(-4.8F, -10.0F, -1.0F, 0.8F, 1.5F, 2.0F) // Right-Mid Spire

                // Grand Front Cog (Foreground showcase: Y: -14.0 to -6.5)
                .uv(0, 20).cuboid(-2.0F, -12.5F, -5.4F, 4.0F, 4.0F, 1.0F)  // Cog Hub
                .uv(12, 20).cuboid(-1.0F, -14.0F, -5.4F, 2.0F, 7.0F, 1.0F) // Vertical Teeth
                .uv(20, 20).cuboid(-3.5F, -11.5F, -5.4F, 7.0F, 2.0F, 1.0F) // Horizontal Teeth
                .uv(38, 20).cuboid(-0.5F, -11.0F, -5.8F, 1.0F, 1.0F, 0.6F) // Steel Axle Pin

                // Side Meshing Gears (Left & Right temples)
                .uv(0, 30).cuboid(4.2F, -11.5F, -1.5F, 1.0F, 3.0F, 3.0F)   // Left Gear Disc
                .uv(10, 30).cuboid(4.2F, -12.5F, -0.5F, 1.0F, 5.0F, 1.0F)  // Left Gear Teeth
                .uv(0, 30).cuboid(-5.2F, -11.5F, -1.5F, 1.0F, 3.0F, 3.0F)  // Right Gear Disc
                .uv(10, 30).cuboid(-5.2F, -12.5F, -0.5F, 1.0F, 5.0F, 1.0F) // Right Gear Teeth

                // Rear Steam Pressure Gauge
                .uv(20, 30).cuboid(-1.5F, -10.5F, 4.2F, 3.0F, 3.0F, 1.0F); // Gauge & Valve

        root.addChild("crown", crown, ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 64).createModel().getChild("crown");
    }

    private static ModelPart createElectriciansModel() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartBuilder crown = ModelPartBuilder.create()
                // Superconductor base band
                .uv(0, 0).cuboid(-4.8F, -8.5F, -4.8F, 9.6F, 2.0F, 0.8F)   // Front
                .uv(0, 4).cuboid(-4.8F, -8.5F, 4.0F, 9.6F, 2.0F, 0.8F)    // Back
                .uv(0, 8).cuboid(4.0F, -8.5F, -4.0F, 0.8F, 2.0F, 8.0F)    // Left
                .uv(0, 8).cuboid(-4.8F, -8.5F, -4.0F, 0.8F, 2.0F, 8.0F)   // Right

                // Front Elactorite Core Gem & Socket (Y: -12.0 to -7.5)
                .uv(0, 20).cuboid(-2.0F, -11.5F, -5.4F, 4.0F, 4.0F, 1.0F)  // Socket Frame
                .uv(12, 20).cuboid(-1.5F, -11.0F, -5.7F, 3.0F, 3.0F, 0.6F) // Faceted Glowing Elactorite Gem

                // Twin Lightning Horn Spires (sweeping upwards and backwards to Y: -14.5)
                // Left Horn
                .uv(20, 20).cuboid(3.2F, -10.5F, -4.8F, 1.5F, 2.0F, 1.5F)
                .uv(20, 20).cuboid(3.6F, -12.5F, -4.0F, 1.2F, 2.5F, 1.2F)
                .uv(20, 20).cuboid(4.0F, -14.5F, -3.0F, 0.8F, 2.5F, 0.8F)
                // Right Horn
                .uv(20, 20).cuboid(-4.7F, -10.5F, -4.8F, 1.5F, 2.0F, 1.5F)
                .uv(20, 20).cuboid(-4.8F, -12.5F, -4.0F, 1.2F, 2.5F, 1.2F)
                .uv(20, 20).cuboid(-4.8F, -14.5F, -3.0F, 0.8F, 2.5F, 0.8F)

                // Twin Tesla Coils (Temples)
                .uv(0, 30).cuboid(4.2F, -12.0F, -0.5F, 1.0F, 4.0F, 1.0F)   // Left Copper Rod
                .uv(8, 30).cuboid(3.7F, -13.5F, -1.0F, 2.0F, 1.5F, 2.0F)   // Left Discharge Sphere
                .uv(0, 30).cuboid(-5.2F, -12.0F, -0.5F, 1.0F, 4.0F, 1.0F)  // Right Copper Rod
                .uv(8, 30).cuboid(-5.7F, -13.5F, -1.0F, 2.0F, 1.5F, 2.0F)  // Right Discharge Sphere

                // Central Lightning Mast & Capacitor Emitter
                .uv(20, 30).cuboid(-0.5F, -14.0F, -0.5F, 1.0F, 5.5F, 1.0F)
                .uv(26, 30).cuboid(-1.0F, -14.8F, -1.0F, 2.0F, 1.0F, 2.0F);

        root.addChild("crown", crown, ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 64).createModel().getChild("crown");
    }

    private static ModelPart createCastlesModel() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartBuilder crown = ModelPartBuilder.create()
                // Fortress Stone Walls
                .uv(0, 0).cuboid(-4.8F, -8.5F, -4.8F, 9.6F, 2.0F, 0.8F)   // Front Wall
                .uv(0, 4).cuboid(-4.8F, -8.5F, 4.0F, 9.6F, 2.0F, 0.8F)    // Back Wall
                .uv(0, 8).cuboid(4.0F, -8.5F, -4.0F, 0.8F, 2.0F, 8.0F)    // Left Wall
                .uv(0, 8).cuboid(-4.8F, -8.5F, -4.0F, 0.8F, 2.0F, 8.0F)   // Right Wall
                .uv(0, 18).cuboid(-5.0F, -6.5F, -5.0F, 10.0F, 0.5F, 10.0F) // Royal Gold Bottom Moulding

                // Four Corner Watchtowers & Crenellations (Y: -12.0 to -8.5)
                .uv(20, 0).cuboid(-5.0F, -11.0F, -5.0F, 2.0F, 3.0F, 2.0F)  // Front-Left Tower
                .uv(30, 0).cuboid(-5.2F, -12.0F, -5.2F, 2.4F, 1.0F, 2.4F)  // Front-Left Crenellation
                .uv(20, 0).cuboid(3.0F, -11.0F, -5.0F, 2.0F, 3.0F, 2.0F)   // Front-Right Tower
                .uv(30, 0).cuboid(2.8F, -12.0F, -5.2F, 2.4F, 1.0F, 2.4F)   // Front-Right Crenellation
                .uv(20, 0).cuboid(-5.0F, -11.0F, 3.0F, 2.0F, 3.0F, 2.0F)   // Back-Left Tower
                .uv(30, 0).cuboid(-5.2F, -12.0F, 2.8F, 2.4F, 1.0F, 2.4F)   // Back-Left Crenellation
                .uv(20, 0).cuboid(3.0F, -11.0F, 3.0F, 2.0F, 3.0F, 2.0F)    // Back-Right Tower
                .uv(30, 0).cuboid(2.8F, -12.0F, 2.8F, 2.4F, 1.0F, 2.4F)    // Back-Right Crenellation

                // Central Fortress Keep & Golden Royal Spire
                .uv(0, 26).cuboid(-2.0F, -11.5F, -5.0F, 4.0F, 3.5F, 1.5F)  // Center Keep
                .uv(14, 26).cuboid(-0.5F, -14.5F, -4.8F, 1.0F, 3.0F, 1.0F) // Golden Spire

                // TWIN 3D SIEGE CANNONS mounted on temple battlements!
                // Left Cannon
                .uv(0, 36).cuboid(-5.5F, -9.5F, -1.5F, 1.4F, 1.8F, 2.5F)   // Trunnion Carriage
                .uv(12, 36).cuboid(-5.6F, -10.5F, -4.5F, 1.6F, 1.6F, 5.0F) // Heavy Iron Barrel
                .uv(28, 36).cuboid(-5.8F, -10.7F, -4.8F, 2.0F, 2.0F, 0.5F) // Muzzle Bore Ring
                // Right Cannon
                .uv(0, 36).cuboid(4.1F, -9.5F, -1.5F, 1.4F, 1.8F, 2.5F)    // Trunnion Carriage
                .uv(12, 36).cuboid(4.0F, -10.5F, -4.5F, 1.6F, 1.6F, 5.0F)  // Heavy Iron Barrel
                .uv(28, 36).cuboid(3.8F, -10.7F, -4.8F, 2.0F, 2.0F, 0.5F)  // Muzzle Bore Ring
                // Rear Wall Cannonball Stack
                .uv(36, 36).cuboid(-1.5F, -9.5F, 3.8F, 3.0F, 1.5F, 1.0F);

        root.addChild("crown", crown, ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 64).createModel().getChild("crown");
    }
}
