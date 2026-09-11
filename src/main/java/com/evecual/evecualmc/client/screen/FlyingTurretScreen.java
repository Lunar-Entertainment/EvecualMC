package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.FlyingTurretScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class FlyingTurretScreen extends HandledScreen<FlyingTurretScreenHandler> {

    public FlyingTurretScreen(FlyingTurretScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 186;
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
        // Cyan Glowing Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF38BDF8);
        // Header
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 15, 0xFF142238);
        context.drawText(this.textRenderer, "🚁 FLYING DEFENSE DRONE", x + 8, y + 4, 0xFF38BDF8, false);

        // 1. Area Coverage & Altitude Buttons (Row 1)
        int r = this.handler.getRadius();
        int areaSize = r * 2;
        drawModernButton(context, x + 8, y + 18, 104, 14, "🌐 " + areaSize + "x" + areaSize + " (" + r + "m)", 0xFF38BDF8, 0xFF0F172A, mouseX, mouseY);

        int alt = this.handler.getPatrolAltitude();
        drawModernButton(context, x + 116, y + 18, 52, 14, "Alt: " + alt + "m", 0xFFFCD34D, 0xFF0F172A, mouseX, mouseY);

        // 2. Targeting Filter Toggles (2 rows x 2 columns)
        // Row 1: Players & Monsters
        boolean targetPlayers = this.handler.isTargetPlayers();
        String pText = targetPlayers ? "👤 Players: TARGET" : "👤 Players: ALLOWED";
        int pColor = targetPlayers ? 0xFFEF4444 : 0xFF22C55E;
        drawModernButton(context, x + 8, y + 35, 78, 14, pText, pColor, 0xFF0F172A, mouseX, mouseY);

        boolean targetMonsters = this.handler.isTargetMonsters();
        String mText = targetMonsters ? "🧟 Mobs: TARGET" : "🧟 Mobs: IGNORE";
        int mColor = targetMonsters ? 0xFFEF4444 : 0xFF94A3B8;
        drawModernButton(context, x + 90, y + 35, 78, 14, mText, mColor, 0xFF0F172A, mouseX, mouseY);

        // Row 2: Animals & Bosses
        boolean targetAnimals = this.handler.isTargetAnimals();
        String aText = targetAnimals ? "🐷 Animals: TARGET" : "🐷 Animals: IGNORE";
        int aColor = targetAnimals ? 0xFFEF4444 : 0xFF94A3B8;
        drawModernButton(context, x + 8, y + 52, 78, 14, aText, aColor, 0xFF0F172A, mouseX, mouseY);

        boolean targetBosses = this.handler.isTargetBosses();
        String bText = targetBosses ? "💀 Bosses: TARGET" : "💀 Bosses: IGNORE";
        int bColor = targetBosses ? 0xFFEF4444 : 0xFF94A3B8;
        drawModernButton(context, x + 90, y + 52, 78, 14, bText, bColor, 0xFF0F172A, mouseX, mouseY);

        // 3. Ammo Container Status Box
        boolean linked = this.handler.hasLinkedContainer();
        context.fill(x + 8, y + 69, x + 168, y + 95, 0xFF080D1A);
        context.drawBorder(x + 8, y + 69, 160, 26, linked ? 0xFF10B981 : 0xFFF59E0B);

        if (linked) {
            BlockPos cPos = this.handler.getLinkedContainerPos();
            context.drawText(this.textRenderer, "📦 Ammo: LINKED [" + cPos.getX() + ", " + cPos.getY() + ", " + cPos.getZ() + "]", x + 12, y + 73, 0xFF86EFAC, false);
            context.drawText(this.textRenderer, "Wireless Ammo Draw (Copper, Iron, Diamond)", x + 12, y + 83, 0xFF94A3B8, false);
        } else {
            context.drawText(this.textRenderer, "⚠️ No Ammo Container Linked!", x + 12, y + 73, 0xFFFBBF24, false);
            context.drawText(this.textRenderer, "Right-click container with Linker Tool", x + 12, y + 83, 0xFF94A3B8, false);
        }

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 101 + i * 18, x + 23 + j * 18, y + 117 + i * 18, 0xFF0F1A2E);
                context.drawBorder(x + 7 + j * 18, y + 101 + i * 18, 16, 16, 0xFF1C3150);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 159, x + 23 + i * 18, y + 175, 0xFF0F1A2E);
            context.drawBorder(x + 7 + i * 18, y + 159, 16, 16, 0xFF00E5FF);
        }
    }

    private void drawModernButton(DrawContext context, int x, int y, int w, int h, String text, int textColor, int bgColor, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? 0xFF1E293B : bgColor);
        context.drawBorder(x, y, w, h, hovered ? 0xFF38BDF8 : 0xFF334155);
        int tw = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, x + (w - tw) / 2, y + (h - 8) / 2, textColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Button 4: Cycle Area Radius (x: 8, y: 18, w: 104, h: 14)
        if (mouseX >= x + 8 && mouseX <= x + 112 && mouseY >= y + 18 && mouseY <= y + 32) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 4);
                return true;
            }
        }

        // Button 5: Cycle Patrol Altitude (x: 116, y: 18, w: 52, h: 14)
        if (mouseX >= x + 116 && mouseX <= x + 168 && mouseY >= y + 18 && mouseY <= y + 32) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 5);
                return true;
            }
        }

        // Button 0: Toggle Target Players (x: 8, y: 35, w: 78, h: 14)
        if (mouseX >= x + 8 && mouseX <= x + 86 && mouseY >= y + 35 && mouseY <= y + 49) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 0);
                return true;
            }
        }

        // Button 1: Toggle Target Monsters (x: 90, y: 35, w: 78, h: 14)
        if (mouseX >= x + 90 && mouseX <= x + 168 && mouseY >= y + 35 && mouseY <= y + 49) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 1);
                return true;
            }
        }

        // Button 2: Toggle Target Animals (x: 8, y: 52, w: 78, h: 14)
        if (mouseX >= x + 8 && mouseX <= x + 86 && mouseY >= y + 52 && mouseY <= y + 66) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 2);
                return true;
            }
        }

        // Button 3: Toggle Target Bosses (x: 90, y: 52, w: 78, h: 14)
        if (mouseX >= x + 90 && mouseX <= x + 168 && mouseY >= y + 52 && mouseY <= y + 66) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 3);
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
    }
}
