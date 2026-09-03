package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.client.render.CarEntityModel;
import com.evecual.evecualmc.client.render.CarEntityRenderer;
import com.evecual.evecualmc.client.screen.CarTrunkScreen;
import com.evecual.evecualmc.client.screen.ElectronicCombinerScreen;
import com.evecual.evecualmc.entity.CarEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EvecualMCClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("evecualmc-client");

    public static final KeyBinding OPEN_TRUNK_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.open_trunk",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding ATTACH_CABLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.attach_cable",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding AUTO_PARK_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.auto_park",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.evecualmc.evecual"
    ));

    @Override
    public void onInitializeClient() {
        // Register HUD tip overlay for Solar Panel, Battery, Combiner, Charger, and Car
        HudRenderCallback.EVENT.register(new EnergyHudOverlay());

        // Register Screens
        HandledScreens.register(EvecualMC.ELECTRONIC_COMBINER_SCREEN_HANDLER, ElectronicCombinerScreen::new);
        HandledScreens.register(EvecualMC.CAR_TRUNK_SCREEN_HANDLER, CarTrunkScreen::new);

        // Register Car Entity Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(CarEntityModel.MODEL_LAYER, CarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.CAR_ENTITY, CarEntityRenderer::new);

        // Cutout render layer for wire block, solar panel, and parking lines
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.WIRE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.SOLAR_PANEL_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.PARKING_LINES_BLOCK, RenderLayer.getCutout());

        // Register Charger Waypoint Network Sync
        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.CHARGER_WAYPOINT_PACKET_ID, (client, handler, buf, responseSender) -> {
            net.minecraft.util.math.BlockPos pos = buf.readBlockPos();
            boolean add = buf.readBoolean();
            client.execute(() -> {
                if (add) {
                    ChargerWaypointManager.addWaypoint(pos);
                } else {
                    ChargerWaypointManager.removeWaypoint(pos);
                }
            });
        });

        // Client Tick: handle car driving inputs and 'Z' key for opening trunk
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                // 1. Driving inputs
                if (client.player.getVehicle() instanceof CarEntity car) {
                    boolean forward = client.options.forwardKey.isPressed();
                    boolean back = client.options.backKey.isPressed();
                    boolean left = client.options.leftKey.isPressed();
                    boolean right = client.options.rightKey.isPressed();
                    boolean sprint = client.options.sprintKey.isPressed();

                    car.setInputs(forward, back, left, right, sprint);

                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeBoolean(forward);
                    buf.writeBoolean(back);
                    buf.writeBoolean(left);
                    buf.writeBoolean(right);
                    buf.writeBoolean(sprint);
                    ClientPlayNetworking.send(EvecualMC.CAR_INPUT_PACKET_ID, buf);
                }

                // 2. 'Z' Key to Open Car Trunk
                while (OPEN_TRUNK_KEY.wasPressed()) {
                    CarEntity targetCar = null;
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        targetCar = car;
                    } else if (client.targetedEntity instanceof CarEntity car) {
                        targetCar = car;
                    } else if (client.world != null) {
                        for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(5.0))) {
                            if (entity instanceof CarEntity car) {
                                targetCar = car;
                                break;
                            }
                        }
                    }

                    if (targetCar != null) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(targetCar.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                    }
                }

                // 3. 'X' Key to Attach / Detach Charger Cable to Car
                while (ATTACH_CABLE_KEY.wasPressed()) {
                    ClientPlayNetworking.send(EvecualMC.TOGGLE_CABLE_PACKET_ID, PacketByteBufs.empty());
                }

                // 4. 'C' Key to Auto Park Vehicle into nearest Parking Lines bay
                while (AUTO_PARK_KEY.wasPressed()) {
                    ClientPlayNetworking.send(EvecualMC.AUTO_PARK_PACKET_ID, PacketByteBufs.empty());
                }
            }
        });

        LOGGER.info("EvecualMC client initialized with Trunk Screen, Keybindings, and Color Variants!");
    }
}
