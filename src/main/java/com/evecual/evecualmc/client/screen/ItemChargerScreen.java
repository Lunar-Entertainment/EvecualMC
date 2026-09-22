package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.energy.ItemEnergyHelper;
import com.evecual.evecualmc.screen.ItemChargerScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemChargerScreen extends HandledScreen<ItemChargerScreenHandler> {

    public ItemChargerScreen(ItemChargerScreenHandler handler, PlayerInventory inventory, Text title) {
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

        // Dark Cyan-Slate Cybernetic Backdrop
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF2090D16);
        // Outer Cyan Neon Border
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF0284C7);
        // Inner Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 16, 0xFF0F172A);

        // Header Title
        context.drawText(this.textRenderer, "⚡ ITEM CHARGING POD", x + 8, y + 4, 0xFF38BDF8, false);

        // Center Charging Cradle Slot Frame (Slot 0, x: 79, y: 34)
        boolean isActive = this.handler.isActive();
        int cradleBorderColor = isActive ? 0xFF38BDF8 : 0xFF0284C7;
        context.fill(x + 79, y + 34, x + 97, y + 52, 0xFF0F172A);
        context.drawBorder(x + 79, y + 34, 18, 18, cradleBorderColor);

        // Holographic corner bracket highlights around charging cradle
        context.fill(x + 76, y + 31, x + 78, y + 36, 0xFF00E5FF);
        context.fill(x + 76, y + 31, x + 81, y + 33, 0xFF00E5FF);

        context.fill(x + 98, y + 31, x + 100, y + 36, 0xFF00E5FF);
        context.fill(x + 95, y + 31, x + 100, y + 33, 0xFF00E5FF);

        context.fill(x + 76, y + 50, x + 78, y + 55, 0xFF00E5FF);
        context.fill(x + 76, y + 53, x + 81, y + 55, 0xFF00E5FF);

        context.fill(x + 98, y + 50, x + 100, y + 55, 0xFF00E5FF);
        context.fill(x + 95, y + 53, x + 100, y + 55, 0xFF00E5FF);

        // 1. Station Energy Storage Gauge Bar (x: 16, y: 22, w: 14, h: 50)
        int stationE = this.handler.getEnergy();
        int maxStationE = this.handler.getMaxEnergy();
        float stationRatio = Math.min(1.0F, (float) stationE / (float) Math.max(1, maxStationE));
        int stationFillHeight = (int) (48 * stationRatio);

        context.fill(x + 16, y + 22, x + 30, y + 72, 0xFF020617);
        context.drawBorder(x + 16, y + 22, 14, 50, 0xFF0284C7);

        if (stationFillHeight > 0) {
            context.fill(x + 17, y + 71 - stationFillHeight, x + 29, y + 71, 0xFF00E5FF);
        }

        // 2. Horizontal Item Battery Gauge Bar (x: 48, y: 56, w: 80, h: 8)
        ItemStack chargingStack = this.handler.getChargingItem();
        boolean hasItem = !chargingStack.isEmpty() && ItemEnergyHelper.isChargeable(chargingStack);
        long itemE = hasItem ? ItemEnergyHelper.getEnergy(chargingStack) : 0;
        long itemMaxE = hasItem ? ItemEnergyHelper.getMaxEnergy(chargingStack) : 1;
        float itemRatio = hasItem ? Math.min(1.0F, (float) itemE / (float) Math.max(1, itemMaxE)) : 0.0F;
        int itemFillWidth = (int) (78 * itemRatio);

        context.fill(x + 48, y + 56, x + 128, y + 64, 0xFF020617);
        context.drawBorder(x + 48, y + 56, 80, 8, 0xFF334155);

        if (hasItem && itemFillWidth > 0) {
            int barColor = (itemE >= itemMaxE) ? 0xFF4ADE80 : 0xFF00E5FF;
            context.fill(x + 49, y + 57, x + 49 + itemFillWidth, y + 63, barColor);
        }

        // Status Readout
        if (hasItem) {
            int pct = (int) (itemRatio * 100);
            if (isActive) {
                context.drawText(this.textRenderer, "⚡ Slow Charging: " + pct + "% (1 EU/t)", x + 38, y + 68, 0xFF38BDF8, false);
            } else if (itemE >= itemMaxE) {
                context.drawText(this.textRenderer, "⚡ Battery Full (" + itemE + "/" + itemMaxE + " EU)", x + 38, y + 68, 0xFF4ADE80, false);
            } else if (stationE < 1) {
                context.drawText(this.textRenderer, "⚠️ Station Depleted (Needs EU)", x + 38, y + 68, 0xFFF87171, false);
            } else {
                context.drawText(this.textRenderer, "⚡ Charge: " + itemE + " / " + itemMaxE + " EU (" + pct + "%)", x + 38, y + 68, 0xFFE2E8F0, false);
            }
        } else {
            context.drawText(this.textRenderer, "⚡ Insert Tool / Drone / Battery", x + 36, y + 68, 0xFF64748B, false);
        }

        // Player Inventory Background Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 83 + i * 18, x + 23 + j * 18, y + 99 + i * 18, 0xFF0F172A);
                context.drawBorder(x + 7 + j * 18, y + 83 + i * 18, 16, 16, 0xFF1E293B);
            }
        }

        // Player Hotbar Background Slots
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 141, x + 23 + i * 18, y + 157, 0xFF0F172A);
            context.drawBorder(x + 7 + i * 18, y + 141, 16, 16, 0xFF0284C7);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x94A3B8, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Hover Tooltip for Station Energy Gauge (x: 16 to 30, y: 22 to 72)
        if (mouseX >= x + 16 && mouseX <= x + 30 && mouseY >= y + 22 && mouseY <= y + 72) {
            int energy = this.handler.getEnergy();
            int maxEnergy = this.handler.getMaxEnergy();
            context.drawTooltip(this.textRenderer, Text.literal("§b⚡ Station Energy: §f" + energy + " / " + maxEnergy + " EU"), mouseX, mouseY);
        }

        // Hover Tooltip for Item Battery Gauge (x: 48 to 128, y: 56 to 64)
        if (mouseX >= x + 48 && mouseX <= x + 128 && mouseY >= y + 56 && mouseY <= y + 64) {
            ItemStack stack = this.handler.getChargingItem();
            if (!stack.isEmpty() && ItemEnergyHelper.isChargeable(stack)) {
                long itemE = ItemEnergyHelper.getEnergy(stack);
                long itemMaxE = ItemEnergyHelper.getMaxEnergy(stack);
                int pct = (int) (itemE * 100 / Math.max(1, itemMaxE));
                context.drawTooltip(this.textRenderer, Text.literal("§e🔋 Item Battery: §f" + itemE + " / " + itemMaxE + " EU (" + pct + "%)"), mouseX, mouseY);
            }
        }
    }
}
