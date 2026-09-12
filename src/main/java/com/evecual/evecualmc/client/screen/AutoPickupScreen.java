package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.AutoPickupScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AutoPickupScreen extends HandledScreen<AutoPickupScreenHandler> {

    public AutoPickupScreen(AutoPickupScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = 72;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Dark Glassmorphic Backdrop
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF00A0F1D);
        // Outer Glowing Cyan/Neon Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF00E5FF);
        // Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 15, 0xFF142238);
        context.drawText(this.textRenderer, "📡 AUTO PICKUP FILTER", x + 8, y + 4, 0xFF00E5FF, false);

        // Subheader / Configuration guide
        context.drawText(this.textRenderer, "§c⛔ Ignored / Blacklisted Items:", x + 8, y + 22, 0xFFF87171, false);

        // Filter Slots (1 row x 9 columns)
        for (int i = 0; i < 9; ++i) {
            int sx = x + 7 + i * 18;
            int sy = y + 35;
            context.fill(sx, sy, sx + 18, sy + 18, 0xFF1E1118);
            context.drawBorder(sx, sy, 18, 18, 0xFFEF4444);
        }

        // Separator Line
        context.fill(x + 8, y + 62, x + this.backgroundWidth - 8, y + 63, 0xFF334155);

        // Player Inventory Background Slots (3x9)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int px = x + 7 + j * 18;
                int py = y + 83 + i * 18;
                context.fill(px, py, px + 18, py + 18, 0xFF0F1A2E);
                context.drawBorder(px, py, 18, 18, 0xFF1C3150);
            }
        }

        // Player Hotbar Background Slots (1x9)
        for (int i = 0; i < 9; ++i) {
            int hx = x + 7 + i * 18;
            int hy = y + 141;
            context.fill(hx, hy, hx + 18, hy + 18, 0xFF0F1A2E);
            context.drawBorder(hx, hy, 18, 18, 0xFF00E5FF);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Hover tooltip on empty filter slots
        if (mouseY >= y + 35 && mouseY < y + 53) {
            for (int i = 0; i < 9; i++) {
                int sx = x + 7 + i * 18;
                if (mouseX >= sx && mouseX < sx + 18) {
                    if (!this.handler.getSlot(i).hasStack()) {
                        context.drawTooltip(this.textRenderer,
                                Text.literal("§7Place an item here to §cblacklist§7 it.\n§8Drone will ignore this item."),
                                mouseX, mouseY);
                    }
                    break;
                }
            }
        }
    }
}
