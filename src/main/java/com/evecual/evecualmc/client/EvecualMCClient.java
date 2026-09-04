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

    private static boolean wasCPressed = false;
    private static boolean wasAttackPressed = false;
    private static int robotAttackCooldown = 0;
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
            sendSafeActionBar(client, "§b📷 RC Camera: §eTHIRD PERSON §7[RMB: FPV, Scroll: Zoom]");
        } else {
            client.options.setPerspective(Perspective.FIRST_PERSON);
            targetRcCameraYaw = 0.0F;
            smoothRcCameraYaw = 0.0F;
            sendSafeActionBar(client, "§b📷 RC Camera: §aFIRST PERSON (FPV) §7[RMB: 3RD, Scroll: Zoom]");
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
        smoothRcCameraYaw = MathHelper.lerp(0.25F, smoothRcCameraYaw, targetRcCameraYaw);
        return smoothRcCameraYaw;
    }

    public static float getRcCameraPitch() {
        smoothRcCameraPitch = MathHelper.lerp(0.25F, smoothRcCameraPitch, targetRcCameraPitch);
        return smoothRcCameraPitch;
    }

    public static void onRcMouseTurn(double cursorDeltaX, double cursorDeltaY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && isRcCameraActive() && client.options.getPerspective().isFirstPerson()) {
            Entity cam = client.getCameraEntity();
            float yawDelta = (float) (cursorDeltaX * 0.15);

            if (cam instanceof RcDroneEntity drone) {
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
            targetRcFpZoom = 1.0F;
            rcFpZoom = 1.0F;
            sendSafeActionBar(client, "§7📷 RC Camera: §cDISABLED §7[Player View]");
        } else {
            resetRcCameraAngle();
            targetRcCameraDistance = 3.5F;
            rcCameraDistance = 3.5F;
            targetRcFpZoom = 1.0F;
            rcFpZoom = 1.0F;
            previousPerspective = client.options.getPerspective();
            client.setCameraEntity(targetRc);
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            String name = (targetRc instanceof RcDroneEntity) ? "Drone" : (targetRc instanceof RcRobotEntity) ? "Robot" : "Car";
            sendSafeActionBar(client, "§b📷 " + name + " Cam: §aACTIVE §7[RMB: View | F: Exit]");
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

        // Client Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (robotAttackCooldown > 0) {
                    robotAttackCooldown--;
                }

                // Open Trunk / Cargo Key ('Z')
                while (OPEN_TRUNK_KEY.wasPressed()) {
                    // 1. If inside car
                    if (client.player.getVehicle() instanceof CarEntity car) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(car.getId());
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
                        if (e instanceof RcRobotEntity || e instanceof RcDroneEntity || e instanceof RcCarEntity || e instanceof CarEntity) {
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
                                e -> e instanceof RcRobotEntity || e instanceof RcDroneEntity || e instanceof RcCarEntity || e instanceof CarEntity);
                        if (!nearEntities.isEmpty()) {
                            nearEntities.sort(java.util.Comparator.comparingDouble(e -> e.squaredDistanceTo(client.player)));
                            Entity closest = nearEntities.get(0);
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(closest.getId());
                            ClientPlayNetworking.send(EvecualMC.OPEN_TRUNK_PACKET_ID, buf);
                        }
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
                            robotBuf.writeFloat(targetRobot.getYaw());
                            ClientPlayNetworking.send(EvecualMC.RC_ROBOT_INPUT_PACKET_ID, robotBuf);

                            // LMB: Tool Action (holding LMB repeatedly swings & strikes every 4 ticks)
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

                            if (client.player.age % 10 == 0) {
                                boolean isCamView = client.getCameraEntity() == targetRobot;
                                String toolName = targetRobot.getEquippedTool().isEmpty() ? "Bare Hand" : targetRobot.getEquippedTool().getName().getString();
                                if (toolName.length() > 14) {
                                    toolName = toolName.substring(0, 12) + "…";
                                }
                                int range = (int)client.player.distanceTo(targetRobot);
                                int energy = targetRobot.getEnergy();
                                int screenWidth = client.getWindow().getScaledWidth();

                                String msg;
                                if (isCamView) {
                                    boolean fp = client.options.getPerspective().isFirstPerson();
                                    String viewMode = fp ? "FPV" : "3RD";
                                    if (screenWidth < 380) {
                                        msg = "§6🤖 Cam (" + viewMode + ") §a" + energy + "E §7| §b" + toolName;
                                    } else {
                                        msg = "§6🤖 Cam (" + viewMode + ") §a" + energy + "E §7| §b" + toolName + " §8[§fLMB§7:Mine §fZ§7:Inv §fRMB§7:View §fF§7:Exit]";
                                    }
                                } else {
                                    if (screenWidth < 380) {
                                        msg = "§6🤖 Robot: §a" + energy + "E §7| §b" + toolName + " §7| §e" + range + "m";
                                    } else {
                                        msg = "§6🤖 Robot: §a" + energy + "E §7| §b" + toolName + " §7| §e" + range + "m §8[§fF§7:Cam §fZ§7:Cargo §fL§7:Light §fC§7:Dock]";
                                    }
                                }
                                sendSafeActionBar(client, msg);
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
                            rcBuf.writeFloat(targetRc.getYaw());
                            ClientPlayNetworking.send(EvecualMC.RC_CAR_INPUT_PACKET_ID, rcBuf);

                            if (client.player.age % 10 == 0) {
                                boolean isCamView = client.getCameraEntity() == targetRc;
                                int range = (int)client.player.distanceTo(targetRc);
                                int energy = targetRc.getEnergy();
                                int screenWidth = client.getWindow().getScaledWidth();

                                String msg;
                                if (isCamView) {
                                    boolean fp = client.options.getPerspective().isFirstPerson();
                                    String viewMode = fp ? "FPV" : "3RD";
                                    if (screenWidth < 340) {
                                        msg = "§b🏎️ Car Cam (" + viewMode + ") §a" + energy + "E";
                                    } else {
                                        msg = "§b🏎️ Car Cam (" + viewMode + ") §a" + energy + "E §8[§fRMB§7:View §fL§7:Light §fF§7:Exit]";
                                    }
                                } else {
                                    if (screenWidth < 360) {
                                        msg = "§b🏎️ RC Car: §a" + energy + "E §7| §e" + range + "m";
                                    } else {
                                        msg = "§b🏎️ RC Car: §a" + energy + "E §7| §e" + range + "m §8[§fF§7:Cam §fL§7:Light §fC§7:Dock]";
                                    }
                                }
                                sendSafeActionBar(client, msg);
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

                            if (client.player.age % 10 == 0) {
                                boolean isCamView = client.getCameraEntity() == targetDrone;
                                int range = (int)client.player.distanceTo(targetDrone);
                                int energy = targetDrone.getEnergy();
                                String alt = String.format("%.1f", targetDrone.getY());
                                int screenWidth = client.getWindow().getScaledWidth();

                                String msg;
                                if (isCamView) {
                                    boolean fp = client.options.getPerspective().isFirstPerson();
                                    String viewMode = fp ? "FPV" : "3RD";
                                    if (screenWidth < 360) {
                                        msg = "§b🚁 Drone (" + viewMode + ") §a" + energy + "E §7| §e" + alt + "m";
                                    } else {
                                        msg = "§b🚁 Drone (" + viewMode + ") §a" + energy + "E §7| §e" + alt + "m §8[§fA/D§7:Strafe §fRMB§7:View §fF§7:Exit]";
                                    }
                                } else {
                                    if (screenWidth < 380) {
                                        msg = "§b🚁 Drone: §a" + energy + "E §7| §e" + alt + "m §7| §e" + range + "m";
                                    } else {
                                        msg = "§b🚁 Drone: §a" + energy + "E §7| §e" + alt + "m §7| §e" + range + "m §8[§fF§7:Cam §fL§7:Light §fC§7:Dock]";
                                    }
                                }
                                sendSafeActionBar(client, msg);
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
                        }
                        targetRcCameraYaw = 0.0F;
                        smoothRcCameraYaw = 0.0F;
                    } else {
                        if (left) targetRcCameraYaw -= 3.0f;
                        if (right) targetRcCameraYaw += 3.0f;
                    }
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
