package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.render.RenderLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EvecualMCClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("evecualmc-client");

    @Override
    public void onInitializeClient() {
        // Register HUD tip overlay for Solar Panel, Battery, and Wire
        HudRenderCallback.EVENT.register(new EnergyHudOverlay());

        // Cutout render layer for wire block and solar panel
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.WIRE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.SOLAR_PANEL_BLOCK, RenderLayer.getCutout());

        LOGGER.info("EvecualMC client initialized with Energy HUD tip menu!");
    }
}
