package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.ElectronicDuperScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ElectronicDuperScreen extends HandledScreen<ElectronicDuperScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    public ElectronicDuperScreen(ElectronicDuperScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Dark Glassmorphic Backdrop
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF00A0F1D);
        // Outer Glowing Cyan Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF00E5FF);
        // Inner Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 16, 0xFF142238);

        // Header Title
        context.drawText(this.textRenderer, "⚡ ELECTRONIC DUPER", x + 10, y + 4, 0xFF00E5FF, false);

        // Input Slot Frame (Slot 0, x: 44, y: 35)
        context.fill(x + 43, y + 34, x + 61, y + 52, 0xFF0F1A2E);
        context.drawBorder(x + 43, y + 34, 18, 18, 0xFF00E5FF);

        // Output Slot Frame (Slot 1, x: 132, y: 35)
        context.fill(x + 131, y + 34, x + 149, y + 52, 0xFF0F1A2E);
        context.drawBorder(x + 131, y + 34, 18, 18, 0xFFFFD700);

        // 1. Energy Storage Gauge Bar (x: 16, y: 24, w: 14, h: 48)
        int energy = this.handler.getEnergy();
        int maxEnergy = 3000;
        float energyRatio = Math.min(1.0F, (float) energy / (float) maxEnergy);
        int energyFillHeight = (int) (46 * energyRatio);

        context.fill(x + 16, y + 24, x + 30, y + 72, 0xFF080D1A);
        context.drawBorder(x + 16, y + 24, 14, 48, 0xFF00E5FF);

        // Required 1500 EU Threshold Line
        int reqY = y + 72 - (int) (46 * (1500.0F / 3000.0F));
        context.fill(x + 17, reqY, x + 29, reqY + 1, 0xFFFFD700);

        if (energyFillHeight > 0) {
            context.fill(x + 17, y + 71 - energyFillHeight, x + 29, y + 71, 0xFF00E5FF);
        }

        // 2. Quantum Duplication Progress Beam (x: 68, y: 39, w: 56, h: 10)
        int progress = this.handler.getProgressTicks();
        int maxProgress = 2400; // 2 minutes
        boolean isDuplicating = this.handler.isDuplicating();
        float progressRatio = isDuplicating ? Math.min(1.0F, (float) progress / (float) maxProgress) : 0.0F;
        int progressFillWidth = (int) (54 * progressRatio);

        context.fill(x + 68, y + 39, x + 124, y + 49, 0xFF080D1A);
        context.drawBorder(x + 68, y + 39, 56, 10, 0xFF00E5FF);

        if (progressFillWidth > 0) {
            context.fill(x + 69, y + 40, x + 69 + progressFillWidth, y + 48, 0xFFFFD700);
        }

        // Status Message below slots
        if (isDuplicating) {
            int pct = (int) (progressRatio * 100);
            int remSec = Math.max(0, (maxProgress - progress) / 20);
            int mins = remSec / 60;
            int secs = remSec % 60;
            String timeStr = String.format("%dm %02ds", mins, secs);
            context.drawText(this.textRenderer, "🌀 Duplicating... " + pct + "% (" + timeStr + ")", x + 40, y + 62, 0xFFFFD700, false);
        } else {
            if (energy < 1500) {
                context.drawText(this.textRenderer, "⚡ Needs 1500 EU (Stored: " + energy + " EU)", x + 38, y + 62, 0xFFFFAA00, false);
            } else {
                context.drawText(this.textRenderer, "⚡ Ready - Insert Item & 1500 EU", x + 38, y + 62, 0xFF55FF55, false);
            }
        }

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 83 + i * 18, x + 23 + j * 18, y + 99 + i * 18, 0xFF0F1A2E);
                context.drawBorder(x + 7 + j * 18, y + 83 + i * 18, 16, 16, 0xFF1C3150);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 141, x + 23 + i * 18, y + 157, 0xFF0F1A2E);
            context.drawBorder(x + 7 + i * 18, y + 141, 16, 16, 0xFF00E5FF);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Energy Gauge Tooltip
        if (mouseX >= x + 16 && mouseX <= x + 30 && mouseY >= y + 24 && mouseY <= y + 72) {
            context.drawTooltip(this.textRenderer, Text.literal("⚡ Stored Energy: " + this.handler.getEnergy() + " / 3000 EU\nRequired per cycle: 1500 EU"), mouseX, mouseY);
        }
    }
}
