package com.evecual.evecualmc.client.render;

import com.evecual.evecualmc.EvecualMC;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CrownArmorRenderer implements ArmorRenderer {
    private static final Identifier MECHANICALS_TEXTURE = new Identifier(EvecualMC.MOD_ID, "textures/models/armor/crown_mechanicals_layer_1.png");
    private static final Identifier ELECTRICIANS_TEXTURE = new Identifier(EvecualMC.MOD_ID, "textures/models/armor/crown_electricians_layer_1.png");
    private static final Identifier CASTLES_TEXTURE = new Identifier(EvecualMC.MOD_ID, "textures/models/armor/crown_castles_layer_1.png");

    private BipedEntityModel<LivingEntity> armorModel;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int light,
                       BipedEntityModel<LivingEntity> contextModel) {
        if (slot != EquipmentSlot.HEAD) {
            return;
        }

        if (this.armorModel == null) {
            this.armorModel = new BipedEntityModel<>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR));
        }

        Identifier texture;
        if (stack.isOf(EvecualMC.THE_MECHANICALS_CROWN)) {
            texture = MECHANICALS_TEXTURE;
        } else if (stack.isOf(EvecualMC.THE_ELECTRICIANS_CROWN)) {
            texture = ELECTRICIANS_TEXTURE;
        } else if (stack.isOf(EvecualMC.THE_CASTLES_CROWN)) {
            texture = CASTLES_TEXTURE;
        } else {
            return;
        }

        contextModel.copyBipedStateTo(this.armorModel);
        this.armorModel.setVisible(false);
        this.armorModel.head.visible = true;
        this.armorModel.hat.visible = true;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture));
        this.armorModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
