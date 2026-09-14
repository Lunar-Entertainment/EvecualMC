package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.block.entity.ItemFabricatorBlockEntity;
import com.evecual.evecualmc.screen.ItemFabricatorScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemFabricatorScreen extends HandledScreen<ItemFabricatorScreenHandler> {

    public ItemFabricatorScreen(ItemFabricatorScreenHandler handler, PlayerInventory inventory, Text title) {
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
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF0070B14);
        // Outer Glowing Cyan Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF00E5FF);
        // Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 14, 0xFF101B2E);

        // Header Title
        context.drawText(this.textRenderer, "⚡ ITEM FABRICATOR", x + 8, y + 3, 0xFF00E5FF, false);

        int recipe = this.handler.getSelectedRecipe();

        // Blueprint Toggle Buttons
        // Button 1: ZAPPER (x: 44, y: 14, w: 50, h: 10)
        boolean zapperSelected = (recipe == ItemFabricatorBlockEntity.RECIPE_ZAPPER);
        int zapperBg = zapperSelected ? 0xFF0284C7 : 0xFF0F172A;
        int zapperBorder = zapperSelected ? 0xFF38BDF8 : 0xFF334155;
        context.fill(x + 44, y + 14, x + 94, y + 24, zapperBg);
        context.drawBorder(x + 44, y + 14, 50, 10, zapperBorder);
        context.drawText(this.textRenderer, "⚡ ZAPPER", x + 48, y + 15, zapperSelected ? 0xFFFFFFFF : 0xFF94A3B8, false);

        // Button 2: RAILGUN (x: 98, y: 14, w: 54, h: 10)
        boolean railgunSelected = (recipe == ItemFabricatorBlockEntity.RECIPE_RAILGUN);
        int railgunBg = railgunSelected ? 0xFF9333EA : 0xFF0F172A;
        int railgunBorder = railgunSelected ? 0xFFC084FC : 0xFF334155;
        context.fill(x + 98, y + 14, x + 152, y + 24, railgunBg);
        context.drawBorder(x + 98, y + 14, 54, 10, railgunBorder);
        context.drawText(this.textRenderer, "💥 RAILGUN", x + 101, y + 15, railgunSelected ? 0xFFFFFFFF : 0xFF94A3B8, false);

        // Input Slot Frames (3 columns x 2 rows)
        int[] slotXs = new int[]{44, 62, 80, 44, 62, 80};
        int[] slotYs = new int[]{26, 26, 26, 44, 44, 44};
        for (int i = 0; i < 6; i++) {
            int sx = x + slotXs[i] - 1;
            int sy = y + slotYs[i] - 1;
            context.fill(sx, sy, sx + 18, sy + 18, 0xFF0E1726);
            context.drawBorder(sx, sy, 18, 18, zapperSelected ? 0xFF00E5FF : 0xFFA855F7);
        }

        // Output Slot Frame (Slot 6, x: 134, y: 35)
        context.fill(x + 133, y + 34, x + 151, y + 52, 0xFF1C142E);
        context.drawBorder(x + 133, y + 34, 18, 18, 0xFFFFD700);

        // 1. Energy Storage Gauge Bar (x: 14, y: 22, w: 14, h: 48)
        int energy = this.handler.getEnergy();
        int maxEnergy = this.handler.getMaxEnergy();
        float energyRatio = Math.min(1.0F, (float) energy / (float) Math.max(1, maxEnergy));
        int energyFillHeight = (int) (46 * energyRatio);

        context.fill(x + 14, y + 22, x + 28, y + 70, 0xFF060912);
        context.drawBorder(x + 14, y + 22, 14, 48, 0xFF00E5FF);

        if (energyFillHeight > 0) {
            context.fill(x + 15, y + 69 - energyFillHeight, x + 27, y + 69, 0xFF00E5FF);
        }

        // 2. Progress Arrow Bar (x: 100, y: 39, w: 30, h: 10)
        int progress = this.handler.getProgressTicks();
        int maxProgress = this.handler.getTotalTicks();
        boolean isActive = this.handler.isActive();
        float progressRatio = (maxProgress > 0) ? Math.min(1.0F, (float) progress / (float) maxProgress) : 0.0F;
        int progressFillWidth = (int) (28 * progressRatio);

        context.fill(x + 100, y + 39, x + 130, y + 49, 0xFF080D1A);
        context.drawBorder(x + 100, y + 39, 30, 10, zapperSelected ? 0xFF00E5FF : 0xFFA855F7);

        if (progressFillWidth > 0) {
            context.fill(x + 101, y + 40, x + 101 + progressFillWidth, y + 48, zapperSelected ? 0xFF38BDF8 : 0xFFC084FC);
        }

        // Status description & blueprint components guidance below grid
        if (isActive) {
            int pct = (int) (progressRatio * 100);
            int remTicks = Math.max(0, maxProgress - progress);
            int remSec = remTicks / 20;
            String itemName = zapperSelected ? "Electronic Zapper" : "Hypervelocity Railgun";
            context.drawText(this.textRenderer, "🌀 Fabricating: " + pct + "% (" + remSec + "s)", x + 38, y + 64, 0xFFFFD700, false);
            context.drawText(this.textRenderer, itemName + " assembly active", x + 38, y + 73, 0xFF67E8F9, false);
        } else {
            if (zapperSelected) {
                context.drawText(this.textRenderer, "Req: 1 Elactorite, 2 Rods, 1 Battery, 2 Wires", x + 30, y + 64, 0xFF38BDF8, false);
                context.drawText(this.textRenderer, "Energy: 500 EU (5 EU/t) | High-Voltage Tool", x + 30, y + 73, 0xFF94A3B8, false);
            } else {
                context.drawText(this.textRenderer, "Req: 2 Netherite, 4 Elactorite, 4 Rods,", x + 30, y + 64, 0xFFA855F7, false);
                context.drawText(this.textRenderer, "1 Engine+, 4 Cu-Plates, 4 Wires (2500 EU)", x + 30, y + 73, 0xFFE2E8F0, false);
            }
        }

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 83 + i * 18, x + 23 + j * 18, y + 99 + i * 18, 0xFF0E1626);
                context.drawBorder(x + 7 + j * 18, y + 83 + i * 18, 16, 16, 0xFF1C2C45);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 141, x + 23 + i * 18, y + 157, 0xFF0E1626);
            context.drawBorder(x + 7 + i * 18, y + 141, 16, 16, 0xFF00E5FF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Check Button 1: ZAPPER
        if (mouseX >= x + 44 && mouseX <= x + 94 && mouseY >= y + 14 && mouseY <= y + 24) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, ItemFabricatorBlockEntity.RECIPE_ZAPPER);
                return true;
            }
        }

        // Check Button 2: RAILGUN
        if (mouseX >= x + 98 && mouseX <= x + 152 && mouseY >= y + 14 && mouseY <= y + 24) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, ItemFabricatorBlockEntity.RECIPE_RAILGUN);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Energy Gauge Tooltip
        if (mouseX >= x + 14 && mouseX <= x + 28 && mouseY >= y + 22 && mouseY <= y + 70) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("⚡ Stored Energy: " + this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " EU\nActive Rate: 5-10 EU/t"),
                    mouseX, mouseY);
        }
    }
}
