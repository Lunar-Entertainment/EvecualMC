package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.client.screen.ElectronicCombinerScreen;
import com.evecual.evecualmc.client.screen.CarTrunkScreen;
import com.evecual.evecualmc.client.render.CarEntityModel;
import com.evecual.evecualmc.client.render.CarEntityRenderer;
import com.evecual.evecualmc.client.render.RcCarEntityModel;
import com.evecual.evecualmc.client.render.RcCarEntityRenderer;
import com.evecual.evecualmc.client.render.RcDroneEntityModel;
import com.evecual.evecualmc.client.render.RcDroneEntityRenderer;
import com.evecual.evecualmc.client.render.RcRobotEntityModel;
import com.evecual.evecualmc.client.render.RcRobotEntityRenderer;
import com.evecual.evecualmc.client.render.HeliEntityModel;
import com.evecual.evecualmc.client.render.HeliEntityRenderer;
import com.evecual.evecualmc.client.render.WindTurbineBlockEntityRenderer;
import com.evecual.evecualmc.entity.CarEntity;
import com.evecual.evecualmc.entity.HeliEntity;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class EvecualMCClient implements ClientModInitializer {
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

    public static final KeyBinding TOGGLE_LIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.toggle_light",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding OPEN_TIP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.open_tips",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding HELI_LEFT_ARM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.heli_left_arm",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding HELI_RIGHT_ARM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.heli_right_arm",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding RC_PERSPECTIVE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.toggle_rc_perspective",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_WORLD_1,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding HELI_WEAPON_UP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.heli_weapon_up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "category.evecualmc.evecual"
    ));

    public static final KeyBinding HELI_WEAPON_DOWN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evecualmc.heli_weapon_down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "category.evecualmc.evecual"
    ));

    public static boolean isKeyOrMousePressed(KeyBinding keyBinding, long windowHandle) {
        if (keyBinding == null) return false;
        if (keyBinding.isPressed()) return true;
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(keyBinding);
        if (boundKey.getCategory() == InputUtil.Type.KEYSYM) {
            int code = boundKey.getCode();
            if (code != GLFW.GLFW_KEY_UNKNOWN && InputUtil.isKeyPressed(windowHandle, code)) {
                return true;
            }
        } else if (boundKey.getCategory() == InputUtil.Type.MOUSE) {
            int button = boundKey.getCode();
            if (button >= 0 && GLFW.glfwGetMouseButton(windowHandle, button) == GLFW.GLFW_PRESS) {
                return true;
            }
        }
        return false;
    }

    private static boolean wasCPressed = false;
    private static boolean wasPerspectivePressed = false;
    private static boolean wasAttackPressed = false;
    private static boolean wasUsePressed = false;
    private static boolean wasLeftArmPressed = false;
    private static boolean wasRightArmPressed = false;
    private static int robotAttackCooldown = 0;
    private static int robotPlaceCooldown = 0;
    private static int heliLeftArmCooldown = 0;
    private static int heliRightArmCooldown = 0;
    private static Perspective previousPerspective = Perspective.FIRST_PERSON;

    // Camera angles with exponential smoothing for ultra-fluid mouse orbiting
    private static float targetRcCameraYaw = 0.0F;
    private static float targetRcCameraPitch = 12.0F;
    private static float smoothRcCameraYaw = 0.0F;
    private static float smoothRcCameraPitch = 12.0F;

    // Camera distance & optical FOV zoom
    private static float rcCameraDistance = 3.5F;
    private static float targetRcCameraDistance = 3.5F;
    private static float rcFpZoom = 1.0F;
    private static float targetRcFpZoom = 1.0F;

    public static float getRcCameraDistance() {
        rcCameraDistance = MathHelper.lerp(0.3F, rcCameraDistance, targetRcCameraDistance);
        return rcCameraDistance;
    }

    public static float getRcFpZoom() {
        rcFpZoom = MathHelper.lerp(0.3F, rcFpZoom, targetRcFpZoom);
        return rcFpZoom;
    }

    public static void onRcCameraScroll(double vertical) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        boolean isFirstPerson = client.options.getPerspective().isFirstPerson();
        if (isFirstPerson) {
            // Optical FOV zoom in First-Person mode (1.0x to 5.0x)
            targetRcFpZoom = MathHelper.clamp(targetRcFpZoom + (float) (vertical * 0.35F), 1.0F, 5.0F);
        } else {
            // Third-Person orbit camera distance (1.0m to 12.0m)
            targetRcCameraDistance = MathHelper.clamp(targetRcCameraDistance - (float) (vertical * 0.6F), 1.0F, 12.0F);
        }
    }

    public static void sendSafeActionBar(MinecraftClient client, String text) {
        if (client == null || client.player == null) return;
        int maxPixelWidth = Math.max(100, client.getWindow().getScaledWidth() - 24);
        if (client.textRenderer != null && client.textRenderer.getWidth(text) > maxPixelWidth) {
            String trimmed = client.textRenderer.trimToWidth(text, maxPixelWidth - 10) + "…";
            client.player.sendMessage(Text.literal(trimmed), true);
        } else {
            client.player.sendMessage(Text.literal(text), true);
        }
    }

    public static void toggleRcPerspective(MinecraftClient client) {
        if (client == null || !isRcCameraActive() || client.player == null) return;
        if (client.options.getPerspective().isFirstPerson()) {
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            sendSafeActionBar(client, "§b📷 RC Camera: §eTHIRD PERSON §7[<: FPV, Scroll: Zoom]");
        } else {
            client.options.setPerspective(Perspective.FIRST_PERSON);
            targetRcCameraYaw = 0.0F;
            smoothRcCameraYaw = 0.0F;
            sendSafeActionBar(client, "§b📷 RC Camera: §aFIRST PERSON (FPV) §7[<: 3RD, Scroll: Zoom]");
        }
    }

    public static boolean isRcVehicleLightOn() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;
        Entity cam = client.getCameraEntity();
        if (cam instanceof RcCarEntity car) return car.isLightOn();
        if (cam instanceof RcDroneEntity drone) return drone.isLightOn();
        if (cam instanceof RcRobotEntity robot) return robot.isLightOn();
        return false;
    }

    public static void toggleRcLight(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;
        Entity target = null;
        if (isRcCameraActive()) {
            target = client.getCameraEntity();
        }
        if (target == null) {
            target = getTargetRcCar(client);
        }
        if (target == null) {
            target = getTargetRcDrone(client);
        }
        if (target == null) {
            target = getTargetRcRobot(client);
        }
        if (target == null && client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit) {
            Entity e = hit.getEntity();
            if (e instanceof RcCarEntity || e instanceof RcDroneEntity || e instanceof RcRobotEntity) {
                target = e;
            }
        }
        if (target != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(target.getUuid());
            ClientPlayNetworking.send(EvecualMC.TOGGLE_RC_LIGHT_PACKET_ID, buf);
        }
    }

    public static float getRcCameraYaw() {
        if (Math.abs(smoothRcCameraYaw - targetRcCameraYaw) < 0.01F) {
            smoothRcCameraYaw = targetRcCameraYaw;
        } else {
            smoothRcCameraYaw = MathHelper.lerp(0.25F, smoothRcCameraYaw, targetRcCameraYaw);
        }
        return smoothRcCameraYaw;
    }

    public static float getRcCameraPitch() {
        if (Math.abs(smoothRcCameraPitch - targetRcCameraPitch) < 0.01F) {
            smoothRcCameraPitch = targetRcCameraPitch;
        } else {
            smoothRcCameraPitch = MathHelper.lerp(0.25F, smoothRcCameraPitch, targetRcCameraPitch);
        }
        return smoothRcCameraPitch;
    }

    public static void onRcMouseTurn(double cursorDeltaX, double cursorDeltaY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && isRcCameraActive() && client.options.getPerspective().isFirstPerson()) {
            Entity cam = client.getCameraEntity();
            float yawDelta = (float) (cursorDeltaX * 0.15);

            if (cam instanceof RcDroneEntity drone) {
                if (drone.isAutoReturning()) {
                    targetRcCameraYaw = 0.0F;
                    smoothRcCameraYaw = 0.0F;
                    targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
                    return;
                }
                float newYaw = MathHelper.wrapDegrees(drone.getYaw() + yawDelta);
                drone.setYaw(newYaw);
                drone.prevYaw += yawDelta;
                drone.setBodyYaw(newYaw);
                drone.setHeadYaw(newYaw);
                targetRcCameraYaw = 0.0F;
                smoothRcCameraYaw = 0.0F;
                targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
                return;
            } else if (cam instanceof RcRobotEntity robot) {
                if (robot.isAutoReturning()) {
                    targetRcCameraYaw = 0.0F;
                    smoothRcCameraYaw = 0.0F;
                    targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
                    robot.setHeadPitch(targetRcCameraPitch);
                    return;
                }
                float newYaw = MathHelper.wrapDegrees(robot.getYaw() + yawDelta);
                robot.setYaw(newYaw);
                robot.prevYaw += yawDelta;
                robot.setBodyYaw(newYaw);
                robot.setHeadYaw(newYaw);
                targetRcCameraYaw = 0.0F;
                smoothRcCameraYaw = 0.0F;
                targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
                robot.setHeadPitch(targetRcCameraPitch);
                return;
            } else if (cam instanceof RcCarEntity car) {
                if (car.isAutoReturning()) {
                    targetRcCameraYaw = 0.0F;
                    smoothRcCameraYaw = 0.0F;
                    targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
                    return;
                }
                float newYaw = MathHelper.wrapDegrees(car.getYaw() + yawDelta);
                car.setYaw(newYaw);
                car.prevYaw += yawDelta;
                car.setBodyYaw(newYaw);
                car.setHeadYaw(newYaw);
                targetRcCameraYaw = 0.0F;
                smoothRcCameraYaw = 0.0F;
                targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
                return;
            }
        }
        targetRcCameraYaw += (float) (cursorDeltaX * 0.15);
        targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + (float) (cursorDeltaY * 0.15), -80.0F, 80.0F);
    }

    public static void resetRcCameraAngle() {
        targetRcCameraYaw = 0.0F;
        targetRcCameraPitch = 12.0F;
        smoothRcCameraYaw = 0.0F;
        smoothRcCameraPitch = 12.0F;
    }

    public static boolean isRcCameraActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;
        Entity cam = client.getCameraEntity();
        return cam instanceof RcCarEntity || cam instanceof RcDroneEntity || cam instanceof RcRobotEntity || cam instanceof HeliEntity;
    }

    public static BlockPos activeStationPos = null;
    public static java.util.UUID activeStationVehicleUuid = null;
    public static String activeStationType = null;

    public static boolean isRcLinkActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.currentScreen != null) {
            return false;
        }
        if (activeStationPos != null && activeStationVehicleUuid != null) {
            return true;
        }
        net.minecraft.item.ItemStack held = null;
        if (client.player.getMainHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM) || client.player.getMainHandStack().isOf(EvecualMC.HELI_CONTROLLER_ITEM)) {
            held = client.player.getMainHandStack();
        } else if (client.player.getOffHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM) || client.player.getOffHandStack().isOf(EvecualMC.HELI_CONTROLLER_ITEM)) {
            held = client.player.getOffHandStack();
        }
        if (held != null && held.hasNbt()) {
            net.minecraft.nbt.NbtCompound nbt = held.getNbt();
            return nbt != null && (nbt.containsUuid("PairedCar") || nbt.containsUuid("PairedDrone") || nbt.containsUuid("PairedRobot") || nbt.containsUuid("PairedHeli")) && nbt.getBoolean("ActiveLink");
        }
        return false;
    }

    public static HeliEntity getTargetHeli(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return null;
        net.minecraft.item.ItemStack held = null;
        if (client.player.getMainHandStack().isOf(EvecualMC.HELI_CONTROLLER_ITEM)) {
            held = client.player.getMainHandStack();
        } else if (client.player.getOffHandStack().isOf(EvecualMC.HELI_CONTROLLER_ITEM)) {
            held = client.player.getOffHandStack();
        }
        if (held == null || !held.hasNbt()) return null;
        net.minecraft.nbt.NbtCompound nbt = held.getNbt();
        if (nbt == null || !nbt.containsUuid("PairedHeli")) return null;
        java.util.UUID pairedUuid = nbt.getUuid("PairedHeli");

        for (Entity e : client.world.getEntities()) {
            if (e instanceof HeliEntity heli && heli.getUuid().equals(pairedUuid)) {
                return heli;
            }
        }
        return null;
    }

    public static RcCarEntity getTargetRcCar(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return null;
        if (activeStationPos != null && activeStationVehicleUuid != null && "car".equals(activeStationType)) {
            for (Entity e : client.world.getEntities()) {
                if (e instanceof RcCarEntity rc && rc.getUuid().equals(activeStationVehicleUuid)) {
                    return rc;
                }
            }
            return null;
        }
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
            if (e instanceof RcCarEntity rc && rc.getUuid().equals(pairedUuid)) {
                return rc;
            }
        }
        return null;
    }

    public static RcDroneEntity getTargetRcDrone(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return null;
        if (activeStationPos != null && activeStationVehicleUuid != null && "drone".equals(activeStationType)) {
            for (Entity e : client.world.getEntities()) {
                if (e instanceof RcDroneEntity drone && drone.getUuid().equals(activeStationVehicleUuid)) {
                    return drone;
                }
            }
            return null;
        }
        net.minecraft.item.ItemStack held = null;
        if (client.player.getMainHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getMainHandStack();
        } else if (client.player.getOffHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getOffHandStack();
        }
        if (held == null || !held.hasNbt()) return null;
        net.minecraft.nbt.NbtCompound nbt = held.getNbt();
        if (nbt == null || !nbt.containsUuid("PairedDrone")) return null;
        java.util.UUID pairedUuid = nbt.getUuid("PairedDrone");

        for (Entity e : client.world.getEntities()) {
            if (e instanceof RcDroneEntity drone && drone.getUuid().equals(pairedUuid)) {
                return drone;
            }
        }
        return null;
    }

    public static RcRobotEntity getTargetRcRobot(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return null;
        if (activeStationPos != null && activeStationVehicleUuid != null && "robot".equals(activeStationType)) {
            for (Entity e : client.world.getEntities()) {
                if (e instanceof RcRobotEntity robot && robot.getUuid().equals(activeStationVehicleUuid)) {
                    return robot;
                }
            }
            return null;
        }
        net.minecraft.item.ItemStack held = null;
        if (client.player.getMainHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getMainHandStack();
        } else if (client.player.getOffHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)) {
            held = client.player.getOffHandStack();
        }
        if (held == null || !held.hasNbt()) return null;
        net.minecraft.nbt.NbtCompound nbt = held.getNbt();
        if (nbt == null || !nbt.containsUuid("PairedRobot")) return null;
        java.util.UUID pairedUuid = nbt.getUuid("PairedRobot");

        for (Entity e : client.world.getEntities()) {
            if (e instanceof RcRobotEntity robot && robot.getUuid().equals(pairedUuid)) {
                return robot;
            }
        }
        return null;
    }

    public static void toggleRcCamera(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        if (activeStationPos != null) {
            // In stationary controller mode, exiting camera exits the station back to player control!
            ClientPlayNetworking.send(EvecualMC.EXIT_RC_STATION_PACKET_ID, PacketByteBufs.empty());
            activeStationPos = null;
            activeStationVehicleUuid = null;
            activeStationType = null;
            client.setCameraEntity(client.player);
            client.options.setPerspective(previousPerspective != null ? previousPerspective : Perspective.FIRST_PERSON);
            sendSafeActionBar(client, "§7📡 Disconnected from RC Control Station.");
            return;
        }

        Entity targetRc = getTargetRcCar(client);
        if (targetRc == null) {
            targetRc = getTargetRcDrone(client);
        }
        if (targetRc == null) {
            targetRc = getTargetRcRobot(client);
        }
        if (targetRc == null) {
            targetRc = getTargetHeli(client);
        }

        if (targetRc == null) {
            client.player.sendMessage(Text.literal("§c📷 No linked RC Vehicle or EV Heli found in range!"), true);
            return;
        }

        if (client.getCameraEntity() == targetRc) {
            client.setCameraEntity(client.player);
            client.options.setPerspective(previousPerspective);
            targetRcFpZoom = 1.0F;
            rcFpZoom = 1.0F;
            sendSafeActionBar(client, "§7📷 RC Camera: §cDISABLED §7[Player View]");
        } else {
            resetRcCameraAngle();
            targetRcCameraDistance = (targetRc instanceof HeliEntity) ? 6.5F : 3.5F;
            rcCameraDistance = targetRcCameraDistance;
            targetRcFpZoom = 1.0F;
            rcFpZoom = 1.0F;
            previousPerspective = client.options.getPerspective();
            client.setCameraEntity(targetRc);
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            String name = (targetRc instanceof HeliEntity) ? "Heli" : (targetRc instanceof RcDroneEntity) ? "Drone" : (targetRc instanceof RcRobotEntity) ? "Robot" : "Car";
            sendSafeActionBar(client, "§b📷 " + name + " Cam: §aACTIVE §7[<: View | F: Exit]");
        }
    }

    @Override
    public void onInitializeClient() {
        // Register HUD tip overlay for Solar Panel, Battery, Combiner, Charger, and Car
        HudRenderCallback.EVENT.register(new EnergyHudOverlay());

        // Register Wire Cutout Render Layer & Wind Turbine Renderer
        BlockRenderLayerMap.INSTANCE.putBlock(EvecualMC.WIRE_BLOCK, RenderLayer.getCutout());
        BlockEntityRendererFactories.register(EvecualMC.WIND_TURBINE_BLOCK_ENTITY, WindTurbineBlockEntityRenderer::new);

        // Register Screens
        HandledScreens.register(EvecualMC.ELECTRONIC_COMBINER_SCREEN_HANDLER, ElectronicCombinerScreen::new);
        HandledScreens.register(EvecualMC.ELECTRONIC_DUPER_SCREEN_HANDLER, com.evecual.evecualmc.client.screen.ElectronicDuperScreen::new);
        HandledScreens.register(EvecualMC.CAR_TRUNK_SCREEN_HANDLER, CarTrunkScreen::new);
        HandledScreens.register(EvecualMC.HELI_UPGRADE_SCREEN_HANDLER, com.evecual.evecualmc.client.screen.HeliUpgradeScreen::new);

        // Register Car Entity Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(CarEntityModel.MODEL_LAYER, CarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.CAR_ENTITY, CarEntityRenderer::new);

        // Register RC Car Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(RcCarEntityModel.MODEL_LAYER, RcCarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_CAR_ENTITY, RcCarEntityRenderer::new);

        // Register RC Drone Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(RcDroneEntityModel.MODEL_LAYER, RcDroneEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_DRONE_ENTITY, RcDroneEntityRenderer::new);

        // Register RC Robot Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(RcRobotEntityModel.MODEL_LAYER, RcRobotEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_ROBOT_ENTITY, RcRobotEntityRenderer::new);

        // Register EV Heli Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(HeliEntityModel.MODEL_LAYER, HeliEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.HELI_ENTITY, HeliEntityRenderer::new);

        // Explicitly render the Player in the world when looking through RC Camera view
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null && isRcCameraActive() && client.world != null) {
                Camera camera = context.camera();
                Vec3d camPos = camera.getPos();
                float tickDelta = context.tickDelta();

                double x = MathHelper.lerp((double) tickDelta, client.player.prevX, client.player.getX()) - camPos.x;
                double y = MathHelper.lerp((double) tickDelta, client.player.prevY, client.player.getY()) - camPos.y;
                double z = MathHelper.lerp((double) tickDelta, client.player.prevZ, client.player.getZ()) - camPos.z;
                float yaw = MathHelper.lerp(tickDelta, client.player.prevYaw, client.player.getYaw());
                int light = client.getEntityRenderDispatcher().getLight(client.player, tickDelta);

                context.matrixStack().push();
                client.getEntityRenderDispatcher().render(
                        client.player,
                        x, y, z,
                        yaw,
                        tickDelta,
                        context.matrixStack(),
                        context.consumers(),
                        light
                );
                context.matrixStack().pop();
            }
        });

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

        // Register EV Heli Auto-Park S2C sync
        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.START_HELI_AUTO_PARK_S2C_PACKET_ID, (client, handler, buf, responseSender) -> {
            int heliId = buf.readInt();
            int hx = buf.readInt();
            int hy = buf.readInt();
            int hz = buf.readInt();
            client.execute(() -> {
                if (client.world != null && client.world.getEntityById(heliId) instanceof HeliEntity heli) {
                    heli.startAutoPark(new net.minecraft.util.math.BlockPos(hx, hy, hz));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.CANCEL_HELI_AUTO_PARK_S2C_PACKET_ID, (client, handler, buf, responseSender) -> {
            int heliId = buf.readInt();
            client.execute(() -> {
                if (client.world != null && client.world.getEntityById(heliId) instanceof HeliEntity heli) {
                    heli.cancelAutoPark(null);
                }
            });
        });

        // Register Open Tip Menu / Field Guide packet receiver
        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.OPEN_TIP_SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
            String topicId = buf.readString();
            int energy = buf.readInt();
            int maxEnergy = buf.readInt();
            String status = buf.readString();
            client.execute(() -> {
                com.evecual.evecualmc.client.screen.TipTopic topic = com.evecual.evecualmc.client.screen.TipTopic.fromId(topicId);
                client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(topic, energy, maxEnergy, status));
            });
        });

        // Register Enter RC Station S2C receiver
        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.ENTER_RC_STATION_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos stationPos = buf.readBlockPos();
            java.util.UUID vehicleUuid = buf.readUuid();
            String type = buf.readString();

            client.execute(() -> {
                activeStationPos = stationPos;
                activeStationVehicleUuid = vehicleUuid;
                activeStationType = type;

                Entity vehicle = null;
                if (client.world != null) {
                    for (Entity e : client.world.getEntities()) {
                        if (e.getUuid().equals(vehicleUuid)) {
                            vehicle = e;
                            break;
                        }
                    }
                }

                if (vehicle != null) {
                    previousPerspective = client.options.getPerspective();
                    client.setCameraEntity(vehicle);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    resetRcCameraAngle();
                    targetRcCameraDistance = 3.5F;
                    rcCameraDistance = 3.5F;
                    sendSafeActionBar(client, "§b🖥️ RC Station: §aLINKED §7[Shift/Sneak: Exit]");
                }
            });
        });

        // Register Exit RC Station S2C receiver
        ClientPlayNetworking.registerGlobalReceiver(EvecualMC.EXIT_RC_STATION_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if (activeStationPos != null) {
                    activeStationPos = null;
                    activeStationVehicleUuid = null;
                    activeStationType = null;
                    if (client.player != null) {
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective != null ? previousPerspective : Perspective.FIRST_PERSON);
                        sendSafeActionBar(client, "§7📡 Disconnected from RC Control Station.");
                    }
                }
            });
        });

        // Client Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RcHudManager.tick();
            if (client.player != null) {
                if (robotAttackCooldown > 0) {
                    robotAttackCooldown--;
                }
                if (robotPlaceCooldown > 0) {
                    robotPlaceCooldown--;
                }
                if (heliLeftArmCooldown > 0) {
                    heliLeftArmCooldown--;
                }
                if (heliRightArmCooldown > 0) {
                    heliRightArmCooldown--;
                }

                // Toggle RC Perspective Key ('<' by default, also checks physical < / comma key)
                boolean perspectiveDown = isKeyOrMousePressed(RC_PERSPECTIVE_KEY, client.getWindow().getHandle())
                        || (isRcCameraActive() && (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_WORLD_1) || InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_COMMA)));
                if (perspectiveDown) {
                    if (!wasPerspectivePressed && client.currentScreen == null) {
                        wasPerspectivePressed = true;
                        toggleRcPerspective(client);
                    }
                } else {
                    wasPerspectivePressed = false;
                }

                // Open Trunk / Cargo Key ('Z')
                while (OPEN_TRUNK_KEY.wasPressed()) {
                    // 1. If inside car or heli
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(car.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        continue;
                    }
                    if (client.player.getVehicle() instanceof HeliEntity heli) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(heli.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        continue;
                    }

                    // 2. RC Robot (when controlling, in camera view, or paired)
                    RcRobotEntity targetRobot = getTargetRcRobot(client);
                    if (targetRobot != null && (client.getCameraEntity() == targetRobot || isRcLinkActive())) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(targetRobot.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        continue;
                    }

                    // 3. RC Drone
                    RcDroneEntity targetDrone = getTargetRcDrone(client);
                    if (targetDrone != null && (client.getCameraEntity() == targetDrone || isRcLinkActive())) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(targetDrone.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        continue;
                    }

                    // 4. RC Car
                    RcCarEntity targetRc = getTargetRcCar(client);
                    if (targetRc != null && (client.getCameraEntity() == targetRc || isRcLinkActive())) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(targetRc.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        continue;
                    }

                    // 5. When NOT controlling: Check if aiming directly at an entity with crosshair
                    if (client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit && hit.getEntity() != null) {
                        Entity e = hit.getEntity();
                        if (e instanceof RcRobotEntity || e instanceof RcDroneEntity || e instanceof RcCarEntity || e instanceof CarEntity || e instanceof HeliEntity) {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(e.getId());
                            ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                            continue;
                        }
                    }

                    // 6. When NOT controlling: Find nearest vehicle within 6 blocks
                    if (client.world != null) {
                        net.minecraft.util.math.Box searchBox = client.player.getBoundingBox().expand(6.0);
                        java.util.List<Entity> nearEntities = client.world.getOtherEntities(client.player, searchBox,
                                e -> e instanceof RcRobotEntity || e instanceof RcDroneEntity || e instanceof RcCarEntity || e instanceof CarEntity || e instanceof HeliEntity);
                        if (!nearEntities.isEmpty()) {
                            nearEntities.sort(java.util.Comparator.comparingDouble(e -> e.squaredDistanceTo(client.player)));
                            Entity closest = nearEntities.get(0);
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(closest.getId());
                            ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        }
                    }
                }

                // Attach Cable / Open Heli Upgrade Key ('X')
                while (ATTACH_CABLE_KEY.wasPressed()) {
                    Entity targetHeli = null;
                    if (client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit && hit.getEntity() instanceof HeliEntity heli) {
                        targetHeli = heli;
                    } else if (client.player != null && client.world != null) {
                        net.minecraft.util.math.Vec3d eyePos = client.player.getEyePos();
                        net.minecraft.util.math.Vec3d lookVec = client.player.getRotationVec(1.0F);
                        for (HeliEntity heli : client.world.getEntitiesByClass(HeliEntity.class, client.player.getBoundingBox().expand(8.0), Entity::isAlive)) {
                            net.minecraft.util.math.Vec3d toEntity = heli.getBoundingBox().getCenter().subtract(eyePos).normalize();
                            if (lookVec.dotProduct(toEntity) > 0.35 && client.player.squaredDistanceTo(heli) < 64.0) {
                                targetHeli = heli;
                                break;
                            }
                        }
                    }

                    if (targetHeli != null) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(targetHeli.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_HELI_UPGRADE_PACKET_ID, buf);
                    } else {
                        ClientPlayNetworking.send(EvecualMC.TOGGLE_CABLE_PACKET_ID, PacketByteBufs.empty());
                    }
                }

                // Auto Park Key (for full-size Car & EV Heli)
                while (AUTO_PARK_KEY.wasPressed()) {
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(car.getId());
                        ClientPlayNetworking.send(EvecualMC.AUTO_PARK_PACKET_ID, buf);
                    } else if (client.player.getVehicle() instanceof HeliEntity heli) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(heli.getId());
                        ClientPlayNetworking.send(EvecualMC.AUTO_PARK_PACKET_ID, buf);
                    }
                }

                // RC Camera Toggle Key ('F') / Heli Dismount Key
                while (RC_CAMERA_KEY.wasPressed()) {
                    if (client.player.getVehicle() instanceof HeliEntity) {
                        ClientPlayNetworking.send(EvecualMC.DISMOUNT_HELI_PACKET_ID, PacketByteBufs.empty());
                    } else {
                        toggleRcCamera(client);
                    }
                }

                // RC Vehicle Light Toggle Key ('L')
                while (TOGGLE_LIGHT_KEY.wasPressed()) {
                    toggleRcLight(client);
                }

                // Field Guide / Tip Menu Shortcut Key ('H')
                while (OPEN_TIP_KEY.wasPressed()) {
                    openTipScreenContextual(client);
                }

                // Full-size Car driving inputs
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

                // EV Helicopter flight inputs, arm actions & sneak dismount suppression
                if (client.player.getVehicle() instanceof HeliEntity heli) {
                    // Suppress vanilla sneak dismount so Shift ONLY acts as vertical descent (Go Down)
                    if (client.player.input != null) {
                        client.player.input.sneaking = false;
                    }

                    boolean forward = client.options.forwardKey.isPressed();
                    boolean back = client.options.backKey.isPressed();
                    boolean left = client.options.leftKey.isPressed();
                    boolean right = client.options.rightKey.isPressed();
                    boolean up = client.options.jumpKey.isPressed();
                    boolean down = client.options.sneakKey.isPressed();
                    boolean sprint = client.options.sprintKey.isPressed() || InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);

                    heli.setInputs(forward, back, left, right, up, down, sprint);

                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeBoolean(forward);
                    buf.writeBoolean(back);
                    buf.writeBoolean(left);
                    buf.writeBoolean(right);
                    buf.writeBoolean(up);
                    buf.writeBoolean(down);
                    buf.writeBoolean(sprint);
                    ClientPlayNetworking.send(EvecualMC.HELI_INPUT_PACKET_ID, buf);

                    // Hardpoint Weapon Angling (-60° to +30°)
                    long wHandle = client.getWindow().getHandle();
                    boolean upAngle = isKeyOrMousePressed(HELI_WEAPON_UP_KEY, wHandle) || InputUtil.isKeyPressed(wHandle, GLFW.GLFW_KEY_UP);
                    boolean downAngle = isKeyOrMousePressed(HELI_WEAPON_DOWN_KEY, wHandle) || InputUtil.isKeyPressed(wHandle, GLFW.GLFW_KEY_DOWN);
                    if (upAngle || downAngle) {
                        float curAngle = heli.getWeaponAngle();
                        float newAngle = curAngle;
                        if (upAngle) newAngle += 1.5F;
                        if (downAngle) newAngle -= 1.5F;
                        newAngle = MathHelper.clamp(newAngle, -60.0F, 30.0F);
                        if (Math.abs(newAngle - curAngle) > 0.01F) {
                            heli.setWeaponAngle(newAngle);
                            PacketByteBuf angleBuf = PacketByteBufs.create();
                            angleBuf.writeInt(heli.getId());
                            angleBuf.writeFloat(newAngle);
                            ClientPlayNetworking.send(EvecualMC.HELI_WEAPON_ANGLE_PACKET_ID, angleBuf);
                            sendSafeActionBar(client, String.format("§6🎯 Hardpoint Angle: §e%.1f° §7(Up/Down Arrow)", newAngle));
                        }
                    }

                    // Left Arm & Right Arm: Fire hardpoint arms while in flight (allows same keybind!)
                    boolean iDown = isKeyOrMousePressed(HELI_LEFT_ARM_KEY, wHandle) || InputUtil.isKeyPressed(wHandle, GLFW.GLFW_KEY_I);
                    boolean oDown = isKeyOrMousePressed(HELI_RIGHT_ARM_KEY, wHandle) || InputUtil.isKeyPressed(wHandle, GLFW.GLFW_KEY_O);

                    if (iDown) {
                        if (!wasLeftArmPressed || heliLeftArmCooldown <= 0) {
                            wasLeftArmPressed = true;
                            heliLeftArmCooldown = 4;
                            PacketByteBuf armBuf = PacketByteBufs.create();
                            armBuf.writeInt(0); // 0 = Left Arm
                            ClientPlayNetworking.send(EvecualMC.HELI_ARM_ACTION_PACKET_ID, armBuf);
                        }
                    } else {
                        wasLeftArmPressed = false;
                    }

                    if (oDown) {
                        if (!wasRightArmPressed || heliRightArmCooldown <= 0) {
                            wasRightArmPressed = true;
                            heliRightArmCooldown = 4;
                            PacketByteBuf armBuf = PacketByteBufs.create();
                            armBuf.writeInt(1); // 1 = Right Arm
                            ClientPlayNetworking.send(EvecualMC.HELI_ARM_ACTION_PACKET_ID, armBuf);
                        }
                    } else {
                        wasRightArmPressed = false;
                    }
                }

                // Stationary RC Controller handling
                if (activeStationPos != null) {
                    if (client.world == null || client.player == null ||
                        client.player.squaredDistanceTo(activeStationPos.getX() + 0.5, activeStationPos.getY() + 0.5, activeStationPos.getZ() + 0.5) > 36.0 ||
                        !client.world.getBlockState(activeStationPos).isOf(EvecualMC.STATIONARY_RC_CONTROLLER_BLOCK)) {
                        ClientPlayNetworking.send(EvecualMC.EXIT_RC_STATION_PACKET_ID, PacketByteBufs.empty());
                        activeStationPos = null;
                        activeStationVehicleUuid = null;
                        activeStationType = null;
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective != null ? previousPerspective : Perspective.FIRST_PERSON);
                    } else {
                        // 1. Lock player movement: stuck in place at terminal
                        client.player.setVelocity(0, client.player.getVelocity().y, 0);
                        if (client.player.input != null) {
                            client.player.input.movementForward = 0.0F;
                            client.player.input.movementSideways = 0.0F;
                            client.player.input.jumping = false;
                        }

                        // 2. Enforce RC Camera view: cannot be in player perspective
                        Entity targetRc = getTargetRcCar(client);
                        if (targetRc == null) targetRc = getTargetRcDrone(client);
                        if (targetRc == null) targetRc = getTargetRcRobot(client);

                        if (targetRc != null && targetRc.isAlive()) {
                            if (client.getCameraEntity() != targetRc) {
                                client.setCameraEntity(targetRc);
                            }
                            if (client.options.getPerspective() == Perspective.FIRST_PERSON && client.getCameraEntity() == client.player) {
                                client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                            }
                        }
                    }
                }

                // Remote Control: RC Car, RC Drone, and RC Robot inputs
                boolean isRcActive = isRcLinkActive();
                long windowHandle = client.getWindow().getHandle();
                boolean cDown = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_C);

                if (!cDown) {
                    wasCPressed = false;
                }

                if (isRcActive) {
                    // --- 1. RC Robot Handling ---
                    RcRobotEntity targetRobot = getTargetRcRobot(client);
                    if (targetRobot != null) {
                        java.util.UUID pairedUuid = targetRobot.getUuid();

                        // 'C' Key: Auto Return to RC Charger / parking spot
                        if (cDown && !wasCPressed) {
                            wasCPressed = true;
                            PacketByteBuf dockBuf = PacketByteBufs.create();
                            dockBuf.writeUuid(pairedUuid);
                            ClientPlayNetworking.send(EvecualMC.RC_ROBOT_AUTO_DOCK_PACKET_ID, dockBuf);
                        }

                        if (client.player.squaredDistanceTo(targetRobot) <= 65536.0) { // 256m
                            boolean rcFwd = client.options.forwardKey.isPressed();
                            boolean rcBack = client.options.backKey.isPressed();
                            boolean rcLeft = client.options.leftKey.isPressed();
                            boolean rcRight = client.options.rightKey.isPressed();
                            boolean rcSprint = client.options.sprintKey.isPressed();
                            boolean rcJump = client.options.jumpKey.isPressed();

                            boolean hasManualMove = rcFwd || rcBack || rcLeft || rcRight || rcJump;

                            if (!targetRobot.isAutoReturning() || hasManualMove) {
                                targetRobot.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcSprint, rcJump);

                                PacketByteBuf robotBuf = PacketByteBufs.create();
                                robotBuf.writeUuid(pairedUuid);
                                robotBuf.writeBoolean(rcFwd);
                                robotBuf.writeBoolean(rcBack);
                                robotBuf.writeBoolean(rcLeft);
                                robotBuf.writeBoolean(rcRight);
                                robotBuf.writeBoolean(rcSprint);
                                robotBuf.writeBoolean(rcJump);
                                robotBuf.writeFloat(targetRobot.getYaw());
                                ClientPlayNetworking.send(EvecualMC.RC_ROBOT_INPUT_PACKET_ID, robotBuf);
                            }

                            // LMB: Right Arm (Weapon / Tool Action)
                            boolean isAttackDown = client.options.attackKey.isPressed();
                            if (isAttackDown) {
                                if (!wasAttackPressed || robotAttackCooldown <= 0) {
                                    wasAttackPressed = true;
                                    robotAttackCooldown = 4;
                                    targetRobot.performToolAction(targetRcCameraPitch, targetRobot.getYaw() + targetRcCameraYaw);

                                    PacketByteBuf toolBuf = PacketByteBufs.create();
                                    toolBuf.writeUuid(pairedUuid);
                                    toolBuf.writeFloat(targetRcCameraPitch);
                                    toolBuf.writeFloat(targetRobot.getYaw() + targetRcCameraYaw);
                                    ClientPlayNetworking.send(EvecualMC.RC_ROBOT_TOOL_ACTION_PACKET_ID, toolBuf);
                                }
                            } else {
                                wasAttackPressed = false;
                                robotAttackCooldown = 0;
                            }

                            // RMB: Left Arm (Block / Item Placement Action)
                            boolean isUseDown = client.options.useKey.isPressed();
                            if (isUseDown) {
                                if (!wasUsePressed || robotPlaceCooldown <= 0) {
                                    wasUsePressed = true;
                                    robotPlaceCooldown = 4;
                                    targetRobot.performPlaceAction(targetRcCameraPitch, targetRobot.getYaw() + targetRcCameraYaw);

                                    PacketByteBuf placeBuf = PacketByteBufs.create();
                                    placeBuf.writeUuid(pairedUuid);
                                    placeBuf.writeFloat(targetRcCameraPitch);
                                    placeBuf.writeFloat(targetRobot.getYaw() + targetRcCameraYaw);
                                    ClientPlayNetworking.send(EvecualMC.RC_ROBOT_PLACE_ACTION_PACKET_ID, placeBuf);
                                }
                            } else {
                                wasUsePressed = false;
                                robotPlaceCooldown = 0;
                            }

                        }
                    }

                    // --- 2. RC Car Handling ---
                    RcCarEntity targetRc = getTargetRcCar(client);
                    if (targetRc != null) {
                        java.util.UUID pairedUuid = targetRc.getUuid();

                        // 'C' Key while RC link is active: Auto Return to RC Charger!
                        if (cDown && !wasCPressed) {
                            wasCPressed = true;
                            PacketByteBuf dockBuf = PacketByteBufs.create();
                            dockBuf.writeUuid(pairedUuid);
                            ClientPlayNetworking.send(EvecualMC.RC_CAR_AUTO_DOCK_PACKET_ID, dockBuf);
                        }

                        if (client.player.squaredDistanceTo(targetRc) <= 65536.0) { // 256m max range
                            boolean rcFwd = client.options.forwardKey.isPressed();
                            boolean rcBack = client.options.backKey.isPressed();
                            boolean rcLeft = client.options.leftKey.isPressed();
                            boolean rcRight = client.options.rightKey.isPressed();
                            boolean rcSprint = client.options.sprintKey.isPressed();
                            boolean rcJump = client.options.jumpKey.isPressed();

                            boolean hasManualMove = rcFwd || rcBack || rcLeft || rcRight || rcJump;

                            if (!targetRc.isAutoReturning() || hasManualMove) {
                                targetRc.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcSprint, rcJump);

                                PacketByteBuf rcBuf = PacketByteBufs.create();
                                rcBuf.writeUuid(pairedUuid);
                                rcBuf.writeBoolean(rcFwd);
                                rcBuf.writeBoolean(rcBack);
                                rcBuf.writeBoolean(rcLeft);
                                rcBuf.writeBoolean(rcRight);
                                rcBuf.writeBoolean(rcSprint);
                                rcBuf.writeBoolean(rcJump);
                                rcBuf.writeFloat(targetRc.getYaw());
                                ClientPlayNetworking.send(EvecualMC.RC_CAR_INPUT_PACKET_ID, rcBuf);
                            }

                        }
                    }

                    // --- 3. RC Drone Handling ---
                    RcDroneEntity targetDrone = getTargetRcDrone(client);
                    if (targetDrone != null) {
                        java.util.UUID pairedUuid = targetDrone.getUuid();

                        // 'C' Key while RC link is active: Auto Return to RC Charger!
                        if (cDown && !wasCPressed) {
                            wasCPressed = true;
                            PacketByteBuf dockBuf = PacketByteBufs.create();
                            dockBuf.writeUuid(pairedUuid);
                            ClientPlayNetworking.send(EvecualMC.RC_DRONE_AUTO_DOCK_PACKET_ID, dockBuf);
                        }

                        if (client.player.squaredDistanceTo(targetDrone) <= 262144.0) { // 512m max range
                            boolean rcFwd = client.options.forwardKey.isPressed();
                            boolean rcBack = client.options.backKey.isPressed();
                            boolean rcLeft = client.options.leftKey.isPressed();
                            boolean rcRight = client.options.rightKey.isPressed();
                            boolean rcUp = client.options.jumpKey.isPressed();
                            boolean rcDown = client.options.sneakKey.isPressed();
                            boolean rcSprint = client.options.sprintKey.isPressed();
                            boolean isDroneInFp = client.getCameraEntity() == targetDrone && client.options.getPerspective().isFirstPerson();

                            boolean hasManualMove = rcFwd || rcBack || rcLeft || rcRight || rcUp || rcDown;

                            if (!targetDrone.isAutoReturning() || hasManualMove) {
                                targetDrone.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcUp, rcDown, rcSprint, isDroneInFp);

                                PacketByteBuf droneBuf = PacketByteBufs.create();
                                droneBuf.writeUuid(pairedUuid);
                                droneBuf.writeBoolean(rcFwd);
                                droneBuf.writeBoolean(rcBack);
                                droneBuf.writeBoolean(rcLeft);
                                droneBuf.writeBoolean(rcRight);
                                droneBuf.writeBoolean(rcUp);
                                droneBuf.writeBoolean(rcDown);
                                droneBuf.writeBoolean(rcSprint);
                                droneBuf.writeFloat(targetDrone.getYaw());
                                droneBuf.writeBoolean(isDroneInFp);
                                ClientPlayNetworking.send(EvecualMC.RC_DRONE_INPUT_PACKET_ID, droneBuf);
                            }

                        }
                    }

                    // --- 4. EV Heli Controller Handling ---
                    HeliEntity targetHeli = getTargetHeli(client);
                    if (targetHeli != null) {
                        java.util.UUID pairedUuid = targetHeli.getUuid();

                        // 'C' Key while Heli RC link is active: Auto Return to 3x3 Helipad!
                        if (cDown && !wasCPressed) {
                            wasCPressed = true;
                            PacketByteBuf dockBuf = PacketByteBufs.create();
                            dockBuf.writeUuid(pairedUuid);
                            ClientPlayNetworking.send(EvecualMC.HELI_CONTROLLER_AUTO_DOCK_PACKET_ID, dockBuf);
                        }

                        if (client.player.squaredDistanceTo(targetHeli) <= 1048576.0) { // 1024m max range (1024^2)
                            boolean rcFwd = client.options.forwardKey.isPressed();
                            boolean rcBack = client.options.backKey.isPressed();
                            boolean rcLeft = client.options.leftKey.isPressed();
                            boolean rcRight = client.options.rightKey.isPressed();
                            boolean rcUp = client.options.jumpKey.isPressed();
                            boolean rcDown = client.options.sneakKey.isPressed();
                            boolean rcSprint = client.options.sprintKey.isPressed() || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL);

                            boolean hasManualMove = rcFwd || rcBack || rcLeft || rcRight || rcUp || rcDown;

                            if (!targetHeli.isAutoReturning() || hasManualMove) {
                                targetHeli.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcUp, rcDown, rcSprint);

                                PacketByteBuf heliBuf = PacketByteBufs.create();
                                heliBuf.writeUuid(pairedUuid);
                                heliBuf.writeBoolean(rcFwd);
                                heliBuf.writeBoolean(rcBack);
                                heliBuf.writeBoolean(rcLeft);
                                heliBuf.writeBoolean(rcRight);
                                heliBuf.writeBoolean(rcUp);
                                heliBuf.writeBoolean(rcDown);
                                heliBuf.writeBoolean(rcSprint);
                                heliBuf.writeFloat(targetHeli.getYaw());
                                ClientPlayNetworking.send(EvecualMC.HELI_CONTROLLER_INPUT_PACKET_ID, heliBuf);
                            }

                            // Remote Hardpoint Weapon Angling (-60° to +30°)
                            boolean remoteUp = isKeyOrMousePressed(HELI_WEAPON_UP_KEY, windowHandle) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_UP);
                            boolean remoteDown = isKeyOrMousePressed(HELI_WEAPON_DOWN_KEY, windowHandle) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_DOWN);
                            if (remoteUp || remoteDown) {
                                float curAngle = targetHeli.getWeaponAngle();
                                float newAngle = curAngle;
                                if (remoteUp) newAngle += 1.5F;
                                if (remoteDown) newAngle -= 1.5F;
                                newAngle = MathHelper.clamp(newAngle, -60.0F, 30.0F);
                                if (Math.abs(newAngle - curAngle) > 0.01F) {
                                    targetHeli.setWeaponAngle(newAngle);
                                    PacketByteBuf angleBuf = PacketByteBufs.create();
                                    angleBuf.writeInt(targetHeli.getId());
                                    angleBuf.writeFloat(newAngle);
                                    ClientPlayNetworking.send(EvecualMC.HELI_WEAPON_ANGLE_PACKET_ID, angleBuf);
                                    sendSafeActionBar(client, String.format("§6🎯 Hardpoint Angle: §e%.1f° §7(Up/Down Arrow)", newAngle));
                                }
                            }

                            // Left Arm & Right Arm remotely (allows same keybind!)
                            boolean remoteI = isKeyOrMousePressed(HELI_LEFT_ARM_KEY, windowHandle) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_I);
                            boolean remoteO = isKeyOrMousePressed(HELI_RIGHT_ARM_KEY, windowHandle) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_O);

                            if (remoteI) {
                                if (!wasLeftArmPressed || heliLeftArmCooldown <= 0) {
                                    wasLeftArmPressed = true;
                                    heliLeftArmCooldown = 4;
                                    PacketByteBuf armBuf = PacketByteBufs.create();
                                    armBuf.writeUuid(pairedUuid);
                                    armBuf.writeInt(0); // 0 = Left Arm
                                    ClientPlayNetworking.send(EvecualMC.HELI_CONTROLLER_ARM_ACTION_PACKET_ID, armBuf);
                                }
                            } else {
                                wasLeftArmPressed = false;
                            }

                            if (remoteO) {
                                if (!wasRightArmPressed || heliRightArmCooldown <= 0) {
                                    wasRightArmPressed = true;
                                    heliRightArmCooldown = 4;
                                    PacketByteBuf armBuf = PacketByteBufs.create();
                                    armBuf.writeUuid(pairedUuid);
                                    armBuf.writeInt(1); // 1 = Right Arm
                                    ClientPlayNetworking.send(EvecualMC.HELI_CONTROLLER_ARM_ACTION_PACKET_ID, armBuf);
                                }
                            } else {
                                wasRightArmPressed = false;
                            }
                        }
                    }
                }

                // Arrow keys support as secondary camera control
                if (isRcCameraActive() && client.currentScreen == null) {
                    boolean left = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_LEFT);
                    boolean right = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT);
                    boolean up = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_UP);
                    boolean down = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_DOWN);

                    if (client.options.getPerspective().isFirstPerson()) {
                        Entity cam = client.getCameraEntity();
                        if (cam instanceof RcDroneEntity drone) {
                            if (left) { drone.setYaw(MathHelper.wrapDegrees(drone.getYaw() - 3.0F)); drone.prevYaw -= 3.0F; }
                            if (right) { drone.setYaw(MathHelper.wrapDegrees(drone.getYaw() + 3.0F)); drone.prevYaw += 3.0F; }
                        } else if (cam instanceof RcRobotEntity robot) {
                            if (left) { robot.setYaw(MathHelper.wrapDegrees(robot.getYaw() - 3.0F)); robot.prevYaw -= 3.0F; }
                            if (right) { robot.setYaw(MathHelper.wrapDegrees(robot.getYaw() + 3.0F)); robot.prevYaw += 3.0F; }
                        } else if (cam instanceof RcCarEntity car) {
                            if (left) { car.setYaw(MathHelper.wrapDegrees(car.getYaw() - 3.0F)); car.prevYaw -= 3.0F; }
                            if (right) { car.setYaw(MathHelper.wrapDegrees(car.getYaw() + 3.0F)); car.prevYaw += 3.0F; }
                        } else if (cam instanceof HeliEntity heli) {
                            if (left) { heli.setYaw(MathHelper.wrapDegrees(heli.getYaw() - 3.0F)); heli.prevYaw -= 3.0F; }
                            if (right) { heli.setYaw(MathHelper.wrapDegrees(heli.getYaw() + 3.0F)); heli.prevYaw += 3.0F; }
                        }
                        targetRcCameraYaw = 0.0F;
                        smoothRcCameraYaw = 0.0F;
                    } else {
                        if (left) targetRcCameraYaw -= 3.0f;
                        if (right) targetRcCameraYaw += 3.0f;
                    }
                    if (!(client.getCameraEntity() instanceof HeliEntity) && !(client.player.getVehicle() instanceof HeliEntity)) {
                        if (up) targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch - 2.0f, -80.0f, 80.0f);
                        if (down) targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + 2.0f, -80.0f, 80.0f);
                    }
                }

                // Reset camera if RC link is disabled or target vehicle is invalid
                if (client.getCameraEntity() instanceof RcCarEntity rc) {
                    boolean valid = isRcActive && rc.isAlive() && !rc.isRemoved() && client.player.squaredDistanceTo(rc) <= 65536.0;
                    if (!valid) {
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective);
                    }
                } else if (client.getCameraEntity() instanceof RcDroneEntity drone) {
                    boolean valid = isRcActive && drone.isAlive() && !drone.isRemoved() && client.player.squaredDistanceTo(drone) <= 262144.0;
                    if (!valid) {
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective);
                    }
                } else if (client.getCameraEntity() instanceof RcRobotEntity robot) {
                    boolean valid = isRcActive && robot.isAlive() && !robot.isRemoved() && client.player.squaredDistanceTo(robot) <= 65536.0;
                    if (!valid) {
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective);
                    }
                } else if (client.getCameraEntity() instanceof HeliEntity heli) {
                    boolean valid = isRcActive && heli.isAlive() && !heli.isRemoved() && client.player.squaredDistanceTo(heli) <= 1048576.0;
                    if (!valid) {
                        client.setCameraEntity(client.player);
                        client.options.setPerspective(previousPerspective);
                    }
                }
            }
        });
    }

    private static void openTipScreenContextual(MinecraftClient client) {
        if (client == null || client.player == null) return;

        // 1. If currently inside or piloting full-size Car
        if (client.player.getVehicle() instanceof CarEntity car) {
            client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.CAR, 0, 1000, "⚡ Vehicle Diagnostics: Active Piloting"));
            return;
        }

        // 2. If looking through RC camera or piloting RC vehicle
        Entity cam = client.getCameraEntity();
        if (cam instanceof RcDroneEntity drone) {
            client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.RC_DRONE, drone.getEnergy(), RcDroneEntity.MAX_ENERGY, "🚁 Aerial Telemetry: " + drone.getEnergy() + " / " + RcDroneEntity.MAX_ENERGY + " EU"));
            return;
        }
        if (cam instanceof RcRobotEntity robot) {
            client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.RC_ROBOT, robot.getEnergy(), RcRobotEntity.MAX_ENERGY, "🤖 Excavator Telemetry: " + robot.getEnergy() + " / " + RcRobotEntity.MAX_ENERGY + " EU"));
            return;
        }
        if (cam instanceof RcCarEntity car) {
            client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.RC_CAR, car.getEnergy(), RcCarEntity.MAX_ENERGY, "🏎️ Ground Telemetry: " + car.getEnergy() + " / " + RcCarEntity.MAX_ENERGY + " EU"));
            return;
        }

        // 3. If crosshair is aiming directly at an entity
        if (client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit && hit.getEntity() != null) {
            Entity hitEnt = hit.getEntity();
            if (hitEnt instanceof RcDroneEntity drone) {
                client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.RC_DRONE, drone.getEnergy(), RcDroneEntity.MAX_ENERGY, "🚁 Drone Targeted: " + drone.getEnergy() + " / " + RcDroneEntity.MAX_ENERGY + " EU"));
                return;
            }
            if (hitEnt instanceof RcRobotEntity robot) {
                client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.RC_ROBOT, robot.getEnergy(), RcRobotEntity.MAX_ENERGY, "🤖 Robot Targeted: " + robot.getEnergy() + " / " + RcRobotEntity.MAX_ENERGY + " EU"));
                return;
            }
            if (hitEnt instanceof RcCarEntity car) {
                client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.RC_CAR, car.getEnergy(), RcCarEntity.MAX_ENERGY, "🏎️ RC Car Targeted: " + car.getEnergy() + " / " + RcCarEntity.MAX_ENERGY + " EU"));
                return;
            }
            if (hitEnt instanceof CarEntity car) {
                client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.CAR, 0, 1000, "🚗 Electric Car Targeted"));
                return;
            }
        }

        // 4. If crosshair is aiming directly at a block
        if (client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult blockHit && client.world != null) {
            net.minecraft.block.BlockState bs = client.world.getBlockState(blockHit.getBlockPos());
            com.evecual.evecualmc.client.screen.TipTopic blockTopic = com.evecual.evecualmc.client.screen.TipTopic.fromBlock(bs.getBlock());
            if (blockTopic != null) {
                client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(blockTopic, 0, 0, "🔍 Inspected: " + blockTopic.title));
                return;
            }
        }

        // 5. If holding a mod item in main hand
        net.minecraft.item.ItemStack held = client.player.getMainHandStack();
        com.evecual.evecualmc.client.screen.TipTopic itemTopic = com.evecual.evecualmc.client.screen.TipTopic.fromItem(held.getItem());
        if (itemTopic != null) {
            client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(itemTopic, 0, 0, "📖 Guide: " + itemTopic.title));
            return;
        }

        // Default: Open general guide (starting on Solar Panel)
        client.setScreen(new com.evecual.evecualmc.client.screen.ModTipScreen(com.evecual.evecualmc.client.screen.TipTopic.SOLAR_PANEL, 0, 0, "📖 EvecualMC Field Guide & Diagnostics"));
    }
}
