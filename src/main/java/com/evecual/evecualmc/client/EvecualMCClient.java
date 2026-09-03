package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.client.render.CarEntityModel;
import com.evecual.evecualmc.client.render.CarEntityRenderer;
import com.evecual.evecualmc.client.screen.ElectronicCombinerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EvecualMCClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("evecualmc-client");

    @Override
    public void onInitializeClient() {
        // Register HUD tip overlay for Solar Panel, Battery, Combiner, Charger, and Car
        HudRenderCallback.EVENT.register(new EnergyHudOverlay());

        // Register Combiner Screen
        HandledScreens.register(EvecualMC.ELECTRONIC_COMBINER_SCREEN_HANDLER, ElectronicCombinerScreen::new);

        // Register Car Entity Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(CarEntityModel.MODEL_LAYER, CarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.CAR_ENTITY, CarEntityRenderer::new);

        // Cutout render layer for wire block and solar panel
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.WIRE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.SOLAR_PANEL_BLOCK, RenderLayer.getCutout());

        LOGGER.info("EvecualMC client initialized with Combiner Screen and Car Entity Renderer!");
    }
}
