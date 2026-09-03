package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import com.evecual.evecualmc.block.entity.ChargerBlockEntity;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import com.evecual.evecualmc.entity.CarEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EnergyHudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) return;
        if (client.options.hudHidden) return;

        // 1. Render active Vehicle Charger Waypoint
        renderChargerWaypoints(drawContext, client);

        HitResult hit = client.crosshairTarget;
        if (hit == null) return;

        // 2. Check if looking at an entity (like the electric car)
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof CarEntity car) {
                renderCarTip(drawContext, client, car);
                return;
            }
        }

        // 3. Check if riding a car
        if (client.player.getVehicle() instanceof CarEntity car) {
            renderCarTip(drawContext, client, car);
            return;
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
            } else if (state.isOf(EvecualMC.WIRE_BLOCK)) {
                renderWireTip(drawContext, client);
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

    private void renderSolarTip(DrawContext context, MinecraftClient client, SolarPanelBlockEntity solar) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 150;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 24;

        long energy = solar.getEnergy();
        long maxEnergy = solar.getMaxEnergy();
        int genRate = solar.getGenerationRate();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFF59E0B);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("☀ Solar Panel"), x + 8, y + 5, 0xFFFDE047, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        String genStr = genRate > 0 ? "⚡ Generating: +" + genRate + " E/s" : "🌙 Inactive (No Sunlight)";
        int statusColor = genRate > 0 ? 0xFF86EFAC : 0xFF94A3B8;
        context.drawText(tr, Text.literal(genStr), x + 8, y + 16, statusColor, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFFFBBF24);
    }

    private void renderBatteryTip(DrawContext context, MinecraftClient client, BatteryBlockEntity battery) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 150;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 24;

        long energy = battery.getEnergy();
        long maxEnergy = battery.getMaxEnergy();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFF3B82F6);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("🔋 Battery"), x + 8, y + 5, 0xFF60A5FA, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        int pct = (int) (energy * 100 / Math.max(1, maxEnergy));
        context.drawText(tr, Text.literal("Charge: " + pct + "%"), x + 8, y + 16, 0xFF93C5FD, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFF3B82F6);
    }

    private void renderCombinerTip(DrawContext context, MinecraftClient client, ElectronicCombinerBlockEntity combiner) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 160;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 24;

        long energy = combiner.getEnergy();
        long maxEnergy = combiner.getMaxEnergy();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFA855F7);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("⚙ Combiner"), x + 8, y + 5, 0xFFC084FC, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        String status = combiner.isCrafting() ? "⚡ Assembling Car..." : "Idle (Needs Items/Power)";
        int color = combiner.isCrafting() ? 0xFF86EFAC : 0xFF94A3B8;
        context.drawText(tr, Text.literal(status), x + 8, y + 16, color, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFFA855F7);
    }

    private void renderChargerTip(DrawContext context, MinecraftClient client, ChargerBlockEntity charger) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 160;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 24;

        long energy = charger.getEnergy();
        long maxEnergy = charger.getMaxEnergy();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFF10B981);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("⚡ Vehicle Charger"), x + 8, y + 5, 0xFF34D399, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        String status = charger.isCharging() ? "⚡ Charging Vehicle..." : "Ready (Park Car Nearby)";
        int color = charger.isCharging() ? 0xFF86EFAC : 0xFF94A3B8;
        context.drawText(tr, Text.literal(status), x + 8, y + 16, color, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFF10B981);
    }

    private void renderCarTip(DrawContext context, MinecraftClient client, CarEntity car) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 160;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 24;

        int energy = car.getEnergy();
        int maxEnergy = car.getMaxEnergy();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFEF4444);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("🚗 Electric Car"), x + 8, y + 5, 0xFFF87171, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        int pct = energy * 100 / Math.max(1, maxEnergy);
        String status = "Battery: " + pct + "%" + (energy <= 0 ? " (Empty!)" : "");
        int color = energy > 200 ? 0xFF86EFAC : (energy > 50 ? 0xFFFBBF24 : 0xFFF87171);
        context.drawText(tr, Text.literal(status), x + 8, y + 16, color, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFFEF4444);
    }

    private void renderWireTip(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 130;
        int boxHeight = 24;
        int x = (screenWidth - boxWidth) / 2;
        int y = 24;

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFF59E0B);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("🔌 Electrical Wire"), x + 8, y + 4, 0xFFFCD34D, true);
        context.drawText(tr, Text.literal("Connects Power"), x + 8, y + 14, 0xFF94A3B8, true);
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
