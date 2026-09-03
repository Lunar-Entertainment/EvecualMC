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

public class EnergyHudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) return;
        if (client.options.hudHidden) return;

        HitResult hit = client.crosshairTarget;
        if (hit == null) return;

        // Check if looking at an entity (like the electric car)
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof CarEntity car) {
                renderCarTip(drawContext, client, car);
                return;
            }
        }

        // Check if riding a car
        if (client.player.getVehicle() instanceof CarEntity car) {
            renderCarTip(drawContext, client, car);
            return;
        }

        // Check if looking at a block
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
                renderChargerTip(drawContext, client, charger);
            } else if (state.isOf(EvecualMC.WIRE_BLOCK)) {
                renderWireTip(drawContext, client);
            }
        }
    }

    private void renderSolarTip(DrawContext context, MinecraftClient client, SolarPanelBlockEntity solar) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 150;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 14;

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
        int y = 14;

        long energy = battery.getEnergy();
        long maxEnergy = battery.getMaxEnergy();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFF10B981);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("🔋 Battery"), x + 8, y + 5, 0xFF6EE7B7, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        int percent = (int) Math.round(((double) energy / maxEnergy) * 100);
        context.drawText(tr, Text.literal("Capacity: " + percent + "%"), x + 8, y + 16, 0xFF94A3B8, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFF22C55E);
    }

    private void renderCombinerTip(DrawContext context, MinecraftClient client, ElectronicCombinerBlockEntity combiner) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 160;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 14;

        long energy = combiner.getEnergy();
        long maxEnergy = combiner.getMaxEnergy();
        int progress = combiner.getProgress();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFF38BDF8);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("⚙ Combiner"), x + 8, y + 5, 0xFF7DD3FC, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        String statusStr = progress > 0 ? "Assembling Car (" + progress + "%)" : "Right-click to Open";
        context.drawText(tr, Text.literal(statusStr), x + 8, y + 16, 0xFF94A3B8, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFF38BDF8);
    }

    private void renderChargerTip(DrawContext context, MinecraftClient client, ChargerBlockEntity charger) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 150;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 14;

        long energy = charger.getEnergy();
        long maxEnergy = charger.getMaxEnergy();
        boolean charging = charger.isCharging();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFF22C55E);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("⚡ Car Charger"), x + 8, y + 5, 0xFF86EFAC, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        String status = charging ? "⚡ Charging Vehicle..." : "Ready (Park car near)";
        int color = charging ? 0xFFFDE047 : 0xFF94A3B8;
        context.drawText(tr, Text.literal(status), x + 8, y + 16, color, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFF22C55E);
    }

    private void renderCarTip(DrawContext context, MinecraftClient client, CarEntity car) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 150;
        int boxHeight = 44;
        int x = (screenWidth - boxWidth) / 2;
        int y = 14;

        int energy = car.getEnergy();
        int maxEnergy = car.getMaxEnergy();

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFEF4444);

        TextRenderer tr = client.textRenderer;
        context.drawText(tr, Text.literal("🚗 Electric Car"), x + 8, y + 5, 0xFFFCA5A5, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        int percent = (int) Math.round(((double) energy / maxEnergy) * 100);
        String status = energy > 0 ? "Battery: " + percent + "%" : "⚡ Out of Energy (Needs Charge!)";
        int color = energy > 0 ? 0xFF86EFAC : 0xFFF87171;
        context.drawText(tr, Text.literal(status), x + 8, y + 16, color, true);

        drawProgressBar(context, x + 8, y + 28, boxWidth - 16, 8, (double) energy / Math.max(1, maxEnergy), 0xFFEF4444);
    }

    private void renderWireTip(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 130;
        int boxHeight = 24;
        int x = (screenWidth - boxWidth) / 2;
        int y = 14;

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
