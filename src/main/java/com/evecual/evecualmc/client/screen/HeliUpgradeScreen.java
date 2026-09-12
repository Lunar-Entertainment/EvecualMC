package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.HeliUpgradeScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class HeliUpgradeScreen extends HandledScreen<HeliUpgradeScreenHandler> {
    public HeliUpgradeScreen(HeliUpgradeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 190;
        this.playerInventoryTitleY = 96;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("📖 Guide"),
                btn -> {
                    if (this.client != null) {
                        this.client.setScreen(new ModTipScreen(TipTopic.EV_HELI, 0, 3000, "🚁 EV Heli Avionics & Modular Hardpoint Upgrades"));
                    }
                }
        ).dimensions(x + this.backgroundWidth - 55, y + 5, 48, 14).build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Dark sci-fi aerospace terminal background
        context.fillGradient(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFA0F172A, 0xFA020617);
        // Cyan cyan/sky high-tech border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF0284C7);
        context.drawBorder(x + 1, y + 1, this.backgroundWidth - 2, this.backgroundHeight - 2, 0xFF0369A1);

        // Header Divider
        context.fill(x + 5, y + 21, x + this.backgroundWidth - 5, y + 22, 0xFF0EA5E9);

        // Section Panels for Upgrades
        // Left Mod Section: Speed, Cargo, Battery
        context.fill(x + 6, y + 24, x + 86, y + 94, 0x660284C7);
        context.drawBorder(x + 6, y + 24, 80, 70, 0xFF0369A1);

        // Right Mod Section: Left Arm & Right Arm Hardpoints
        context.fill(x + 90, y + 24, x + 170, y + 94, 0x660284C7);
        context.drawBorder(x + 90, y + 24, 80, 70, 0xFF0369A1);

        // Draw Slot Boxes
        drawSlotBox(context, x + 29, y + 25);
        drawSlotBox(context, x + 29, y + 49);
        drawSlotBox(context, x + 29, y + 73);

        drawSlotBox(context, x + 129, y + 35);
        drawSlotBox(context, x + 129, y + 63);

        // Slot Labels
        context.drawText(this.textRenderer, "Speed", x + 50, y + 30, 0xFF38BDF8, false);
        context.drawText(this.textRenderer, "Cargo", x + 50, y + 54, 0xFF38BDF8, false);
        context.drawText(this.textRenderer, "Battery", x + 50, y + 78, 0xFF38BDF8, false);

        context.drawText(this.textRenderer, "Left Arm", x + 94, y + 26, 0xFFF59E0B, false);
        context.drawText(this.textRenderer, "Right Arm", x + 94, y + 54, 0xFFF59E0B, false);

        // Player Inventory Area Box
        context.fill(x + 6, y + 106, x + 170, y + 184, 0x440284C7);
        context.drawBorder(x + 6, y + 106, 164, 78, 0xFF0369A1);

        // Draw Player Inventory Slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBox(context, x + 7 + col * 18, y + 107 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBox(context, x + 7 + col * 18, y + 165);
        }
    }

    private void drawSlotBox(DrawContext context, int x, int y) {
        context.fill(x, y, x + 18, y + 18, 0xAA020617);
        context.drawBorder(x, y, 18, 18, 0xFF0284C7);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Terminal Title
        context.drawText(this.textRenderer, "🚁 EV Heli Hardpoints", 8, 7, 0xFFE0F2FE, false);
        // Player Inventory Title
        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.playerInventoryTitleY, 0xFF94A3B8, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
