package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.TurretAmmoContainerScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class TurretAmmoContainerScreen extends HandledScreen<TurretAmmoContainerScreenHandler> {

    public TurretAmmoContainerScreen(TurretAmmoContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 168;
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
        // Outer Glowing Orange/Amber Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFFF59E0B);
        // Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 15, 0xFF142238);
        context.drawText(this.textRenderer, "📦 TURRET AMMO CONTAINER", x + 8, y + 4, 0xFFF59E0B, false);

        // Ammo Slots (3x9 grid)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int sx = x + 7 + j * 18;
                int sy = y + 17 + i * 18;
                context.fill(sx, sy, sx + 18, sy + 18, 0xFF0F1A2E);
                context.drawBorder(sx, sy, 18, 18, 0xFF38BDF8);
            }
        }

        // Separator Line
        context.fill(x + 8, y + 75, x + this.backgroundWidth - 8, y + 76, 0xFF334155);

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 84 + i * 18, x + 23 + j * 18, y + 100 + i * 18, 0xFF0F1A2E);
                context.drawBorder(x + 7 + j * 18, y + 84 + i * 18, 16, 16, 0xFF1C3150);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 142, x + 23 + i * 18, y + 158, 0xFF0F1A2E);
            context.drawBorder(x + 7 + i * 18, y + 142, 16, 16, 0xFF00E5FF);
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
    }
}
