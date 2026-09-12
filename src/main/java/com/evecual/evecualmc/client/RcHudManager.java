package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class RcHudManager {
    public static final int DEFAULT_NOTIFICATION_DURATION = 100; // 5.0 seconds (100 ticks)

    private static Text tempNotification = null;
    private static int tempNotificationTicks = 0;

    public static void setTempNotification(Text text, int durationTicks) {
        if (text == null) return;
        tempNotification = text;
        tempNotificationTicks = Math.max(30, durationTicks);
    }

    public static void tick() {
        if (tempNotificationTicks > 0) {
            tempNotificationTicks--;
            if (tempNotificationTicks <= 0) {
                tempNotification = null;
            }
        }
    }

    public static boolean isRcControllerActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;
        if (EvecualMCClient.isRcCameraActive()) return true;

        ItemStack main = client.player.getMainHandStack();
        ItemStack off = client.player.getOffHandStack();
        return (main != null && main.isOf(EvecualMC.RC_CONTROLLER_ITEM)) ||
               (off != null && off.isOf(EvecualMC.RC_CONTROLLER_ITEM));
    }

    public static void renderRcHud(DrawContext drawContext, MinecraftClient client) {
        if (client == null || client.player == null || client.options.hudHidden) return;
        if (!isRcControllerActive()) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // 1. Resolve Active Vehicle
        RcRobotEntity robot = EvecualMCClient.getTargetRcRobot(client);
        RcCarEntity car = EvecualMCClient.getTargetRcCar(client);
        RcDroneEntity drone = EvecualMCClient.getTargetRcDrone(client);

        Entity camEntity = client.getCameraEntity();
        if (camEntity instanceof RcRobotEntity r) robot = r;
        else if (camEntity instanceof RcCarEntity c) car = c;
        else if (camEntity instanceof RcDroneEntity d) drone = d;

        // 2. Build Persistent Telemetry String
        String persistentText = null;

        if (robot != null && (client.player.squaredDistanceTo(robot) <= 65536.0 || camEntity == robot)) {
            boolean isCamView = camEntity == robot;
            String toolName = robot.getEquippedTool().isEmpty() ? "Bare Hand" : robot.getEquippedTool().getName().getString();
            if (toolName.length() > 14) {
                toolName = toolName.substring(0, 12) + "…";
            }
            int range = (int) client.player.distanceTo(robot);
            int energy = robot.getEnergy();

            if (isCamView) {
                boolean fp = client.options.getPerspective().isFirstPerson();
                String viewMode = fp ? "FPV" : "3RD";
                if (screenWidth < 380) {
                    persistentText = "§6🤖 Cam (" + viewMode + ") §a" + energy + "E §7| §b" + toolName;
                } else {
                    persistentText = "§6🤖 Cam (" + viewMode + ") §a" + energy + "E §7| §b" + toolName + " §8[§fLMB§7:Mine §fZ§7:Inv §fRMB§7:View §fF§7:Exit]";
                }
            } else {
                if (screenWidth < 380) {
                    persistentText = "§6🤖 Robot: §a" + energy + "E §7| §b" + toolName + " §7| §e" + range + "m";
                } else {
                    persistentText = "§6🤖 Robot: §a" + energy + "E §7| §b" + toolName + " §7| §e" + range + "m §8[§fF§7:Cam §fZ§7:Cargo §fL§7:Light §fC§7:Dock]";
                }
            }
        } else if (car != null && (client.player.squaredDistanceTo(car) <= 65536.0 || camEntity == car)) {
            boolean isCamView = camEntity == car;
            int range = (int) client.player.distanceTo(car);
            int energy = car.getEnergy();

            if (isCamView) {
                boolean fp = client.options.getPerspective().isFirstPerson();
                String viewMode = fp ? "FPV" : "3RD";
                if (screenWidth < 340) {
                    persistentText = "§b🏎️ Car Cam (" + viewMode + ") §a" + energy + "E";
                } else {
                    persistentText = "§b🏎️ Car Cam (" + viewMode + ") §a" + energy + "E §8[§fRMB§7:View §fL§7:Light §fF§7:Exit]";
                }
            } else {
                if (screenWidth < 360) {
                    persistentText = "§b🏎️ RC Car: §a" + energy + "E §7| §e" + range + "m";
                } else {
                    persistentText = "§b🏎️ RC Car: §a" + energy + "E §7| §e" + range + "m §8[§fF§7:Cam §fL§7:Light §fC§7:Dock]";
                }
            }
        } else if (drone != null && (client.player.squaredDistanceTo(drone) <= 262144.0 || camEntity == drone)) {
            boolean isCamView = camEntity == drone;
            boolean isPickup = drone instanceof com.evecual.evecualmc.entity.PickupDroneEntity;
            String icon = isPickup ? "🛡️" : "🚁";
            String dName = isPickup ? "Pickup Drone" : "Drone";
            int range = (int) client.player.distanceTo(drone);
            int energy = drone.getEnergy();
            String alt = String.format("%.1f", drone.getY());

            if (isCamView) {
                boolean fp = client.options.getPerspective().isFirstPerson();
                String viewMode = fp ? "FPV" : "3RD";
                if (screenWidth < 360) {
                    persistentText = "§b" + icon + " " + dName + " (" + viewMode + ") §a" + energy + "E §7| §e" + alt + "m";
                } else {
                    persistentText = "§b" + icon + " " + dName + " (" + viewMode + ") §a" + energy + "E §7| §e" + alt + "m" + (isPickup ? " §8[§fZ§7:Cargo §fRMB§7:View §fF§7:Exit]" : " §8[§fA/D§7:Strafe §fRMB§7:View §fF§7:Exit]");
                }
            } else {
                if (screenWidth < 380) {
                    persistentText = "§b" + icon + " " + dName + ": §a" + energy + "E §7| §e" + alt + "m §7| §e" + range + "m";
                } else {
                    persistentText = "§b" + icon + " " + dName + ": §a" + energy + "E §7| §e" + alt + "m §7| §e" + range + "m §8[§fF§7:Cam §fZ§7:Cargo §fL§7:Light §fC§7:Dock]";
                }
            }
        } else {
            ItemStack held = client.player.getMainHandStack().isOf(EvecualMC.RC_CONTROLLER_ITEM)
                    ? client.player.getMainHandStack()
                    : client.player.getOffHandStack();
            if (held != null && held.hasNbt()) {
                boolean hasPair = held.getNbt().containsUuid("PairedCar") ||
                                  held.getNbt().containsUuid("PairedDrone") ||
                                  held.getNbt().containsUuid("PairedRobot");
                if (hasPair) {
                    persistentText = "§e📡 Vehicle out of range or not loaded";
                } else {
                    persistentText = "§7📡 RC Controller: §cSTANDBY §7[Sneak+Right-Click near vehicle to pair]";
                }
            } else {
                persistentText = "§7📡 RC Controller: §cSTANDBY §7[Sneak+Right-Click near vehicle to pair]";
            }
        }

        // Base Y position for persistent HUD (action bar height above hotbar)
        int baseY = screenHeight - 68;

        // 3. Render Temporary Info Notification (Placed directly OVER the persistent info text)
        if (tempNotification != null && tempNotificationTicks > 0) {
            int tempY = baseY - 15; // Directly OVER persistent text
            float alpha = tempNotificationTicks < 20 ? (tempNotificationTicks / 20.0f) : 1.0f;
            int textAlpha = (int) (alpha * 255);
            int bgAlpha = (int) (alpha * 190);
            int borderAlpha = (int) (alpha * 240);

            int tWidth = textRenderer.getWidth(tempNotification);
            int tX = (screenWidth - tWidth) / 2;

            // Translucent dark slate background with amber highlight border
            drawContext.fill(tX - 6, tempY - 2, tX + tWidth + 6, tempY + 10, (bgAlpha << 24) | 0x0F172A);
            drawContext.drawBorder(tX - 6, tempY - 2, tWidth + 12, 12, (borderAlpha << 24) | 0xF59E0B);
            drawContext.drawTextWithShadow(textRenderer, tempNotification, tX, tempY, (textAlpha << 24) | 0xFFFFFF);
        }

        // 4. Render Persistent Telemetry Line
        if (persistentText != null) {
            int maxPixelWidth = Math.max(100, screenWidth - 24);
            if (textRenderer.getWidth(persistentText) > maxPixelWidth) {
                persistentText = textRenderer.trimToWidth(persistentText, maxPixelWidth - 10) + "…";
            }
            int pWidth = textRenderer.getWidth(persistentText);
            int pX = (screenWidth - pWidth) / 2;

            // Translucent slate backdrop with cyan accent border
            drawContext.fill(pX - 5, baseY - 2, pX + pWidth + 5, baseY + 10, 0x990A0F1D);
            drawContext.drawBorder(pX - 5, baseY - 2, pWidth + 10, 12, 0x4438BDF8);
            drawContext.drawTextWithShadow(textRenderer, persistentText, pX, baseY, 0xFFFFFF);
        }
    }
}
