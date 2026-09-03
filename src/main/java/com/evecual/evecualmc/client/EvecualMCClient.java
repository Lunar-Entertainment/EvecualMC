package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.client.render.CarEntityModel;
import com.evecual.evecualmc.client.render.CarEntityRenderer;
import com.evecual.evecualmc.client.screen.ElectronicCombinerScreen;
import com.evecual.evecualmc.entity.CarEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.network.PacketByteBuf;
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

        // Send Car driving inputs (W, A, S, D, and CTRL boost) to server and update client locally
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.getVehicle() instanceof CarEntity car) {
                boolean forward = client.options.forwardKey.isPressed();
                boolean back = client.options.backKey.isPressed();
                boolean left = client.options.leftKey.isPressed();
                boolean right = client.options.rightKey.isPressed();
                boolean sprint = client.options.sprintKey.isPressed();

                // Apply locally for instant responsiveness
                car.setInputs(forward, back, left, right, sprint);

                // Send to server for authoritative energy drain and synchronized physics
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(forward);
                buf.writeBoolean(back);
                buf.writeBoolean(left);
                buf.writeBoolean(right);
                buf.writeBoolean(sprint);
                ClientPlayNetworking.send(EvecualMC.CAR_INPUT_PACKET_ID, buf);
            }
        });

        LOGGER.info("EvecualMC client initialized with Combiner Screen, Car Renderer, and Driving Input Networking!");
    }
}
