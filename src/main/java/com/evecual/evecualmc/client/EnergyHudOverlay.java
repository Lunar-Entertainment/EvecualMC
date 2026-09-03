package com.evecual.evecualmc.client;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class EnergyHudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) return;
        if (client.options.hudHidden) return;

        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);
        BlockEntity be = client.world.getBlockEntity(pos);

        if (be instanceof SolarPanelBlockEntity solar) {
            renderSolarTip(drawContext, client, solar);
        } else if (be instanceof BatteryBlockEntity battery) {
            renderBatteryTip(drawContext, client, battery);
        } else if (state.isOf(EvecualMC.WIRE_BLOCK)) {
            renderWireTip(drawContext, client);
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

        // Background & glowing border
        context.fill(x, y, x + boxWidth, y + boxHeight, 0xE00F172A);
        context.drawBorder(x, y, boxWidth, boxHeight, 0xFFF59E0B);

        TextRenderer tr = client.textRenderer;

        // Title and energy amount
        context.drawText(tr, Text.literal("☀ Solar Panel"), x + 8, y + 5, 0xFFFDE047, true);
        String energyStr = energy + " / " + maxEnergy + " E";
        context.drawText(tr, Text.literal(energyStr), x + boxWidth - 8 - tr.getWidth(energyStr), y + 5, 0xFFF1F5F9, true);

        // Subtitle / generation status
        String genStr = genRate > 0 ? "⚡ Generating: +" + genRate + " E/s" : "🌙 Inactive (No Sunlight)";
        int statusColor = genRate > 0 ? 0xFF86EFAC : 0xFF94A3B8;
        context.drawText(tr, Text.literal(genStr), x + 8, y + 16, statusColor, true);

        // Progress bar
        int barX = x + 8;
        int barY = y + 28;
        int barWidth = boxWidth - 16;
        int barHeight = 8;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF1E293B);
        int filledWidth = (int) Math.round(barWidth * ((double) energy / Math.max(1, maxEnergy)));
        if (filledWidth > 0) {
            context.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFFFBBF24);
        }
        context.drawBorder(barX, barY, barWidth, barHeight, 0xFF475569);
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

        int barX = x + 8;
        int barY = y + 28;
        int barWidth = boxWidth - 16;
        int barHeight = 8;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF1E293B);
        int filledWidth = (int) Math.round(barWidth * ((double) energy / Math.max(1, maxEnergy)));
        if (filledWidth > 0) {
            context.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF22C55E);
        }
        context.drawBorder(barX, barY, barWidth, barHeight, 0xFF475569);
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
}
