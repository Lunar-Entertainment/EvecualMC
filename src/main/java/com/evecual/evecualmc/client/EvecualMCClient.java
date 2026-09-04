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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
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

    public static final KeyBinding RC_CAMERA_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.rc_camera",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "category.evecualmc.evecual"
    ));

    private static boolean wasCPressed = false;
    private static net.minecraft.client.option.Perspective previousPerspective = net.minecraft.client.option.Perspective.FIRST_PERSON;
    private static float rcCameraYaw = 0.0F;
    private static float rcCameraPitch = 12.0F;

    public static float getRcCameraYaw() {
        return rcCameraYaw;
    }

    public static float getRcCameraPitch() {
        return rcCameraPitch;
    }

    public static void resetRcCameraAngle() {
        rcCameraYaw = 0.0F;
        rcCameraPitch = 12.0F;
    }

    public static boolean isRcLinkActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.currentScreen != null) {
            return false;
        }
        net.minecraft.item.ItemStack held = null;
        if (client.player.getMainHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getMainHandStack();
        } else if (client.player.getOffHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getOffHandStack();
        }
        if (held != null && held.hasNbt()) {
            net.minecraft.nbt.NbtCompound nbt = held.getNbt();
            return nbt != null && nbt.containsUuid("PairedCar") && nbt.getBoolean("ActiveLink");
        }
        return false;
    }

    public static com.evecual.evecualmc.entity.RcCarEntity getTargetRcCar(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return null;
        net.minecraft.item.ItemStack held = null;
        if (client.player.getMainHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getMainHandStack();
        } else if (client.player.getOffHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getOffHandStack();
        }
        if (held == null || !held.hasNbt()) return null;
        net.minecraft.nbt.NbtCompound nbt = held.getNbt();
        if (nbt == null || !nbt.containsUuid("PairedCar")) return null;
        java.util.UUID pairedUuid = nbt.getUuid("PairedCar");

        for (Entity e : client.world.getEntities()) {
            if (e instanceof com.evecual.evecualmc.entity.RcCarEntity rc && rc.getUuid().equals(pairedUuid)) {
                return rc;
            }
        }
        return null;
    }

    public static void toggleRcCamera(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        com.evecual.evecualmc.entity.RcCarEntity targetRc = getTargetRcCar(client);
        if (targetRc == null) {
            client.player.sendMessage(Text.literal("§c📷 No linked RC Car found in range!"), true);
            return;
        }

        if (client.getCameraEntity() == targetRc) {
            client.setCameraEntity(client.player);
            client.options.setPerspective(previousPerspective);
            client.player.sendMessage(Text.literal("§7📷 RC Camera: §cDISABLED §7[Player View]"), true);
        } else {
            resetRcCameraAngle();
            previousPerspective = client.options.getPerspective();
            client.setCameraEntity(targetRc);
            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
            client.player.sendMessage(Text.literal("§b📷 RC Camera: §aENABLED §7[Arrow Keys to Rotate, F to return]"), true);
        }
    }

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

        // Register RC Car Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(com.evecual.evecualmc.client.render.RcCarEntityModel.MODEL_LAYER, com.evecual.evecualmc.client.render.RcCarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_CAR_ENTITY, com.evecual.evecualmc.client.render.RcCarEntityRenderer::new);

        // Cutout render layer for wire block, solar panel, parking lines, and RC charger
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.WIRE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.SOLAR_PANEL_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.PARKING_LINES_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.RC_CHARGER_BLOCK, RenderLayer.getCutout());

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

        // Register Auto-Park S2C sync
        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.START_AUTO_PARK_S2C_PACKET_ID, (client, handler, buf, responseSender) -> {
            int carId = buf.readInt();
            double tx = buf.readDouble();
            double ty = buf.readDouble();
            double tz = buf.readDouble();
            float tyaw = buf.readFloat();
            double ex = buf.readDouble();
            double ey = buf.readDouble();
            double ez = buf.readDouble();
            client.execute(() -> {
                if (client.world != null && client.world.getEntityById(carId) instanceof CarEntity car) {
                    car.startAutoPark(tx, ty, tz, tyaw, ex, ey, ez);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.CANCEL_AUTO_PARK_S2C_PACKET_ID, (client, handler, buf, responseSender) -> {
            int carId = buf.readInt();
            client.execute(() -> {
                if (client.world != null && client.world.getEntityById(carId) instanceof CarEntity car) {
                    car.cancelAutoPark(null);
                }
            });
        });

        // Client Start Tick: intercept F key before vanilla swapHandsKey processes it
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && isRcLinkActive()) {
                boolean fToggled = false;
                while (RC_CAMERA_KEY.wasPressed()) {
                    fToggled = true;
                }
                while (client.options.swapHandsKey.wasPressed()) {
                    fToggled = true;
                }
                if (fToggled) {
                    toggleRcCamera(client);
                }
            }
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

                    if (car.isAutoParking()) {
                        if (forward || back || left || right) {
                            car.cancelAutoPark("§e🅿️ Auto-parking cancelled by driver.");
                            car.setInputs(forward, back, left, right, sprint);
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeBoolean(forward);
                            buf.writeBoolean(back);
                            buf.writeBoolean(left);
                            buf.writeBoolean(right);
                            buf.writeBoolean(sprint);
                            ClientPlayNetworking.send(EvecualMC.CAR_INPUT_PACKET_ID, buf);
                        }
                    } else {
                        car.setInputs(forward, back, left, right, sprint);

                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeBoolean(forward);
                        buf.writeBoolean(back);
                        buf.writeBoolean(left);
                        buf.writeBoolean(right);
                        buf.writeBoolean(sprint);
                        ClientPlayNetworking.send(EvecualMC.CAR_INPUT_PACKET_ID, buf);
                    }
                }

                // 2. 'Z' Key to Open Car Trunk
                while (OPEN_TRUNK_KEY.wasPressed()) {
                    Entity targetCar = null;
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        targetCar = car;
                    } else if (client.targetedEntity instanceof CarEntity || client.targetedEntity instanceof com.evecual.evecualmc.entity.RcCarEntity) {
                        targetCar = client.targetedEntity;
                    } else if (client.world != null) {
                        for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(5.0))) {
                            if (entity instanceof CarEntity || entity instanceof com.evecual.evecualmc.entity.RcCarEntity) {
                                targetCar = entity;
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

                // 4. 'C' Key handling
                boolean cDown = (client.currentScreen == null && InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_C)) || AUTO_PARK_KEY.isPressed();

                // 5. RC Controller Remote Driving Control
                boolean isRcActive = isRcLinkActive();
                if (isRcActive) {
                    // Immobilize player while RC Link is active
                    client.player.input.movementForward = 0.0F;
                    client.player.input.movementSideways = 0.0F;
                    client.player.input.jumping = false;
                    client.player.input.sneaking = false;
                    client.player.setVelocity(0.0, Math.min(client.player.getVelocity().y, 0.0), 0.0);
                    client.player.setSprinting(false);

                    // Consume vanilla swap hands key (F)
                    while (client.options.swapHandsKey.wasPressed()) {}

                    com.evecual.evecualmc.entity.RcCarEntity targetRc = getTargetRcCar(client);
                    if (targetRc != null) {
                        java.util.UUID pairedUuid = targetRc.getUuid();

                        // 'C' Key while RC link is active: Auto Return to RC Charger!
                        if (cDown) {
                            if (!wasCPressed) {
                                wasCPressed = true;
                                PacketByteBuf dockBuf = PacketByteBufs.create();
                                dockBuf.writeUuid(pairedUuid);
                                ClientPlayNetworking.send(EvecualMC.RC_CAR_AUTO_DOCK_PACKET_ID, dockBuf);
                            }
                        }

                        if (client.player.squaredDistanceTo(targetRc) <= 4096.0) {
                            boolean rcFwd = client.options.forwardKey.isPressed();
                            boolean rcBack = client.options.backKey.isPressed();
                            boolean rcLeft = client.options.leftKey.isPressed();
                            boolean rcRight = client.options.rightKey.isPressed();
                            boolean rcSprint = client.options.sprintKey.isPressed();
                            boolean rcJump = client.options.jumpKey.isPressed();

                            targetRc.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcSprint, rcJump);

                            PacketByteBuf rcBuf = PacketByteBufs.create();
                            rcBuf.writeUuid(pairedUuid);
                            rcBuf.writeBoolean(rcFwd);
                            rcBuf.writeBoolean(rcBack);
                            rcBuf.writeBoolean(rcLeft);
                            rcBuf.writeBoolean(rcRight);
                            rcBuf.writeBoolean(rcSprint);
                            rcBuf.writeBoolean(rcJump);
                            ClientPlayNetworking.send(EvecualMC.RC_CAR_INPUT_PACKET_ID, rcBuf);

                            if (client.player.age % 10 == 0) {
                                boolean isCamView = client.getCameraEntity() == targetRc;
                                String camPrompt = isCamView ? "Arrows: Orbit Cam | F: Player View" : "F: RC Camera";
                                client.player.sendMessage(Text.literal("§b📡 RC CAR: §a" + targetRc.getEnergy() + " E §7| §eRange: " + (int)client.player.distanceTo(targetRc) + "m §8| §f[" + camPrompt + ", C: Charger]"), true);
                            }
                        }
                    }
                }

                // Camera orientation control via Arrow Keys while looking through RC Car camera
                if (client.getCameraEntity() instanceof com.evecual.evecualmc.entity.RcCarEntity) {
                    if (client.currentScreen == null) {
                        long windowHandle = client.getWindow().getHandle();
                        boolean left = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_LEFT);
                        boolean right = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT);
                        boolean up = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_UP);
                        boolean down = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_DOWN);

                        if (left) rcCameraYaw -= 3.5f;
                        if (right) rcCameraYaw += 3.5f;
                        if (up) rcCameraPitch = net.minecraft.util.math.MathHelper.clamp(rcCameraPitch - 2.5f, -75.0f, 75.0f);
                        if (down) rcCameraPitch = net.minecraft.util.math.MathHelper.clamp(rcCameraPitch + 2.5f, -75.0f, 75.0f);
                    }
                }

                // Reset camera if RC link is disabled or target car is invalid
                if (client.getCameraEntity() instanceof com.evecual.evecualmc.entity.RcCarEntity rc) {
                    boolean valid = isRcActive && rc.isAlive() && !rc.isRemoved() && client.player.squaredDistanceTo(rc) <= 4096.0;
                    if (!valid) {
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective);
                    }
                }

                // Normal car auto park if not in RC mode
                if (!isRcActive) {
                    if (cDown) {
                        if (!wasCPressed) {
                            wasCPressed = true;
                            ClientPlayNetworking.send(EvecualMC.AUTO_PARK_PACKET_ID, PacketByteBufs.empty());
                        }
                    } else {
                        wasCPressed = false;
                    }
                } else if (!cDown) {
                    wasCPressed = false;
                }
            }
        });

        LOGGER.info("EvecualMC client initialized with Trunk Screen, Keybindings, and Color Variants!");
    }
}
