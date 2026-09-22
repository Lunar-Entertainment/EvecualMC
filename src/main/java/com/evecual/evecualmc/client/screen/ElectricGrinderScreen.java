package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.ElectricGrinderScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ElectricGrinderScreen extends HandledScreen<ElectricGrinderScreenHandler> {

    public ElectricGrinderScreen(ElectricGrinderScreenHandler handler, PlayerInventory inventory, Text title) {
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

        // Dark Industrial Slate Backdrop
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF010141D);
        // Outer Industrial Orange Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFFEA580C);
        // Inner Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 16, 0xFF1E293B);

        // Header Title
        context.drawText(this.textRenderer, "⚙️ ELECTRIC GRINDER", x + 8, y + 4, 0xFFFDBA74, false);

        // Input Slot Frame (Slot 0, x: 54, y: 35)
        context.fill(x + 53, y + 34, x + 71, y + 52, 0xFF1C1917);
        context.drawBorder(x + 53, y + 34, 18, 18, 0xFFF59E0B);

        // Output Slot Frame (Slot 1, x: 126, y: 35)
        context.fill(x + 125, y + 34, x + 143, y + 52, 0xFF1C1917);
        context.drawBorder(x + 125, y + 34, 18, 18, 0xFFEA580C);

        // 1. Energy Storage Gauge Bar (x: 16, y: 24, w: 14, h: 48)
        int energy = this.handler.getEnergy();
        int maxEnergy = this.handler.getMaxEnergy();
        float energyRatio = Math.min(1.0F, (float) energy / (float) Math.max(1, maxEnergy));
        int energyFillHeight = (int) (46 * energyRatio);

        context.fill(x + 16, y + 24, x + 30, y + 72, 0xFF0C0A09);
        context.drawBorder(x + 16, y + 24, 14, 48, 0xFFF97316);

        if (energyFillHeight > 0) {
            context.fill(x + 17, y + 71 - energyFillHeight, x + 29, y + 71, 0xFFF59E0B);
        }

        // 2. Grinding Rotary Progress Bar (x: 77, y: 38, w: 42, h: 12)
        int progress = this.handler.getProgressTicks();
        int maxProgress = this.handler.getTotalTicks();
        boolean isActive = this.handler.isActive();

        float progressRatio = (maxProgress > 0) ? Math.min(1.0F, (float) progress / (float) maxProgress) : 0.0F;
        int progressFillWidth = (int) (40 * progressRatio);

        context.fill(x + 77, y + 38, x + 119, y + 50, 0xFF0C0A09);
        context.drawBorder(x + 77, y + 38, 42, 12, 0xFF78716C);

        if (progressFillWidth > 0) {
            context.fill(x + 78, y + 39, x + 78 + progressFillWidth, y + 49, 0xFFEA580C);
        }

        // Grinder Teeth marks inside progress track
        for (int t = 0; t < 5; t++) {
            int tx = x + 82 + t * 7;
            context.fill(tx, y + 39, tx + 1, y + 42, 0xFFE2E8F0);
            context.fill(tx + 3, y + 46, tx + 4, y + 49, 0xFFE2E8F0);
        }

        // Status text & instructions below slots
        if (isActive) {
            int pct = (int) (progressRatio * 100);
            int remTicks = Math.max(0, maxProgress - progress);
            float remSec = remTicks / 20.0F;
            String timeStr = String.format("%.1fs", remSec);

            context.drawText(this.textRenderer, "⚙️ Grinding: " + pct + "% (" + timeStr + ")", x + 38, y + 58, 0xFFF97316, false);
            context.drawText(this.textRenderer, "Rolling Copper Plates (2 EU/t)", x + 38, y + 68, 0xFFFDBA74, false);
        } else {
            ItemStack input = this.handler.getInputStack();

            if (!input.isEmpty() && energy < 2) {
                context.drawText(this.textRenderer, "⚠️ Needs Energy (2 EU/t)", x + 38, y + 58, 0xFFEF4444, false);
                context.drawText(this.textRenderer, "Connect to Battery / Wire Grid", x + 38, y + 68, 0xFF94A3B8, false);
            } else if (!input.isEmpty()) {
                context.drawText(this.textRenderer, "⚙️ Ready to Grind", x + 38, y + 58, 0xFF22C55E, false);
                context.drawText(this.textRenderer, "Processing 1 item every 5s", x + 38, y + 68, 0xFF94A3B8, false);
            } else {
                context.drawText(this.textRenderer, "⚙️ Insert Copper Ingot / Raw", x + 38, y + 58, 0xFFFDBA74, false);
                context.drawText(this.textRenderer, "Grinds into Copper Plates", x + 38, y + 68, 0xFF94A3B8, false);
            }
        }

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 83 + i * 18, x + 23 + j * 18, y + 99 + i * 18, 0xFF1C1917);
                context.drawBorder(x + 7 + j * 18, y + 83 + i * 18, 16, 16, 0xFF292524);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 141, x + 23 + i * 18, y + 157, 0xFF1C1917);
            context.drawBorder(x + 7 + i * 18, y + 141, 16, 16, 0xFFF97316);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x94A3B8, false);
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
                    Text.literal("⚡ Stored Energy: " + this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " EU\nConsumption: 2 EU/t while grinding"),
                    mouseX, mouseY);
        }
    }
}
