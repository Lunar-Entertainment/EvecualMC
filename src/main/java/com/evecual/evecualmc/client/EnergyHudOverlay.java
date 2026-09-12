package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ParkingLinesBlock;
import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import com.evecual.evecualmc.block.entity.ChargerBlockEntity;
import com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity;
import com.evecual.evecualmc.block.entity.RcChargerBlockEntity;
import com.evecual.evecualmc.block.entity.HeliChargerBlockEntity;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import com.evecual.evecualmc.block.entity.StationaryRcControllerBlockEntity;
import com.evecual.evecualmc.entity.CarEntity;
import com.evecual.evecualmc.entity.HeliEntity;
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

        // 2. Check if riding a car or heli
        if (client.player.getVehicle() instanceof CarEntity car) {
            renderCarTip(drawContext, client, car);
            return;
        }
        if (client.player.getVehicle() instanceof HeliEntity heli) {
            renderHeliTip(drawContext, client, heli);
            return;
        }

        if (hit == null) return;

        // 3. Check if looking at an entity
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof CarEntity car) {
                renderCarTip(drawContext, client, car);
                return;
            } else if (entity instanceof HeliEntity heli) {
                renderHeliTip(drawContext, client, heli);
                return;
            } else if (entity instanceof RcCarEntity rcCar) {
                renderRcCarTip(drawContext, client, rcCar);
                return;
            } else if (entity instanceof com.evecual.evecualmc.entity.PickupDroneEntity pickupDrone) {
                renderPickupDroneTip(drawContext, client, pickupDrone);
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
            } else if (be instanceof com.evecual.evecualmc.block.entity.ElectronicDuperBlockEntity duper) {
                renderDuperTip(drawContext, client, duper);
            } else if (be instanceof HeliChargerBlockEntity heliCharger) {
                renderHeliChargerTip(drawContext, client, heliCharger);
            } else if (state.isOf(EvecualMC.WIND_TURBINE_BLOCK) || be instanceof com.evecual.evecualmc.block.entity.WindTurbineBlockEntity) {
                renderWindTurbineTip(drawContext, client, pos, state, be);
            } else if (state.isOf(EvecualMC.WIRE_BLOCK)) {
                renderWireTip(drawContext, client, pos, state, be);
            } else if (state.isOf(EvecualMC.PARKING_LINES_BLOCK)) {
                renderParkingLinesTip(drawContext, client, pos, state);
            } else if (state.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK)) {
                renderRcParkingSpotTip(drawContext, client);
            } else if (state.isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK)) {
                renderDroneParkingSpotTip(drawContext, client);
            } else if (state.isOf(EvecualMC.ROBOT_PARKING_SPOT_BLOCK)) {
                renderRobotParkingSpotTip(drawContext, client);
            } else if (state.isOf(EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK)) {
                renderPickupDroneParkingSpotTip(drawContext, client, pos, state);
            } else if (be instanceof com.evecual.evecualmc.block.entity.DronePickupBlockEntity dronePickup) {
                renderDronePickupTip(drawContext, client, dronePickup);
            } else if (be instanceof com.evecual.evecualmc.block.entity.ElectricChuteBlockEntity chute) {
                renderElectricChuteTip(drawContext, client, chute);
            } else if (be instanceof com.evecual.evecualmc.block.entity.StorageUnitBlockEntity storageUnit) {
                renderStorageUnitTip(drawContext, client, storageUnit);
            } else if (be instanceof com.evecual.evecualmc.block.entity.StationaryTurretBlockEntity turret) {
                renderStationaryTurretTip(drawContext, client, turret);
            } else if (be instanceof com.evecual.evecualmc.block.entity.TurretAmmoContainerBlockEntity ammoContainer) {
                renderTurretAmmoContainerTip(drawContext, client, ammoContainer);
            } else if (be instanceof StationaryRcControllerBlockEntity station) {
                renderStationaryRcControllerTip(drawContext, client, station);
            }
        }
    }

    private void renderDuperTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.block.entity.ElectronicDuperBlockEntity duper) {
        long energy = duper.getEnergy();
        long maxEnergy = duper.getMaxEnergy();
        int maxTicks = duper.getTotalTicks();
        int reqEnergy = duper.getEnergyCost();
        int progress = duper.getProgressTicks();

        String status;
        if (duper.isDuplicating()) {
            int pct = (int) (((float) progress / Math.max(1, maxTicks)) * 100);
            int remTicks = Math.max(0, maxTicks - progress);
            String timeStr = com.evecual.evecualmc.util.DuperRarityHelper.formatDuration(remTicks);
            status = "🌀 Duplicating... " + pct + "% (" + timeStr + " left)";
        } else {
            ItemStack stack = duper.getStack(0);
            if (!stack.isEmpty()) {
                String durStr = com.evecual.evecualmc.util.DuperRarityHelper.formatDuration(maxTicks);
                status = energy >= reqEnergy
                        ? "⚡ Ready: " + durStr + " (" + reqEnergy + " EU)"
                        : "⚡ Need Energy: " + reqEnergy + " EU (Need " + (reqEnergy - energy) + " more)";
            } else {
                status = "⚡ Idle - Insert Item to Duplicate";
            }
        }
        int color = duper.isDuplicating() ? 0xFF86EFAC : (energy >= reqEnergy ? 0xFF67E8F9 : 0xFFF87171);

        renderUnifiedHud(context, client, "🌀", "Electronic Duper", 0xFF00E5FF,
                energy + " / " + maxEnergy + " EU", 0xFFFFFFFF, status, color,
                duper.isDuplicating() ? (double) progress / Math.max(1, maxTicks) : null,
                duper.isDuplicating() ? 0xFF06B6D4 : null);
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
        String status = "⚡ Wireless Induction Station (16m radius to parking spots)";

        renderUnifiedHud(context, client, "⚡", "Wireless RC Charger", 0xFF38BDF8,
                energy + " / " + maxEnergy + " EU", 0xFFFFFFFF, status, 0xFF67E8F9,
                (double) energy / Math.max(1, maxEnergy), 0xFF06B6D4);
    }

    private void renderWindTurbineTip(DrawContext context, MinecraftClient client, BlockPos pos, BlockState state, BlockEntity be) {
        String rightHeader = "50 EU/t";
        String status = "⚡ Generating: 50 EU/t (4-Block High Turbine Tower)";
        renderUnifiedHud(context, client, "⚡", "Wind Turbine", 0xFF38BDF8,
                rightHeader, 0xFFFFFFFF, status, 0xFF86EFAC,
                1.0, 0xFF0284C7);
    }

    private void renderWireTip(DrawContext context, MinecraftClient client, BlockPos pos, BlockState state, BlockEntity be) {
        int conn = 0;
        if (state.get(net.minecraft.state.property.Properties.NORTH)) conn++;
        if (state.get(net.minecraft.state.property.Properties.SOUTH)) conn++;
        if (state.get(net.minecraft.state.property.Properties.EAST)) conn++;
        if (state.get(net.minecraft.state.property.Properties.WEST)) conn++;
        if (state.get(net.minecraft.state.property.Properties.UP)) conn++;
        if (state.get(net.minecraft.state.property.Properties.DOWN)) conn++;

        int rate = (be instanceof com.evecual.evecualmc.block.entity.WireBlockEntity wbe) ? wbe.getTransferRate() : 0;
        String rightHeader = rate > 0 ? "⚡ " + rate + " EU/t" : "0 EU/t";
        String status = rate > 0
                ? "⚡ Active: Conduiting " + rate + " EU/t (" + conn + " terminals)"
                : "🔌 Standby: Connected to " + conn + " terminal" + (conn == 1 ? "" : "s");
        int statusColor = rate > 0 ? 0xFF86EFAC : 0xFF94A3B8;

        renderUnifiedHud(context, client, "🔌", "Power Wire", 0xFFFCD34D,
                rightHeader, 0xFFFFFFFF, status, statusColor,
                rate > 0 ? 1.0 : null, rate > 0 ? 0xFFF59E0B : null);
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
        String status = "🅿️ Docks & powers down RC Cars (Charges within 16m of Wireless Charger)";
        renderUnifiedHud(context, client, "🅿️", "RC Parking Spot", 0xFFFCD34D,
                "", 0xFFFFFFFF, status, 0xFFE2E8F0, null, null);
    }

    private void renderDroneParkingSpotTip(DrawContext context, MinecraftClient client) {
        String status = "🚁 Lands & shuts down RC Drones (Charges within 16m of Wireless Charger)";
        renderUnifiedHud(context, client, "🚁", "Drone Helipad", 0xFF38BDF8,
                "", 0xFFFFFFFF, status, 0xFFE2E8F0, null, null);
    }

    private void renderRobotParkingSpotTip(DrawContext context, MinecraftClient client) {
        String status = "🤖 Docks & powers down RC Robots (Charges within 16m of Wireless Charger)";
        renderUnifiedHud(context, client, "🤖", "Robot Parking Spot", 0xFFF97316,
                "", 0xFFFFFFFF, status, 0xFFE2E8F0, null, null);
    }

    private void renderStationaryRcControllerTip(DrawContext context, MinecraftClient client, StationaryRcControllerBlockEntity terminal) {
        String pairedName = terminal.getVehicleName();
        boolean paired = terminal.getPairedVehicleUuid() != null;
        String status = paired ? "📡 Linked: " + pairedName + " (Right-Click to Operate)" : "⚠️ Not Paired (Pair in hand before placing)";
        int color = paired ? 0xFF86EFAC : 0xFFF87171;
        renderUnifiedHud(context, client, "🖥️", "Stationary RC Controller", 0xFF60A5FA,
                paired ? "LINKED" : "OFFLINE", 0xFFFFFFFF, status, color, null, null);
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

    private void renderHeliTip(DrawContext context, MinecraftClient client, HeliEntity heli) {
        int energy = heli.getEnergy();
        int maxEnergy = heli.getMaxEnergy();
        int pct = energy * 100 / Math.max(1, maxEnergy);
        double speedBps = heli.getCurrentSpeed() * 20.0;
        String speedStr = String.format("%.1f m/s", speedBps);
        boolean boost = client.options.sprintKey.isPressed() || net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL);
        String flightStatus = heli.isCharging() ? "⚡ CHARGING ON 3x3 HELIPAD" :
                (heli.isAutoReturning() ? "🚁 AUTOPILOT -> HELIPAD (" + speedStr + ")" :
                (heli.isInFlight() ? (boost ? "💨 BOOST: " + speedStr : "🚁 FLYING: " + speedStr) : "🅿️ LANDED | Battery: " + pct + "%"));
        int color = heli.isCharging() ? 0xFF86EFAC : (heli.isAutoReturning() ? 0xFFFBBF24 : (energy > 200 ? 0xFF38BDF8 : 0xFFF87171));

        renderUnifiedHud(context, client, "🚁", "EV Helicopter", 0xFF38BDF8,
                energy + " / " + maxEnergy + " EU", 0xFFFFFFFF, flightStatus, color,
                (double) energy / Math.max(1, maxEnergy), 0xFF0284C7);
    }

    private void renderHeliChargerTip(DrawContext context, MinecraftClient client, HeliChargerBlockEntity heliCharger) {
        int energy = heliCharger.getStoredEnergy();
        int max = heliCharger.getMaxEnergy();
        String status = "🚁 Rapid Inductive Helipad (Place on Vehicle Charger Base to power)";
        renderUnifiedHud(context, client, "⚡", "Heli Charger", 0xFF38BDF8,
                energy + " / " + max + " EU", 0xFFFFFFFF, status, 0xFF67E8F9,
                (double) energy / Math.max(1, max), 0xFF0284C7);
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

    private void renderPickupDroneTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.entity.PickupDroneEntity drone) {
        int energy = drone.getEnergy();
        int max = RcDroneEntity.MAX_ENERGY;
        int pct = energy * 100 / Math.max(1, max);
        int filledSlots = 0;
        int totalItems = 0;
        for (int i = 0; i < drone.getTrunk().size(); i++) {
            ItemStack stack = drone.getTrunk().getStack(i);
            if (!stack.isEmpty()) {
                filledSlots++;
                totalItems += stack.getCount();
            }
        }
        String status = "🛡️ Cargo: " + totalItems + " items (" + filledSlots + "/" + drone.getTrunk().size() + " slots) | Vacuum Active";

        renderUnifiedHud(context, client, "🛡️", "Pickup Drone", 0xFF38BDF8,
                energy + " / " + max + " E", 0xFFFFFFFF, status, 0xFF67E8F9,
                (double) energy / Math.max(1, max), 0xFF0284C7);
    }

    private void renderPickupDroneParkingSpotTip(DrawContext context, MinecraftClient client, BlockPos pos, BlockState state) {
        boolean onPickup = client.world != null && client.world.getBlockState(pos.down()).isOf(EvecualMC.DRONE_PICKUP_BLOCK);
        String status = onPickup
                ? "🛡️ Specialized Dock: Ready for Pickup Drone (Press C in drone to land)"
                : "⚠️ Invalid Spot: Must be placed on a Drone Pickup Station";
        int color = onPickup ? 0xFF86EFAC : 0xFFF87171;
        renderUnifiedHud(context, client, "🛡️", "Pickup Drone Landing Pad", 0xFFF59E0B,
                onPickup ? "ACTIVE" : "INVALID", 0xFFFFFFFF, status, color, null, null);
    }

    private void renderDronePickupTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.block.entity.DronePickupBlockEntity dpbe) {
        String status = dpbe.getStatusMessage();
        com.evecual.evecualmc.entity.PickupDroneEntity drone = dpbe.getParkedDrone();
        int color = drone != null ? 0xFF86EFAC : (status.startsWith("⚠️") ? 0xFFF87171 : 0xFF38BDF8);
        String val = drone != null ? "DOCKED" : "READY";
        renderUnifiedHud(context, client, "📥", "Drone Pickup Station", 0xFF38BDF8,
                val, 0xFFFFFFFF, status, color, null, null);
    }

    private void renderElectricChuteTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.block.entity.ElectricChuteBlockEntity chute) {
        long energy = chute.getEnergy();
        long max = chute.getMaxEnergy();
        String status = chute.getStatusMessage();
        int color = status.startsWith("⚡ Pneumatic") || status.startsWith("🟢") ? 0xFF86EFAC : (status.startsWith("⚡ Unpowered") ? 0xFFF87171 : 0xFFFBBF24);
        renderUnifiedHud(context, client, "⚡", "Electric Chute", 0xFF00E5FF,
                energy + " / " + max + " EU", 0xFFFFFFFF, status, color,
                (double) energy / Math.max(1, max), 0xFF0891B2);
    }

    private void renderStorageUnitTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.block.entity.StorageUnitBlockEntity storage) {
        long energy = storage.getEnergy();
        long max = storage.getMaxEnergy();
        var cluster = storage.findConnectedCluster();
        int clusterSize = cluster.size();
        int totalSlots = clusterSize * com.evecual.evecualmc.block.entity.StorageUnitBlockEntity.SLOTS_PER_UNIT;
        int filledSlots = 0;
        int totalItems = 0;
        for (var unit : cluster) {
            int uFilled = unit.getFilledSlotCount();
            int uTotal = unit.getTotalItemCount();
            if (uTotal == 0 && uFilled == 0) {
                for (int i = 0; i < unit.size(); i++) {
                    ItemStack s = unit.getStack(i);
                    if (!s.isEmpty()) {
                        uFilled++;
                        uTotal += s.getCount();
                    }
                }
            }
            filledSlots += uFilled;
            totalItems += uTotal;
        }
        String status;
        int color;
        if (storage.isLockedDueToPower()) {
            status = "🔒 Locked: Connect ≥ 200 EU to initialize quantum matrix";
            color = 0xFFF87171;
        } else if (storage.isElectricallyCharged()) {
            status = "⚡ Charged (" + totalItems + " items in " + filledSlots + "/" + totalSlots + " slots) | Retention OK";
            color = 0xFF86EFAC;
        } else {
            status = "⚠️ Uncharged (" + totalItems + " items in " + filledSlots + "/" + totalSlots + " slots) | Needs ≥ 200 EU to retain";
            color = 0xFFFBBF24;
        }

        renderUnifiedHud(context, client, "📦", "Storage Unit" + (clusterSize > 1 ? " (" + clusterSize + " Linked)" : ""), 0xFF38BDF8,
                energy + " / " + max + " EU", 0xFFFFFFFF, status, color,
                (double) energy / Math.max(1, max), 0xFF0284C7);
    }

    private void renderStationaryTurretTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.block.entity.StationaryTurretBlockEntity turret) {
        BlockPos linkedPos = turret.getLinkedAmmoContainerPos();
        boolean linked = linkedPos != null;
        int radius = turret.getTargetFilter().getRadius();
        String status;
        int color;
        if (!linked) {
            status = "⚠️ Not linked to Ammo Container (Use Turret Linker on container, then turret)";
            color = 0xFFF87171;
        } else {
            int dist = (int) Math.sqrt(turret.getPos().getSquaredDistance(linkedPos));
            status = "🎯 Defense Sentry Active: Scanning (" + radius + "m radius | Ammo Depot: " + dist + "m)";
            color = 0xFF86EFAC;
        }
        renderUnifiedHud(context, client, "🎯", "Stationary Turret", 0xFFF87171,
                linked ? "LINKED" : "UNLINKED", 0xFFFFFFFF, status, color, null, null);
    }

    private void renderTurretAmmoContainerTip(DrawContext context, MinecraftClient client, com.evecual.evecualmc.block.entity.TurretAmmoContainerBlockEntity container) {
        int count = 0;
        int copper = 0;
        int iron = 0;
        int diamond = 0;
        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getStack(i);
            if (!stack.isEmpty()) {
                count += stack.getCount();
                if (stack.isOf(EvecualMC.COPPER_AMMO)) copper += stack.getCount();
                else if (stack.isOf(EvecualMC.IRON_AMMO)) iron += stack.getCount();
                else if (stack.isOf(EvecualMC.DIAMOND_AMMO)) diamond += stack.getCount();
            }
        }
        int turrets = container.getLinkedStationaryTurrets().size();
        String status = "Feeds " + turrets + " Turret" + (turrets == 1 ? "" : "s") + " (" + count + " total: " + copper + " Cu, " + iron + " Fe, " + diamond + " Dia)";
        int color = count > 0 ? 0xFF86EFAC : 0xFFFBBF24;
        renderUnifiedHud(context, client, "📦", "Turret Ammo Container", 0xFFF59E0B,
                count + " Ammo", 0xFFFFFFFF, status, color, null, null);
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
