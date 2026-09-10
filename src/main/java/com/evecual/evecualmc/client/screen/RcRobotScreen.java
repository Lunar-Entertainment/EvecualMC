package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.entity.RcRobotEntity;
import com.evecual.evecualmc.screen.RcRobotScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class RcRobotScreen extends HandledScreen<RcRobotScreenHandler> {

    public RcRobotScreen(RcRobotScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 224;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = 129;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💡 Tips"),
                btn -> {
                    if (this.client != null) {
                        RcRobotEntity robot = this.handler.getRobot();
                        int energy = robot != null ? robot.getEnergy() : 0;
                        this.client.setScreen(new ModTipScreen(
                                TipTopic.RC_ROBOT,
                                energy,
                                RcRobotEntity.MAX_ENERGY,
                                "🤖 RC Robot Arm Logistics & Cargo Diagnostics"
                        ));
                    }
                }
        ).dimensions(x + 124, y + 4, 46, 12).build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Dark sci-fi metallic gradient background
        context.fillGradient(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFA0F172A, 0xFA020617);
        // Tech Cyan Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF0284C7);
        context.drawBorder(x + 1, y + 1, this.backgroundWidth - 2, this.backgroundHeight - 2, 0xFF0369A1);

        // Header Divider
        context.fill(x + 5, y + 16, x + this.backgroundWidth - 5, y + 17, 0xFF0EA5E9);

        // --- Cargo Slots (6 rows of 9) ---
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBox(context, x + 7 + col * 18, y + 17 + row * 18);
            }
        }

        // --- Player Inventory Slots (3 rows of 9) ---
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBox(context, x + 7 + col * 18, y + 139 + row * 18);
            }
        }

        // --- Player Hotbar (1 row of 9) ---
        for (int col = 0; col < 9; col++) {
            drawSlotBox(context, x + 7 + col * 18, y + 197);
        }

        // --- Right Sidebar: DUAL-ARM STATION ---
        int sideX = x + 174;
        int sideW = 46;
        context.fill(sideX, y + 19, sideX + sideW, y + this.backgroundHeight - 5, 0x660284C7);
        context.drawBorder(sideX, y + 19, sideW, this.backgroundHeight - 24, 0xFF0369A1);

        // Arm Header
        context.drawCenteredTextWithShadow(this.textRenderer, "ARMS", sideX + sideW / 2, y + 22, 0xFFF59E0B);

        // Slot 0 Box: Left Arm (Blocks)
        drawArmSlotBox(context, x + 189, y + 35, 0xFF38BDF8);
        context.drawCenteredTextWithShadow(this.textRenderer, "LEFT", sideX + sideW / 2, y + 54, 0xFF38BDF8);
        context.drawCenteredTextWithShadow(this.textRenderer, "[Blocks]", sideX + sideW / 2, y + 63, 0xFF94A3B8);

        // Slot 1 Box: Right Arm (Tools/Items)
        drawArmSlotBox(context, x + 189, y + 91, 0xFF4ADE80);
        context.drawCenteredTextWithShadow(this.textRenderer, "RIGHT", sideX + sideW / 2, y + 110, 0xFF4ADE80);
        context.drawCenteredTextWithShadow(this.textRenderer, "[Tools]", sideX + sideW / 2, y + 119, 0xFF94A3B8);

        // Mini control guide in sidebar
        context.fill(sideX + 3, y + 134, sideX + sideW - 3, y + 186, 0x88020617);
        context.drawBorder(sideX + 3, y + 134, sideW - 6, 52, 0xFF0369A1);

        context.drawCenteredTextWithShadow(this.textRenderer, "LMB", sideX + sideW / 2, y + 138, 0xFFFBBF24);
        context.drawCenteredTextWithShadow(this.textRenderer, "Tool", sideX + sideW / 2, y + 147, 0xFFE2E8F0);

        context.drawCenteredTextWithShadow(this.textRenderer, "RMB", sideX + sideW / 2, y + 160, 0xFF38BDF8);
        context.drawCenteredTextWithShadow(this.textRenderer, "Place", sideX + sideW / 2, y + 169, 0xFFE2E8F0);

        // Energy Indicator
        RcRobotEntity robot = this.handler.getRobot();
        int energy = robot != null ? robot.getEnergy() : 1000;
        float energyRatio = (float) energy / (float) RcRobotEntity.MAX_ENERGY;
        int barW = sideW - 8;
        int fillW = (int) (barW * energyRatio);

        context.fill(sideX + 4, y + 195, sideX + 4 + barW, y + 203, 0xAA020617);
        int energyColor = energyRatio > 0.4f ? 0xFF10B981 : energyRatio > 0.15f ? 0xFFF59E0B : 0xFFEF4444;
        context.fill(sideX + 4, y + 195, sideX + 4 + fillW, y + 203, energyColor);
        context.drawBorder(sideX + 4, y + 195, barW, 8, 0xFF0284C7);
        context.drawCenteredTextWithShadow(this.textRenderer, energy + " EU", sideX + sideW / 2, y + 206, 0xFF94A3B8);
    }

    private void drawSlotBox(DrawContext context, int x, int y) {
        context.fill(x, y, x + 18, y + 18, 0x88020617);
        context.drawBorder(x, y, 18, 18, 0xFF0284C7);
    }

    private void drawArmSlotBox(DrawContext context, int x, int y, int borderColor) {
        context.fill(x, y, x + 18, y + 18, 0xCC020617);
        context.drawBorder(x, y, 18, 18, borderColor);
        context.drawBorder(x - 1, y - 1, 20, 20, 0x880369A1);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Title
        context.drawText(this.textRenderer, "🤖 RC Robot Cargo (54 Slots)", 8, 5, 0xFFE0F2FE, false);
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
