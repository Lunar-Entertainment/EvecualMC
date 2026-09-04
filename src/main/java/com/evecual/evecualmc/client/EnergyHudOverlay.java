package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ParkingLinesBlock;
import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import com.evecual.evecualmc.block.entity.ChargerBlockEntity;
import com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity;
import com.evecual.evecualmc.block.entity.RcChargerBlockEntity;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import com.evecual.evecualmc.entity.CarEntity;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class EnergyHudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) return;
        if (client.options.hudHidden) return;

        // 1. Render RC Controller HUD (Persistent telemetry & Temporary notifications placed over it)
        RcHudManager.renderRcHud(drawContext, client);

        // 2. Render active Vehicle Charger Waypoint if player is inside a car
        if (client.player.getVehicle() instanceof CarEntity) {
            renderChargerWaypoints(drawContext, client);
        }

        HitResult hit = client.crosshairTarget;

        // 2. Check if riding a car
        if (client.player.getVehicle() instanceof CarEntity car) {
            renderCarTip(drawContext, client, car);
            return;
        }

        if (hit == null) return;

        // 3. Check if looking at an entity
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof CarEntity car) {
                renderCarTip(drawContext, client, car);
                return;
            } else if (entity instanceof RcCarEntity rcCar) {
                renderRcCarTip(drawContext, client, rcCar);
                return;
            } else if (entity instanceof RcDroneEntity rcDrone) {
                renderRcDroneTip(drawContext, client, rcDrone);
                return;
            } else if (entity instanceof RcRobotEntity rcRobot) {
                renderRcRobotTip(drawContext, client, rcRobot);
                return;
            }
        }

        // 4. Check if looking at a block
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);
            BlockEntity be = client.world.getBlockEntity(pos);

            if (be instanceof SolarPanelBlockEntity solar) {
                renderSolarTip(drawContext, client, solar);
            } else if (be instanceof BatteryBlockEntity battery) {
                renderBatteryTip(drawContext, client, battery);
            } else if (be instanceof ElectronicCombinerBlockEntity combiner) {
                renderCombinerTip(drawContext, client, combiner);
            } else if (be instanceof ChargerBlockEntity charger) {
                ChargerWaypointManager.addWaypoint(pos);
                renderChargerTip(drawContext, client, charger);
            } else if (be instanceof ChargerExtensionBlockEntity extension) {
                renderChargerExtensionTip(drawContext, client, extension);
            } else if (be instanceof RcChargerBlockEntity rcCharger) {
                renderRcChargerTip(drawContext, client, rcCharger);
            } else if (state.isOf(EvecualMC.WIRE_BLOCK)) {
                renderWireTip(drawContext, client, state);
            } else if (state.isOf(EvecualMC.PARKING_LINES_BLOCK)) {
                renderParkingLinesTip(drawContext, client, pos, state);
            } else if (state.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK)) {
                renderRcParkingSpotTip(drawContext, client);
            } else if (state.isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK)) {
                renderDroneParkingSpotTip(drawContext, client);
            }
        }
    }

    private void renderChargerWaypoints(DrawContext context, MinecraftClient client) {
        if (client.player == null) return;
        var waypoints = ChargerWaypointManager.getWaypoints();
        if (waypoints.isEmpty()) return;

        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (BlockPos pos : waypoints) {
            double dSq = client.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (dSq < nearestDistSq) {
                nearestDistSq = dSq;
                nearest = pos;
            }
        }

        if (nearest == null) return;

        int dist = (int) Math.sqrt(nearestDistSq);
        int screenWidth = client.getWindow().getScaledWidth();
        TextRenderer tr = client.textRenderer;

        double dx = (nearest.getX() + 0.5) - client.player.getX();
        double dz = (nearest.getZ() + 0.5) - client.player.getZ();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float angleDiff = MathHelper.wrapDegrees(targetYaw - client.player.getYaw());

        String distStr = "⚡ Charger [" + dist + "m]";
        int textWidth = tr.getWidth(distStr);

        if (Math.abs(angleDiff) < 45.0F) {
            int badgeX = (screenWidth / 2) + (int) (angleDiff * 3.5F);
            badgeX = MathHelper.clamp(badgeX, textWidth / 2 + 10, screenWidth - textWidth / 2 - 10);
            int badgeY = 6;

            context.fill(badgeX - textWidth / 2 - 4, badgeY - 2, badgeX + textWidth / 2 + 4, badgeY + 12, 0xCC0F172A);
            context.drawBorder(badgeX - textWidth / 2 - 4, badgeY - 2, textWidth + 8, 14, 0xFFF59E0B);
            context.drawText(tr, Text.literal(distStr), badgeX - textWidth / 2, badgeY + 1, 0xFFFDE047, true);
        } else if (angleDiff <= -45.0F && angleDiff >= -135.0F) {
            String leftText = "◀ " + distStr;
            int y = 6;
            context.fill(6, y - 2, 6 + tr.getWidth(leftText) + 8, y + 12, 0xCC0F172A);
            context.drawBorder(6, y - 2, tr.getWidth(leftText) + 8, 14, 0xFFF59E0B);
            context.drawText(tr, Text.literal(leftText), 10, y + 1, 0xFFFDE047, true);
        } else if (angleDiff >= 45.0F && angleDiff <= 135.0F) {
            String rightText = distStr + " ▶";
            int rWidth = tr.getWidth(rightText);
            int x = screenWidth - rWidth - 14;
            int y = 6;
            context.fill(x - 2, y - 2, x + rWidth + 6, y + 12, 0xCC0F172A);
            context.drawBorder(x - 2, y - 2, rWidth + 8, 14, 0xFFF59E0B);
            context.drawText(tr, Text.literal(rightText), x + 2, y + 1, 0xFFFDE047, true);
        } else {
            String behindText = "▼ " + distStr + " (Behind)";
            int bWidth = tr.getWidth(behindText);
            int x = (screenWidth - bWidth) / 2;
            int y = 6;
            context.fill(x - 4, y - 2, x + bWidth + 4, y + 12, 0xCC0F172A);
            context.drawBorder(x - 4, y - 2, bWidth + 8, 14, 0xFFF59E0B);
            context.drawText(tr, Text.literal(behindText), x, y + 1, 0xFFFCD34D, true);
        }
    }

    /**
     * Unified Modern HUD Renderer matching the golden-bordered, dark-glassmorphic style
     */
    private void renderUnifiedHud(DrawContext context, MinecraftClient client,
                                  String iconEmoji, String title, int titleColor,
                                  String rightHeader, int rightHeaderColor,
                                  String status, int statusColor,
                                  Double progressRatio, Integer progressFillColor) {
        TextRenderer tr = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();

        String headerLeft = (iconEmoji.isEmpty() ? "" : iconEmoji + " ") + title;
        int leftWidth = tr.getWidth(headerLeft);
        int rightWidth = rightHeader != null && !rightHeader.isEmpty() ? tr.getWidth(rightHeader) : 0;
        int statusWidth = status != null && !status.isEmpty() ? tr.getWidth(status) : 0;

        int contentWidth = Math.max(leftWidth + (rightWidth > 0 ? rightWidth + 24 : 0), statusWidth);
        int boxWidth = Math.max(170, contentWidth + 20);
        int hasBar = (progressRatio != null && progressFillColor != null) ? 1 : 0;
        int boxHeight = (status != null && !status.isEmpty() ? 28 : 16) + (hasBar * 12) + 6;

        int x = (screenWidth - boxWidth) / 2;
        int y = 16;

        // Dark background (matching the user's reference image)
        context.fill(x, y, x + boxWidth, y + boxHeight, 0xF00A111E);
        // Golden/Amber glowing border (0xFFF59E0B)
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFF59E0B);

        int curY = y + 5;
        // Draw Left Title
        context.drawText(tr, Text.literal(headerLeft), x + 8, curY, titleColor, true);

        // Draw Right Header (Energy buffer / counter)
        if (rightHeader != null && !rightHeader.isEmpty()) {
            context.drawText(tr, Text.literal(rightHeader), x + boxWidth - 8 - rightWidth, curY, rightHeaderColor, true);
        }

        // Draw Status Line
        if (status != null && !status.isEmpty()) {
            curY += 12;
            context.drawText(tr, Text.literal(status), x + 8, curY, statusColor, true);
        }

        // Draw Progress Bar
        if (hasBar == 1) {
            curY += 12;
            drawProgressBar(context, x + 8, curY, boxWidth - 16, 7, progressRatio, progressFillColor);
        }
    }

    private void renderSolarTip(DrawContext context, MinecraftClient client, SolarPanelBlockEntity solar) {
        long energy = solar.getEnergy();
        long maxEnergy = solar.getMaxEnergy();
        int genRate = solar.getGenerationRate();

        String rightHeader = energy + " / " + maxEnergy + " E";
        String genStr = genRate > 0 ? "⚡ Generating: +" + genRate + " E/s" : "🌙 Inactive (No Sunlight)";
        int statusColor = genRate > 0 ? 0xFF86EFAC : 0xFF94A3B8;

        renderUnifiedHud(context, client, "☀", "Solar Panel", 0xFFFDE047,
                rightHeader, 0xFFFFFFFF, genStr, statusColor,
                (double) energy / Math.max(1, maxEnergy), 0xFFFBBF24);
    }

    private void renderBatteryTip(DrawContext context, MinecraftClient client, BatteryBlockEntity battery) {
        long energy = battery.getEnergy();
        long maxEnergy = battery.getMaxEnergy();
        int pct = (int) (energy * 100 / Math.max(1, maxEnergy));

        String rightHeader = energy + " / " + maxEnergy + " E";
        String status = "Charge: " + pct + "% (Stores power for network)";

        renderUnifiedHud(context, client, "🔋", "Battery", 0xFF60A5FA,
                rightHeader, 0xFFFFFFFF, status, 0xFF93C5FD,
                (double) energy / Math.max(1, maxEnergy), 0xFF3B82F6);
    }

    private void renderCombinerTip(DrawContext context, MinecraftClient client, ElectronicCombinerBlockEntity combiner) {
        long energy = combiner.getEnergy();
        long maxEnergy = combiner.getMaxEnergy();
        String status = combiner.isCrafting() ? "⚡ Assembling Vehicle..." : "Idle (Insert Blueprint & Power)";
        int color = combiner.isCrafting() ? 0xFF86EFAC : 0xFF94A3B8;

        renderUnifiedHud(context, client, "⚙", "Electronic Combiner", 0xFFC084FC,
                energy + " / " + maxEnergy + " E", 0xFFFFFFFF, status, color,
                (double) energy / Math.max(1, maxEnergy), 0xFFA855F7);
    }

    private void renderChargerTip(DrawContext context, MinecraftClient client, ChargerBlockEntity charger) {
        long energy = charger.getEnergy();
        long maxEnergy = charger.getMaxEnergy();
        String status = charger.isCharging() ? "⚡ Charging Vehicle..." : "Ready (Connect Charger Extension)";
        int color = charger.isCharging() ? 0xFF86EFAC : 0xFF94A3B8;

        renderUnifiedHud(context, client, "⚡", "Car Charger", 0xFF34D399,
                energy + " / " + maxEnergy + " E", 0xFFFFFFFF, status, color,
                (double) energy / Math.max(1, maxEnergy), 0xFF10B981);
    }

    private void renderChargerExtensionTip(DrawContext context, MinecraftClient client, ChargerExtensionBlockEntity extension) {
        String status;
        int color;
        if (extension.isConnected()) {
            status = "⚡ Connected: Actively charging car (Press X to unplug)";
            color = 0xFF86EFAC;
        } else if (extension.hasCable()) {
            status = "🟡 Tether Installed: Park car & press X to connect";
            color = 0xFFFCD34D;
        } else {
            status = "⚪ Missing Cable: Right-click with Charger Cable";
            color = 0xFF94A3B8;
        }

        renderUnifiedHud(context, client, "🔌", "Charger Extension", 0xFF38BDF8,
                "", 0xFFFFFFFF, status, color, null, null);
    }

    private void renderRcChargerTip(DrawContext context, MinecraftClient client, RcChargerBlockEntity rcCharger) {
        long energy = rcCharger.getEnergy();
        long maxEnergy = rcCharger.getMaxEnergy();
        String status = "⚡ Wireless Pad (16m broadcast radius to parking spots)";

        renderUnifiedHud(context, client, "⚡", "RC Charger Pad", 0xFF38BDF8,
                energy + " / " + maxEnergy + " E", 0xFFFFFFFF, status, 0xFF67E8F9,
                (double) energy / Math.max(1, maxEnergy), 0xFF06B6D4);
    }

    private void renderWireTip(DrawContext context, MinecraftClient client, BlockState state) {
        int conn = 0;
        if (state.get(net.minecraft.state.property.Properties.NORTH)) conn++;
        if (state.get(net.minecraft.state.property.Properties.SOUTH)) conn++;
        if (state.get(net.minecraft.state.property.Properties.EAST)) conn++;
        if (state.get(net.minecraft.state.property.Properties.WEST)) conn++;
        if (state.get(net.minecraft.state.property.Properties.UP)) conn++;
        if (state.get(net.minecraft.state.property.Properties.DOWN)) conn++;

        String status = "🔌 Connected to " + conn + " terminal" + (conn == 1 ? "" : "s");
        renderUnifiedHud(context, client, "🔌", "Power Wire", 0xFFFCD34D,
                "", 0xFFFFFFFF, status, 0xFF94A3B8, null, null);
    }

    private void renderParkingLinesTip(DrawContext context, MinecraftClient client, BlockPos pos, BlockState state) {
        Direction facing = state.get(ParkingLinesBlock.FACING);
        Direction right = facing.rotateYClockwise();
        com.evecual.evecualmc.block.ParkingLinesPart part = state.get(ParkingLinesBlock.PART);
        BlockPos origin = ParkingLinesBlock.getOriginPos(pos, facing, right, part);
        BlockEntity be = client.world != null ? client.world.getBlockEntity(origin) : null;

        boolean occupied = (be instanceof ParkingLinesBlockEntity plbe && plbe.isCarParked());
        String status = occupied ? "🚗 Bay Occupied: Electric car parked" : "🅿️ Bay Free (Press C in car to auto-park)";
        int color = occupied ? 0xFF86EFAC : 0xFFFCD34D;

        renderUnifiedHud(context, client, "🅿️", "Parking Bay", 0xFFFCD34D,
                "", 0xFFFFFFFF, status, color, null, null);
    }

    private void renderRcParkingSpotTip(DrawContext context, MinecraftClient client) {
        String status = "🅿️ Docks & powers down RC Cars/Robots (Charges within 16m of RC Charger)";
        renderUnifiedHud(context, client, "🅿️", "RC Parking Spot", 0xFFFCD34D,
                "", 0xFFFFFFFF, status, 0xFFE2E8F0, null, null);
    }

    private void renderDroneParkingSpotTip(DrawContext context, MinecraftClient client) {
        String status = "🚁 Lands & shuts down RC Drones (Charges within 16m of RC Charger)";
        renderUnifiedHud(context, client, "🚁", "Drone Helipad", 0xFF38BDF8,
                "", 0xFFFFFFFF, status, 0xFFE2E8F0, null, null);
    }

    private void renderCarTip(DrawContext context, MinecraftClient client, CarEntity car) {
        int energy = car.getEnergy();
        int maxEnergy = car.getMaxEnergy();
        int pct = energy * 100 / Math.max(1, maxEnergy);
        String status = "Battery: " + pct + "%" + (energy <= 0 ? " (Empty!)" : " (Drive with WASD)");
        int color = energy > 200 ? 0xFF86EFAC : (energy > 50 ? 0xFFFBBF24 : 0xFFF87171);

        renderUnifiedHud(context, client, "🚗", "Electric Car", 0xFFF87171,
                energy + " / " + maxEnergy + " E", 0xFFFFFFFF, status, color,
                (double) energy / Math.max(1, maxEnergy), 0xFFEF4444);
    }

    private void renderRcCarTip(DrawContext context, MinecraftClient client, RcCarEntity rcCar) {
        int energy = rcCar.getEnergy();
        int max = RcCarEntity.MAX_ENERGY;
        int pct = energy * 100 / Math.max(1, max);
        String status = "Battery: " + pct + "% | Pair with RC Controller";

        renderUnifiedHud(context, client, "🏎️", "RC Car", 0xFF38BDF8,
                energy + " / " + max + " E", 0xFFFFFFFF, status, 0xFF67E8F9,
                (double) energy / Math.max(1, max), 0xFF0284C7);
    }

    private void renderRcDroneTip(DrawContext context, MinecraftClient client, RcDroneEntity drone) {
        int energy = drone.getEnergy();
        int max = RcDroneEntity.MAX_ENERGY;
        int pct = energy * 100 / Math.max(1, max);
        String status = "Battery: " + pct + "% | Aerial flight range: 512m";

        renderUnifiedHud(context, client, "🚁", "RC Drone", 0xFF34D399,
                energy + " / " + max + " E", 0xFFFFFFFF, status, 0xFF86EFAC,
                (double) energy / Math.max(1, max), 0xFF10B981);
    }

    private void renderRcRobotTip(DrawContext context, MinecraftClient client, RcRobotEntity robot) {
        int energy = robot.getEnergy();
        int max = RcRobotEntity.MAX_ENERGY;
        int pct = energy * 100 / Math.max(1, max);
        ItemStack tool = robot.getEquippedTool();
        String toolName = tool.isEmpty() ? "None" : tool.getName().getString();
        String status = "Tool: " + toolName + " | Battery: " + pct + "%";

        renderUnifiedHud(context, client, "🤖", "RC Robot", 0xFFFBBF24,
                energy + " / " + max + " E", 0xFFFFFFFF, status, 0xFFFDE047,
                (double) energy / Math.max(1, max), 0xFFF59E0B);
    }

    private void drawProgressBar(DrawContext context, int x, int y, int width, int height, double ratio, int fillColor) {
        context.fill(x, y, x + width, y + height, 0xFF1E293B);
        int filledWidth = (int) Math.round(width * Math.max(0.0, Math.min(1.0, ratio)));
        if (filledWidth > 0) {
            context.fill(x, y, x + filledWidth, y + height, fillColor);
        }
        context.drawBorder(x, y, width, height, 0xFF475569);
    }
}
