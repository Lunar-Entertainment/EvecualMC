package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.block.entity.MaterializerBlockEntity;
import com.evecual.evecualmc.screen.MaterializerScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class MaterializerScreen extends HandledScreen<MaterializerScreenHandler> {

    public MaterializerScreen(MaterializerScreenHandler handler, PlayerInventory inventory, Text title) {
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
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF0080B14);
        // Outer Glowing Violet Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF9333EA);
        // Inner Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 16, 0xFF141926);

        // Header Title
        context.drawText(this.textRenderer, "⚡ QUANTUM MATERIALIZER", x + 8, y + 4, 0xFF00FFFF, false);

        // Input Slot A Frame (Slot 0, x: 44, y: 35)
        context.fill(x + 43, y + 34, x + 61, y + 52, 0xFF0F172A);
        context.drawBorder(x + 43, y + 34, 18, 18, 0xFF00E5FF);

        // Input Slot B Frame (Slot 1, x: 68, y: 35)
        context.fill(x + 67, y + 34, x + 85, y + 52, 0xFF0F172A);
        context.drawBorder(x + 67, y + 34, 18, 18, 0xFF00E5FF);

        // Output Slot Frame (Slot 2, x: 132, y: 35)
        context.fill(x + 131, y + 34, x + 149, y + 52, 0xFF1A132B);
        context.drawBorder(x + 131, y + 34, 18, 18, 0xFFFFD700);

        // 1. Energy Storage Gauge Bar (x: 16, y: 24, w: 14, h: 48)
        int energy = this.handler.getEnergy();
        int maxEnergy = this.handler.getMaxEnergy();
        float energyRatio = Math.min(1.0F, (float) energy / (float) Math.max(1, maxEnergy));
        int energyFillHeight = (int) (46 * energyRatio);

        context.fill(x + 16, y + 24, x + 30, y + 72, 0xFF060912);
        context.drawBorder(x + 16, y + 24, 14, 48, 0xFF00E5FF);

        if (energyFillHeight > 0) {
            context.fill(x + 17, y + 71 - energyFillHeight, x + 29, y + 71, 0xFF00E5FF);
        }

        // 2. Quantum Convergence Progress Bar (x: 89, y: 38, w: 38, h: 12)
        int progress = this.handler.getProgressTicks();
        int maxProgress = this.handler.getTotalTicks();
        boolean isActive = this.handler.isActive();
        int recipeType = this.handler.getRecipeType();

        float progressRatio = (maxProgress > 0) ? Math.min(1.0F, (float) progress / (float) maxProgress) : 0.0F;
        int progressFillWidth = (int) (36 * progressRatio);

        context.fill(x + 89, y + 38, x + 127, y + 50, 0xFF080B16);
        context.drawBorder(x + 89, y + 38, 38, 12, 0xFFA855F7);

        if (progressFillWidth > 0) {
            int barColor = (recipeType == MaterializerBlockEntity.RECIPE_ELACTORITE) ? 0xFFA855F7 : 0xFFFFD700;
            context.fill(x + 90, y + 39, x + 90 + progressFillWidth, y + 49, barColor);
        }

        // Status text & instructions below slots
        if (isActive) {
            int pct = (int) (progressRatio * 100);
            int remTicks = Math.max(0, maxProgress - progress);
            int remSecTotal = remTicks / 20;
            int remMin = remSecTotal / 60;
            int remSec = remSecTotal % 60;
            String timeStr = String.format("%02d:%02d", remMin, remSec);

            if (recipeType == MaterializerBlockEntity.RECIPE_ELACTORITE) {
                context.drawText(this.textRenderer, "🌀 Elactorite: " + pct + "% (" + timeStr + ")", x + 38, y + 58, 0xFFA855F7, false);
                context.drawText(this.textRenderer, "Quantum Synthesis Active (1 EU/t)", x + 38, y + 68, 0xFF67E8F9, false);
            } else {
                context.drawText(this.textRenderer, "⚙️ Steel Ingot: " + pct + "% (" + timeStr + ")", x + 38, y + 58, 0xFFFFD700, false);
                context.drawText(this.textRenderer, "Metallurgical Fusion (1 EU/t)", x + 38, y + 68, 0xFFCBD5E1, false);
            }
        } else {
            ItemStack s0 = this.handler.getStackInSlot(0);
            ItemStack s1 = this.handler.getStackInSlot(1);

            if (recipeType != MaterializerBlockEntity.RECIPE_NONE && energy < 1) {
                context.drawText(this.textRenderer, "⚠️ Insufficient Power (1 EU/t)", x + 38, y + 58, 0xFFEF4444, false);
                context.drawText(this.textRenderer, "Connect to Battery / Cable / Solar", x + 38, y + 68, 0xFF94A3B8, false);
            } else if (!s0.isEmpty() || !s1.isEmpty()) {
                context.drawText(this.textRenderer, "⚡ 2 Iron + 1 Diamond -> Elactorite", x + 36, y + 58, 0xFF38BDF8, false);
                context.drawText(this.textRenderer, "⚡ 2 Iron + 1 Coal -> Steel Ingot", x + 36, y + 68, 0xFF94A3B8, false);
            } else {
                context.drawText(this.textRenderer, "⚡ Elactorite: 2 Iron + 1 Diamond", x + 36, y + 58, 0xFFA855F7, false);
                context.drawText(this.textRenderer, "⚡ Steel: 2 Iron + 1 Coal", x + 36, y + 68, 0xFF94A3B8, false);
            }
        }

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 83 + i * 18, x + 23 + j * 18, y + 99 + i * 18, 0xFF0F172A);
                context.drawBorder(x + 7 + j * 18, y + 83 + i * 18, 16, 16, 0xFF1E293B);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 141, x + 23 + i * 18, y + 157, 0xFF0F172A);
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
            context.drawTooltip(this.textRenderer,
                    Text.literal("⚡ Stored Energy: " + this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " EU\nRate: 1 EU/t when materializing"),
                    mouseX, mouseY);
        }
    }
}
