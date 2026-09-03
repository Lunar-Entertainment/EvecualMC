package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ElectronicCombinerScreen extends HandledScreen<ElectronicCombinerScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/gui/container/electronic_combiner.png");

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

        // Render Energy Bar (10, 18 to 18, 62)
        int energy = handler.getEnergy();
        int maxEnergy = handler.getMaxEnergy();
        if (maxEnergy > 0) {
            int energyHeight = (int) (42.0 * ((double) energy / maxEnergy));
            if (energyHeight > 0) {
                context.fill(x + 11, y + 19 + (42 - energyHeight), x + 18, y + 61, 0xFFF59E0B);
            }
        }

        // Render Progress Arrow (78, 35 to 102, 45)
        int progress = handler.getProgress();
        int maxProgress = handler.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) (24.0 * ((double) progress / maxProgress));
            context.fill(x + 79, y + 36, x + 79 + progressWidth, y + 44, 0xFF38BDF8);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Tooltip for energy bar
        if (mouseX >= x + 10 && mouseX <= x + 18 && mouseY >= y + 18 && mouseY <= y + 62) {
            context.drawTooltip(textRenderer, Text.literal("⚡ Energy: " + handler.getEnergy() + " / " + handler.getMaxEnergy() + " E"), mouseX, mouseY);
        }
    }
}
