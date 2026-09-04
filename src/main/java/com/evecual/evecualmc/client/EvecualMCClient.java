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
import com.evecual.evecualmc.entity.CarEntity;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.fabricmc.api.ClientModInitializer;
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
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
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

    private static boolean wasCPressed = false;
    private static boolean wasAttackPressed = false;
    private static Perspective previousPerspective = Perspective.FIRST_PERSON;

    // Camera angles with exponential smoothing for ultra-fluid mouse orbiting
    private static float targetRcCameraYaw = 0.0F;
    private static float targetRcCameraPitch = 12.0F;
    private static float smoothRcCameraYaw = 0.0F;
    private static float smoothRcCameraPitch = 12.0F;

    public static float getRcCameraYaw() {
        smoothRcCameraYaw = MathHelper.lerp(0.25F, smoothRcCameraYaw, targetRcCameraYaw);
        return smoothRcCameraYaw;
    }

    public static float getRcCameraPitch() {
        smoothRcCameraPitch = MathHelper.lerp(0.25F, smoothRcCameraPitch, targetRcCameraPitch);
        return smoothRcCameraPitch;
    }

    public static void onRcMouseTurn(double cursorDeltaX, double cursorDeltaY) {
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
        return cam instanceof RcCarEntity || cam instanceof RcDroneEntity || cam instanceof RcRobotEntity;
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
            return nbt != null && (nbt.containsUuid("PairedCar") || nbt.containsUuid("PairedDrone") || nbt.containsUuid("PairedRobot")) && nbt.getBoolean("ActiveLink");
        }
        return false;
    }

    public static RcCarEntity getTargetRcCar(MinecraftClient client) {
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
            if (e instanceof RcCarEntity rc && rc.getUuid().equals(pairedUuid)) {
                return rc;
            }
        }
        return null;
    }

    public static RcDroneEntity getTargetRcDrone(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return null;
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

        Entity targetRc = getTargetRcCar(client);
        if (targetRc == null) {
            targetRc = getTargetRcDrone(client);
        }
        if (targetRc == null) {
            targetRc = getTargetRcRobot(client);
        }

        if (targetRc == null) {
            client.player.sendMessage(Text.literal("§c📷 No linked RC Car, Drone, or Robot found in range!"), true);
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
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            String name = (targetRc instanceof RcDroneEntity) ? "Drone" : (targetRc instanceof RcRobotEntity) ? "Robot" : "Car";
            client.player.sendMessage(Text.literal("§b📷 RC " + name + " Camera: §aENABLED §7[Mouse to Aim/Rotate, F to return]"), true);
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
        EntityModelLayerRegistry.registerModelLayer(RcCarEntityModel.MODEL_LAYER, RcCarEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_CAR_ENTITY, RcCarEntityRenderer::new);

        // Register RC Drone Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(RcDroneEntityModel.MODEL_LAYER, RcDroneEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_DRONE_ENTITY, RcDroneEntityRenderer::new);

        // Register RC Robot Model and Renderer
        EntityModelLayerRegistry.registerModelLayer(RcRobotEntityModel.MODEL_LAYER, RcRobotEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(EvecualMC.RC_ROBOT_ENTITY, RcRobotEntityRenderer::new);

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

        // Client Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                // Open Trunk Key
                while (OPEN_TRUNK_KEY.wasPressed()) {
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(car.getId());
                        ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                    }
                }

                // Attach Cable Key
                while (ATTACH_CABLE_KEY.wasPressed()) {
                    ClientPlayNetworking.send(EvecualMC.TOGGLE_CABLE_PACKET_ID, PacketByteBufs.empty());
                }

                // Auto Park Key (for full-size Car)
                while (AUTO_PARK_KEY.wasPressed()) {
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(car.getId());
                        ClientPlayNetworking.send(EvecualMC.AUTO_PARK_PACKET_ID, buf);
                    }
                }

                // RC Camera Toggle Key ('F')
                while (RC_CAMERA_KEY.wasPressed()) {
                    toggleRcCamera(client);
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

                            targetRobot.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcSprint, rcJump);

                            PacketByteBuf robotBuf = PacketByteBufs.create();
                            robotBuf.writeUuid(pairedUuid);
                            robotBuf.writeBoolean(rcFwd);
                            robotBuf.writeBoolean(rcBack);
                            robotBuf.writeBoolean(rcLeft);
                            robotBuf.writeBoolean(rcRight);
                            robotBuf.writeBoolean(rcSprint);
                            robotBuf.writeBoolean(rcJump);
                            ClientPlayNetworking.send(EvecualMC.RC_ROBOT_INPUT_PACKET_ID, robotBuf);

                            // LMB: Tool Action
                            boolean attackPressed = client.options.attackKey.wasPressed() || client.options.attackKey.isPressed();
                            if (attackPressed && !wasAttackPressed) {
                                wasAttackPressed = true;
                                targetRobot.performToolAction(targetRcCameraPitch, targetRobot.getYaw() + targetRcCameraYaw);

                                PacketByteBuf toolBuf = PacketByteBufs.create();
                                toolBuf.writeUuid(pairedUuid);
                                toolBuf.writeFloat(targetRcCameraPitch);
                                toolBuf.writeFloat(targetRobot.getYaw() + targetRcCameraYaw);
                                ClientPlayNetworking.send(EvecualMC.RC_ROBOT_TOOL_ACTION_PACKET_ID, toolBuf);
                            } else if (!client.options.attackKey.isPressed()) {
                                wasAttackPressed = false;
                            }

                            if (client.player.age % 10 == 0) {
                                boolean isCamView = client.getCameraEntity() == targetRobot;
                                String camPrompt = isCamView ? "Mouse: Aim | LMB: Use Tool | F: Player View" : "F: Robot Cam";
                                String toolName = targetRobot.getEquippedTool().isEmpty() ? "Bare Hand" : targetRobot.getEquippedTool().getName().getString();
                                client.player.sendMessage(Text.literal("§6🤖 RC ROBOT: §a" + targetRobot.getEnergy() + " E §7| §bTool: " + toolName + " §7| §eRange: " + (int)client.player.distanceTo(targetRobot) + "m/256m §8| §f[" + camPrompt + "]"), true);
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
                                String camPrompt = isCamView ? "Mouse: Orbit Cam | F: Player View" : "F: RC Camera";
                                client.player.sendMessage(Text.literal("§b📡 RC CAR: §a" + targetRc.getEnergy() + " E §7| §eRange: " + (int)client.player.distanceTo(targetRc) + "m/256m §8| §f[" + camPrompt + ", C: Charger]"), true);
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

                            targetDrone.setRemoteInputs(rcFwd, rcBack, rcLeft, rcRight, rcUp, rcDown, rcSprint);

                            PacketByteBuf droneBuf = PacketByteBufs.create();
                            droneBuf.writeUuid(pairedUuid);
                            droneBuf.writeBoolean(rcFwd);
                            droneBuf.writeBoolean(rcBack);
                            droneBuf.writeBoolean(rcLeft);
                            droneBuf.writeBoolean(rcRight);
                            droneBuf.writeBoolean(rcUp);
                            droneBuf.writeBoolean(rcDown);
                            droneBuf.writeBoolean(rcSprint);
                            ClientPlayNetworking.send(EvecualMC.RC_DRONE_INPUT_PACKET_ID, droneBuf);

                            if (client.player.age % 10 == 0) {
                                boolean isCamView = client.getCameraEntity() == targetDrone;
                                String camPrompt = isCamView ? "Mouse: Orbit Cam | F: Player View" : "F: Drone Camera";
                                client.player.sendMessage(Text.literal("§b📡 RC DRONE: §a" + targetDrone.getEnergy() + " E §7| §eAlt: " + String.format("%.1f", targetDrone.getY()) + "m §7| §eRange: " + (int)client.player.distanceTo(targetDrone) + "m/512m §8| §f[" + camPrompt + ", C: Charger]"), true);
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

                    if (left) targetRcCameraYaw -= 3.0f;
                    if (right) targetRcCameraYaw += 3.0f;
                    if (up) targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch - 2.0f, -80.0f, 80.0f);
                    if (down) targetRcCameraPitch = MathHelper.clamp(targetRcCameraPitch + 2.0f, -80.0f, 80.0f);
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
                }
            }
        });
    }
}
