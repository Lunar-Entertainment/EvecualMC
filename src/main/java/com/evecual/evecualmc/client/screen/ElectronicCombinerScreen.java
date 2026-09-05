package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.recipe.CombinerRecipe;
import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ElectronicCombinerScreen extends HandledScreen<ElectronicCombinerScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("evecualmc", "textures/gui/container/electronic_combiner.png");

    private boolean showDropdown = false;
    private int lastRecipeIndex = -1;

    public ElectronicCombinerScreen(ElectronicCombinerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 240;
        this.backgroundHeight = 174;
        this.playerInventoryTitleX = 39;
        this.playerInventoryTitleY = 75;
    }

    @Override
    protected void init() {
        super.init();
        rebuildScreenWidgets();
    }

    private void rebuildScreenWidgets() {
        this.clearChildren();
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        CombinerRecipe[] allRecipes = CombinerRecipe.values();
        int currentIndex = Math.max(1, handler.getSelectedRecipeIndex());
        CombinerRecipe currentRecipe = CombinerRecipe.getRecipeByIndex(currentIndex);
        if (currentRecipe == null) {
            currentRecipe = CombinerRecipe.ELECTRIC_CAR;
            currentIndex = 1;
        }

        // 1. Previous Blueprint Button [ ◀ ]
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("◀"),
                btn -> {
                    showDropdown = false;
                    int total = allRecipes.length;
                    int prev = ((handler.getSelectedRecipeIndex() - 2 + total) % total) + 1;
                    sendSelectRecipe(handler.syncId, prev);
                }
        ).dimensions(x + 22, y + 4, 14, 12).tooltip(Tooltip.of(Text.literal("Previous Blueprint"))).build());

        // 2. Blueprint Selector Pill [ 🏎️ RC Car (2/8) ▾ ]
        String icon = getRecipeIcon(currentRecipe);
        String btnTitle = icon + " " + currentRecipe.getDisplayName() + " (" + currentIndex + "/" + allRecipes.length + ") ▾";
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(btnTitle),
                btn -> {
                    showDropdown = !showDropdown;
                    rebuildScreenWidgets();
                }
        ).dimensions(x + 38, y + 4, 146, 12).tooltip(Tooltip.of(Text.literal("Click to switch blueprint"))).build());

        // 3. Next Blueprint Button [ ▶ ]
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("▶"),
                btn -> {
                    showDropdown = false;
                    int total = allRecipes.length;
                    int next = (handler.getSelectedRecipeIndex() % total) + 1;
                    sendSelectRecipe(handler.syncId, next);
                }
        ).dimensions(x + 186, y + 4, 14, 12).tooltip(Tooltip.of(Text.literal("Next Blueprint"))).build());

        // 4. Tips Button [ 💡 ]
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💡"),
                btn -> {
                    if (client != null) {
                        int energy = handler.getEnergy();
                        int max = handler.getMaxEnergy();
                        String status = "⚡ Combiner: " + energy + " / " + max + " EU | Progress: "
                                + (int) ((handler.getProgress() / (double) Math.max(1, handler.getMaxProgress())) * 100) + "%";
                        client.setScreen(new ModTipScreen(TipTopic.ELECTRONIC_COMBINER, energy, max, status));
                    }
                }
        ).dimensions(x + 208, y + 4, 22, 12).tooltip(Tooltip.of(Text.literal("💡 Combiner Guide & Tips"))).build());

        // 5. Dropdown Menu Grid Buttons (if open)
        if (showDropdown) {
            for (int i = 0; i < allRecipes.length; i++) {
                CombinerRecipe r = allRecipes[i];
                final int recipeIdx = i + 1;
                int col = i % 2;
                int row = i / 2;
                int bx = x + 24 + col * 88;
                int by = y + 20 + row * 12;

                String rIcon = getRecipeIcon(r);
                boolean isSelected = recipeIdx == currentIndex;
                String label = (isSelected ? "§e▶ " : "§f") + rIcon + " " + r.getDisplayName();

                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal(label),
                        btn -> {
                            showDropdown = false;
                            sendSelectRecipe(handler.syncId, recipeIdx);
                        }
                ).dimensions(bx, by, 86, 11).tooltip(Tooltip.of(Text.literal(r.getDescription()))).build());
            }
        }
    }

    private String getRecipeIcon(CombinerRecipe recipe) {
        if (recipe == null) return "⚙️";
        return switch (recipe) {
            case ELECTRIC_CAR -> "🚗";
            case RC_CAR -> "🏎️";
            case RC_DRONE -> "🚁";
            case RC_ROBOT -> "🤖";
            case RC_CONTROLLER -> "🎮";
            case STATIONARY_RC_CONTROLLER -> "🖥️";
            case RC_SENDER -> "📡";
            case RC_RECEIVER -> "📟";
        };
    }

    private void sendSelectRecipe(int syncId, int recipeIndex) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(syncId);
        buf.writeInt(recipeIndex);
        ClientPlayNetworking.send(EvecualMC.SELECT_COMBINER_RECIPE_PACKET_ID, buf);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Draw player inventory title only - container title is rendered via the top blueprint header
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        int selectedRecipeIndex = Math.max(1, handler.getSelectedRecipeIndex());

        // Sync check
        if (selectedRecipeIndex != lastRecipeIndex) {
            lastRecipeIndex = selectedRecipeIndex;
            handler.updateSlotPositions(selectedRecipeIndex);
            rebuildScreenWidgets();
        }

        CombinerRecipe recipe = handler.getSelectedRecipe();
        if (recipe == null) recipe = CombinerRecipe.ELECTRIC_CAR;

        // Render Energy Bar (x + 8, y + 20 to x + 18, y + 68 - height 48)
        int energy = handler.getEnergy();
        int maxEnergy = handler.getMaxEnergy();
        if (maxEnergy > 0) {
            int energyHeight = (int) (48.0 * ((double) energy / maxEnergy));
            if (energyHeight > 0) {
                context.fill(x + 8, y + 20 + (48 - energyHeight), x + 18, y + 68, 0xFFF59E0B);
            }
        }

        // Blueprint CAD Canvas (x + 22 to x + 200, y + 19 to y + 71 - size 178x52)
        int bx = x + 22;
        int by = y + 19;
        int bw = 178;
        int bh = 52;

        // CAD Grid Background
        context.fill(bx, by, bx + bw, by + bh, 0xFF0B132B);
        for (int gx = bx + 12; gx < bx + bw; gx += 14) {
            context.fill(gx, by, gx + 1, by + bh, 0x1538BDF8);
        }
        for (int gy = by + 8; gy < by + bh; gy += 10) {
            context.fill(bx, gy, bx + bw, gy + 1, 0x1538BDF8);
        }

        // CAD Technical Corner Ticks
        context.drawBorder(bx, by, bw, bh, 0xFF1E293B);
        context.fill(bx, by, bx + 5, by + 1, 0xFF0EA5E9);
        context.fill(bx, by, bx + 1, by + 5, 0xFF0EA5E9);
        context.fill(bx + bw - 5, by, bx + bw, by + 1, 0xFF0EA5E9);
        context.fill(bx + bw - 1, by, bx + bw, by + 5, 0xFF0EA5E9);
        context.fill(bx, by + bh - 1, bx + 5, by + bh, 0xFF0EA5E9);
        context.fill(bx, by + bh - 5, bx + 1, by + bh, 0xFF0EA5E9);
        context.fill(bx + bw - 5, by + bh - 1, bx + bw, by + bh, 0xFF0EA5E9);
        context.fill(bx + bw - 1, by + bh - 5, bx + bw, by + bh, 0xFF0EA5E9);

        // Technical Watermark
        context.drawText(textRenderer, "CAD SIDE PROFILE", bx + 3, by + 3, 0x3338BDF8, false);

        // Render CAD Side-View Illustration
        drawMachineSideView(context, recipe, bx, by, bw, bh);

        // Render Progress Coupling into Output Slot
        int progress = handler.getProgress();
        int maxProgress = handler.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) (6.0 * ((double) progress / maxProgress));
            context.fill(x + 201, y + 44, x + 201 + progressWidth, y + 47, 0xFF38BDF8);
        }

        // Render Sockets 0..5 (Physical Mounting Points)
        for (int i = 0; i < 6; i++) {
            Slot slot = handler.slots.get(i);
            if (slot.x < 0) continue;
            CombinerRecipe.SlotRequirement req = recipe.getSlotRequirement(i);
            if (req != null) {
                int sx = x + slot.x;
                int sy = y + slot.y;

                // Socket Housing
                context.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xDD0F172A);

                int borderCol = 0xFF334155;
                if (slot.hasStack()) {
                    borderCol = slot.getStack().getCount() >= req.getRequiredCount() ? 0xFF22C55E : 0xFFF59E0B;
                } else {
                    borderCol = req.isOptional() ? 0xFF64748B : 0xFF0EA5E9;
                }
                context.drawBorder(sx - 1, sy - 1, 18, 18, borderCol);

                if (!slot.hasStack()) {
                    ItemStack ghostStack = req.getDisplayIcon();
                    if (!ghostStack.isEmpty()) {
                        context.drawItem(ghostStack, sx, sy);
                        RenderSystem.enableBlend();
                        context.fill(sx, sy, sx + 16, sy + 16, 0x66000000);
                        String countStr = String.valueOf(req.getRequiredCount());
                        context.drawTextWithShadow(textRenderer, countStr, sx + 16 - textRenderer.getWidth(countStr), sy + 9, 0xFFE2E8F0);
                    }
                } else {
                    if (slot.getStack().getCount() < req.getRequiredCount()) {
                        String countStr = slot.getStack().getCount() + "/" + req.getRequiredCount();
                        context.drawTextWithShadow(textRenderer, countStr, sx + 16 - textRenderer.getWidth(countStr), sy + 9, 0xFFF87171);
                    } else {
                        // Green Check Dot
                        context.fill(sx + 1, sy + 1, sx + 3, sy + 3, 0xFF22C55E);
                    }
                }
            }
        }

        // Render Ghost Output in Slot 6 if empty
        Slot outSlot = handler.slots.get(6);
        if (!outSlot.hasStack()) {
            ItemStack ghostOut = recipe.getOutputTemplate();
            if (!ghostOut.isEmpty()) {
                context.drawItem(ghostOut, x + outSlot.x, y + outSlot.y);
                RenderSystem.enableBlend();
                context.fill(x + outSlot.x, y + outSlot.y, x + outSlot.x + 16, y + outSlot.y + 16, 0x77000000);
            }
        }

        // Dropdown Menu Overlay Fill (if open)
        if (showDropdown) {
            context.fill(bx, by, bx + bw, by + bh, 0xF50A0F1D);
            context.drawBorder(bx, by, bw, bh, 0xFF0EA5E9);
            return;
        }

        // Real-Time HUD Status Line (y + 74)
        Slot hoveredSlot = this.focusedSlot;
        if (hoveredSlot == null) {
            for (int i = 0; i < 6; i++) {
                Slot s = handler.slots.get(i);
                if (s.x >= 0 && mouseX >= x + s.x && mouseX <= x + s.x + 16 && mouseY >= y + s.y && mouseY <= y + s.y + 16) {
                    hoveredSlot = s;
                    break;
                }
            }
        }

        boolean hasEnergy = handler.getEnergy() >= 1;
        boolean isCrafting = handler.isCrafting();

        if (hoveredSlot != null && hoveredSlot.id >= 0 && hoveredSlot.id < 6) {
            CombinerRecipe.SlotRequirement req = recipe.getSlotRequirement(hoveredSlot.id);
            if (req != null) {
                String slotInfoText = "§e[" + req.getName() + "]: §b" + req.getAcceptedItemName() + " §7(" + req.getRequiredCount() + "x)";
                context.drawTextWithShadow(textRenderer, slotInfoText, x + 24, y + 74, 0xFFFFFF);
            }
        } else if (isCrafting) {
            int pct = (int) (((double) handler.getProgress() / Math.max(1, handler.getMaxProgress())) * 100);
            context.drawTextWithShadow(textRenderer, "§b⚡ Fabricating " + recipe.getDisplayName() + "... (" + pct + "%)", x + 24, y + 74, 0xFFFFFF);
        } else if (!hasEnergy) {
            context.drawTextWithShadow(textRenderer, "§c⚡ Power Required (Connect Solar, Battery, Wire)", x + 24, y + 74, 0xFFFFFF);
        } else {
            List<ItemStack> currentInputs = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                currentInputs.add(handler.slots.get(i).getStack());
            }
            if (recipe.canCraft(currentInputs)) {
                context.drawTextWithShadow(textRenderer, "§a✔ Components Verified — Ready to Fabricate!", x + 24, y + 74, 0xFFFFFF);
            } else {
                context.drawTextWithShadow(textRenderer, "§7Mount required items into machine sockets above", x + 24, y + 74, 0xFFFFFF);
            }
        }
    }

    private void drawMachineSideView(DrawContext context, CombinerRecipe recipe, int bx, int by, int bw, int bh) {
        switch (recipe) {
            case ELECTRIC_CAR -> {
                int floorY = by + 45;
                drawCadWheel(context, bx + 42, floorY, 6);
                drawCadWheel(context, bx + 136, floorY, 6);
                context.fill(bx + 48, floorY - 2, bx + 130, floorY, 0xFF475569);
                context.fill(bx + 20, floorY - 14, bx + 54, floorY - 8, 0xFF334155);
                context.fill(bx + 16, floorY - 10, bx + 20, floorY - 6, 0xFF64748B);
                context.fill(bx + 54, floorY - 24, bx + 78, floorY - 14, 0x5538BDF8);
                context.fill(bx + 78, floorY - 25, bx + 120, floorY - 23, 0xFF475569);
                context.fill(bx + 78, floorY - 23, bx + 122, floorY - 13, 0x4438BDF8);
                context.fill(bx + 120, floorY - 20, bx + 148, floorY - 10, 0xFF334155);
                context.fill(bx + 148, floorY - 12, bx + 154, floorY - 8, 0xFF64748B);
                context.fill(bx + 16, floorY - 13, bx + 19, floorY - 11, 0xFF38BDF8);
                context.fill(bx + 152, floorY - 13, bx + 155, floorY - 11, 0xFFEF4444);
            }
            case RC_CAR -> {
                int floorY = by + 45;
                drawCadWheel(context, bx + 38, floorY, 7);
                drawCadWheel(context, bx + 134, floorY, 8);
                context.fill(bx + 18, floorY - 12, bx + 24, floorY - 4, 0xFF0EA5E9);
                context.fill(bx + 24, floorY - 8, bx + 64, floorY - 4, 0xFF38BDF8);
                context.fill(bx + 64, floorY - 20, bx + 104, floorY - 18, 0xFF0284C7);
                context.fill(bx + 122, floorY - 26, bx + 146, floorY - 24, 0xFF38BDF8);
                context.fill(bx + 132, floorY - 24, bx + 135, floorY - 12, 0xFF0EA5E9);
                context.fill(bx + 104, floorY - 32, bx + 105, floorY - 20, 0xFFE2E8F0);
            }
            case RC_DRONE -> {
                int cy = by + 30;
                context.fill(bx + 74, cy - 8, bx + 112, cy + 6, 0xFF1E293B);
                context.drawBorder(bx + 74, cy - 8, 38, 14, 0xFF0EA5E9);
                context.fill(bx + 89, cy - 14, bx + 97, cy - 8, 0xFF38BDF8);
                context.fill(bx + 80, cy + 6, bx + 106, cy + 12, 0xFFF59E0B);
                context.fill(bx + 64, cy + 15, bx + 122, cy + 17, 0xFF64748B);
                context.fill(bx + 74, cy + 6, bx + 76, cy + 15, 0xFF475569);
                context.fill(bx + 110, cy + 6, bx + 112, cy + 15, 0xFF475569);
                context.fill(bx + 26, cy - 3, bx + 74, cy - 1, 0xFF475569);
                context.fill(bx + 112, cy - 3, bx + 160, cy - 1, 0xFF475569);
                context.fill(bx + 22, cy - 7, bx + 30, cy + 1, 0xFF0EA5E9);
                context.fill(bx + 156, cy - 7, bx + 164, cy + 1, 0xFF0EA5E9);
                context.fill(bx + 10, cy - 8, bx + 42, cy - 7, 0x8838BDF8);
                context.fill(bx + 144, cy - 8, bx + 176, cy - 7, 0x8838BDF8);
            }
            case RC_ROBOT -> {
                int floorY = by + 46;
                drawCadTrack(context, bx + 42, floorY - 10, 102, 10);
                context.fill(bx + 64, floorY - 26, bx + 118, floorY - 10, 0xFF1E293B);
                context.drawBorder(bx + 64, floorY - 26, 54, 16, 0xFF0EA5E9);
                context.fill(bx + 48, floorY - 32, bx + 64, floorY - 20, 0xFF334155);
                context.fill(bx + 46, floorY - 28, bx + 48, floorY - 24, 0xFF00E5FF);
                context.fill(bx + 22, floorY - 22, bx + 48, floorY - 18, 0xFFF59E0B);
                context.fill(bx + 18, floorY - 26, bx + 24, floorY - 14, 0xFFE2E8F0);
                context.fill(bx + 118, floorY - 28, bx + 148, floorY - 10, 0xFF854D0E);
                context.drawBorder(bx + 118, floorY - 28, 30, 18, 0xFFEAB308);
            }
            case RC_CONTROLLER -> {
                int cy = by + 28;
                context.fill(bx + 48, cy - 14, bx + 140, cy + 12, 0xFF1E293B);
                context.drawBorder(bx + 48, cy - 14, 92, 26, 0xFF38BDF8);
                context.fill(bx + 42, cy, bx + 48, cy + 18, 0xFF0F172A);
                context.fill(bx + 140, cy, bx + 146, cy + 18, 0xFF0F172A);
                context.fill(bx + 76, cy - 10, bx + 112, cy + 4, 0xFF0284C7);
                context.drawBorder(bx + 76, cy - 10, 36, 14, 0xFF38BDF8);
                context.fill(bx + 128, cy - 24, bx + 130, cy - 14, 0xFFE2E8F0);
                context.fill(bx + 127, cy - 26, bx + 131, cy - 24, 0xFFF59E0B);
            }
            case STATIONARY_RC_CONTROLLER -> {
                int floorY = by + 46;
                context.fill(bx + 62, floorY - 4, bx + 132, floorY, 0xFF475569);
                context.drawBorder(bx + 62, floorY - 4, 70, 4, 0xFF94A3B8);
                context.fill(bx + 90, floorY - 22, bx + 104, floorY - 4, 0xFF334155);
                context.fill(bx + 68, floorY - 24, bx + 126, floorY - 20, 0xFF1E293B);
                context.fill(bx + 46, floorY - 38, bx + 84, floorY - 22, 0xFF0284C7);
                context.drawBorder(bx + 46, floorY - 38, 38, 16, 0xFF38BDF8);
                context.fill(bx + 126, floorY - 38, bx + 144, floorY - 22, 0xFF64748B);
                context.fill(bx + 122, floorY - 31, bx + 126, floorY - 29, 0xFFF59E0B);
            }
            case RC_SENDER -> {
                int cy = by + 28;
                context.fill(bx + 52, cy - 6, bx + 142, cy + 14, 0xFF065F46);
                context.drawBorder(bx + 52, cy - 6, 90, 20, 0xFF10B981);
                context.fill(bx + 92, cy - 24, bx + 95, cy - 6, 0xFFE2E8F0);
                context.fill(bx + 91, cy - 26, bx + 96, cy - 24, 0xFFF59E0B);
                context.fill(bx + 58, cy - 2, bx + 76, cy + 10, 0xFF1E293B);
                context.fill(bx + 86, cy, bx + 102, cy + 8, 0xFFB45309);
                context.fill(bx + 112, cy - 2, bx + 134, cy + 10, 0xFF64748B);
            }
            case RC_RECEIVER -> {
                int cy = by + 28;
                context.fill(bx + 52, cy - 8, bx + 142, cy + 12, 0xFF1E1B4B);
                context.drawBorder(bx + 52, cy - 8, 90, 20, 0xFF6366F1);
                context.fill(bx + 114, cy - 4, bx + 134, cy + 6, 0xFFE0E7FF);
                context.drawBorder(bx + 114, cy - 4, 20, 10, 0xFFA5B4FC);
                context.fill(bx + 58, cy - 4, bx + 78, cy + 6, 0xFF0F172A);
                for (int px = bx + 86; px <= bx + 106; px += 5) {
                    context.fill(px, cy - 14, px + 2, cy - 8, 0xFFD97706);
                }
            }
        }
    }

    private void drawCadWheel(DrawContext context, int cx, int cy, int radius) {
        context.fill(cx - radius, cy - radius, cx + radius, cy + radius, 0xFF0F172A);
        context.fill(cx - radius + 2, cy - radius + 2, cx + radius - 2, cy + radius - 2, 0xFF64748B);
        context.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF38BDF8);
    }

    private void drawCadTrack(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, 0xFF0F172A);
        context.drawBorder(x, y, w, h, 0xFF475569);
        int wheelCount = 5;
        int step = (w - 14) / (wheelCount - 1);
        for (int i = 0; i < wheelCount; i++) {
            int wx = x + 7 + i * step;
            int wy = y + h / 2;
            context.fill(wx - 3, wy - 3, wx + 3, wy + 3, 0xFF64748B);
            context.fill(wx - 1, wy - 1, wx + 1, wy + 1, 0xFF0EA5E9);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showDropdown) {
            int x = (width - backgroundWidth) / 2;
            int y = (height - backgroundHeight) / 2;
            int bx = x + 22;
            int by = y + 19;
            int bw = 178;
            int bh = 52;
            if (mouseX < bx || mouseX > bx + bw || mouseY < by || mouseY > by + bh) {
                showDropdown = false;
                rebuildScreenWidgets();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showDropdown && (keyCode == 256 || keyCode == 259)) { // ESC or Backspace
            showDropdown = false;
            rebuildScreenWidgets();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        if (!showDropdown) {
            // Energy Tooltip (8, 20 to 18, 68)
            if (mouseX >= x + 7 && mouseX <= x + 19 && mouseY >= y + 19 && mouseY <= y + 69) {
                context.drawTooltip(textRenderer, Text.literal("⚡ Energy: " + handler.getEnergy() + " / " + handler.getMaxEnergy() + " EU"), mouseX, mouseY);
            }

            // Slot hover tooltips
            CombinerRecipe recipe = handler.getSelectedRecipe();
            if (recipe != null) {
                Slot hoveredSlot = this.focusedSlot;
                if (hoveredSlot == null) {
                    for (int i = 0; i <= 6; i++) {
                        Slot s = handler.slots.get(i);
                        if (s.x >= 0 && mouseX >= x + s.x && mouseX <= x + s.x + 16 && mouseY >= y + s.y && mouseY <= y + s.y + 16) {
                            hoveredSlot = s;
                            break;
                        }
                    }
                }

                if (hoveredSlot != null && hoveredSlot.id >= 0 && hoveredSlot.id <= 6) {
                    if (hoveredSlot.id < 6) {
                        CombinerRecipe.SlotRequirement req = recipe.getSlotRequirement(hoveredSlot.id);
                        if (req != null) {
                            List<Text> tooltip = new ArrayList<>();
                            tooltip.add(Text.literal("§e⚡ " + req.getName()));
                            tooltip.add(Text.literal("§bRequired Item: §f" + req.getAcceptedItemName()));
                            tooltip.add(Text.literal("§7Machine Socket: §d" + req.getPlacementLocation()));
                            tooltip.add(Text.literal("§7Quantity Needed: §f" + req.getRequiredCount() + "x" + (req.isOptional() ? " §8(Optional Upgrade)" : " §c(Mandatory)")));
                            if (hoveredSlot.hasStack()) {
                                int cur = hoveredSlot.getStack().getCount();
                                String curColor = cur >= req.getRequiredCount() ? "§a" : "§c";
                                String status = cur >= req.getRequiredCount() ? " §a✔ Mounted" : " §c✘ Insufficient";
                                tooltip.add(Text.literal("§7In Socket: " + curColor + cur + "§7 / §f" + req.getRequiredCount() + status));
                            } else {
                                tooltip.add(Text.literal("§7In Socket: §c0 / " + req.getRequiredCount() + " (Empty)"));
                            }
                            tooltip.add(Text.literal("§8Slot only accepts this specific machine component."));
                            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                        }
                    } else {
                        List<Text> tooltip = new ArrayList<>();
                        tooltip.add(Text.literal("§aOutput: " + recipe.getDisplayName()));
                        tooltip.add(Text.literal("§7" + recipe.getDescription()));
                        context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                    }
                }
            }
        }
    }
}
