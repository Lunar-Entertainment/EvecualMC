package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ElectronicCombinerScreen extends HandledScreen<ElectronicCombinerScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/gui/container/electronic_combiner.png");
    private static final Identifier ICON_ENGINE = new Identifier("evecualmc", "textures/gui/icon_engine.png");
    private static final Identifier ICON_HULL = new Identifier("evecualmc", "textures/gui/icon_hull.png");
    private static final Identifier ICON_GLASS = new Identifier("evecualmc", "textures/gui/icon_glass.png");
    private static final Identifier ICON_LEATHER = new Identifier("evecualmc", "textures/gui/icon_leather.png");
    private static final Identifier ICON_COLOR = new Identifier("evecualmc", "textures/gui/icon_color.png");
    private static final Identifier ICON_TRUNK = new Identifier("evecualmc", "textures/gui/icon_trunk.png");

    public ElectronicCombinerScreen(ElectronicCombinerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        // Render Energy Bar (10, 19 to 18, 61)
        int energy = handler.getEnergy();
        int maxEnergy = handler.getMaxEnergy();
        if (maxEnergy > 0) {
            int energyHeight = (int) (42.0 * ((double) energy / maxEnergy));
            if (energyHeight > 0) {
                context.fill(x + 10, y + 19 + (42 - energyHeight), x + 19, y + 61, 0xFFF59E0B);
            }
        }

        // Render Progress Arrow (88, 35 to 114, 41)
        int progress = handler.getProgress();
        int maxProgress = handler.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) (26.0 * ((double) progress / maxProgress));
            context.fill(x + 88, y + 35, x + 88 + progressWidth, y + 41, 0xFF38BDF8);
            if (progressWidth >= 26) {
                // Tip arrow
                context.fill(x + 114, y + 33, x + 120, y + 43, 0xFF38BDF8);
            }
        }

        // Draw Ghost Icons inside empty slots
        drawGhostIcon(context, x, y, 0, ICON_ENGINE);
        drawGhostIcon(context, x, y, 1, ICON_HULL);
        drawGhostIcon(context, x, y, 2, ICON_GLASS);
        drawGhostIcon(context, x, y, 3, ICON_LEATHER);
        drawGhostIcon(context, x, y, 4, ICON_COLOR);
        drawGhostIcon(context, x, y, 5, ICON_TRUNK);
    }

    private void drawGhostIcon(DrawContext context, int x, int y, int slotIndex, Identifier icon) {
        if (slotIndex < handler.slots.size()) {
            Slot slot = handler.slots.get(slotIndex);
            if (!slot.hasStack()) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.65F);
                context.drawTexture(icon, x + slot.x, y + slot.y, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Energy Tooltip
        if (mouseX >= x + 9 && mouseX <= x + 20 && mouseY >= y + 18 && mouseY <= y + 62) {
            context.drawTooltip(textRenderer, Text.literal("⚡ Energy: " + handler.getEnergy() + " / " + handler.getMaxEnergy() + " E"), mouseX, mouseY);
        }

        // Slot hover tips when empty
        if (mouseX >= x + 44 && mouseX <= x + 60 && mouseY >= y + 43 && mouseY <= y + 59 && !handler.slots.get(4).hasStack()) {
            context.drawTooltip(textRenderer, Text.literal("§eColor Slot: §7Insert Dye (Default: Red)"), mouseX, mouseY);
        } else if (mouseX >= x + 64 && mouseX <= x + 80 && mouseY >= y + 43 && mouseY <= y + 59 && !handler.slots.get(5).hasStack()) {
            context.drawTooltip(textRenderer, Text.literal("§eTrunk Slot: §7Insert Trunk Upgrade (Default: Standard)"), mouseX, mouseY);
        } else if (mouseX >= x + 64 && mouseX <= x + 80 && mouseY >= y + 21 && mouseY <= y + 37 && !handler.slots.get(2).hasStack()) {
            context.drawTooltip(textRenderer, Text.literal("§bGlass Slot: §7Any Stained or Normal Glass"), mouseX, mouseY);
        }
    }
}
