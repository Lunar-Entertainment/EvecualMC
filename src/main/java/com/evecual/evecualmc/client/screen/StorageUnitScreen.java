package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.screen.StorageUnitScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StorageUnitScreen extends HandledScreen<StorageUnitScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    public StorageUnitScreen(StorageUnitScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Previous Page Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("◀"), button -> {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 0);
            }
        }).dimensions(x + 120, y + 4, 18, 12).build());

        // Next Page Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("▶"), button -> {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 1);
            }
        }).dimensions(x + 142, y + 4, 18, 12).build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Cybernetic Energy Bar Header
        int energy = this.handler.getEnergy();
        int units = this.handler.getConnectedUnits();
        int page = this.handler.getCurrentPage() + 1;
        int totalPages = this.handler.getTotalPages();

        String pageInfo = "P." + page + "/" + totalPages;
        context.drawText(this.textRenderer, Text.literal("§6" + pageInfo), x + 80, y + 6, 0xFFFFFF, false);

        String energyInfo = "§e⚡ " + energy + " EU §8| §b" + units + (units == 1 ? " Unit" : " Units");
        context.drawText(this.textRenderer, Text.literal(energyInfo), x + 8, y + this.backgroundHeight - 105, 0xFFFFFF, false);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, Text.literal("Storage"), 8, 6, 4210752, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
