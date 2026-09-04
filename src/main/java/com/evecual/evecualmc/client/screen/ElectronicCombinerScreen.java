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

    public enum ViewState {
        CATEGORY_SELECT,
        SUB_SELECT,
        CRAFTING
    }

    private ViewState currentView = ViewState.CATEGORY_SELECT;
    private CombinerRecipe.Category currentCategory = CombinerRecipe.Category.VEHICLES;
    private int lastRecipeIndex = -1;

    public ElectronicCombinerScreen(ElectronicCombinerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        rebuildScreenWidgets();
    }

    private void rebuildScreenWidgets() {
        this.clearChildren();
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Always add the Tips button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💡 Tips"),
                btn -> {
                    if (client != null) {
                        int energy = handler.getEnergy();
                        int max = handler.getMaxEnergy();
                        String status = "⚡ Combiner: " + energy + " / " + max + " EU | Progress: "
                                + (int) ((handler.getProgress() / (double) Math.max(1, handler.getMaxProgress())) * 100) + "%";
                        client.setScreen(new ModTipScreen(TipTopic.ELECTRONIC_COMBINER, energy, max, status));
                    }
                }
        ).dimensions(x + backgroundWidth - 52, y + 4, 46, 14).build());

        int selectedRecipeIndex = handler.getSelectedRecipeIndex();

        if (selectedRecipeIndex > 0) {
            currentView = ViewState.CRAFTING;
            // Back / Change Blueprint button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("⬅ Menu"),
                    btn -> {
                        sendSelectRecipe(handler.syncId, 0);
                        currentView = ViewState.CATEGORY_SELECT;
                        rebuildScreenWidgets();
                    }
            ).dimensions(x + 6, y + 4, 46, 14).tooltip(Tooltip.of(Text.literal("Return to blueprint selection menu"))).build());
        } else if (currentView == ViewState.CATEGORY_SELECT) {
            // Stage 1: Category Selection (Vehicles vs RC)
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("🚗 Vehicles"),
                    btn -> {
                        currentCategory = CombinerRecipe.Category.VEHICLES;
                        currentView = ViewState.SUB_SELECT;
                        rebuildScreenWidgets();
                    }
            ).dimensions(x + 16, y + 26, 144, 23).tooltip(Tooltip.of(Text.literal("Assemble drivable electric road vehicles"))).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("📡 Radio Control (RC)"),
                    btn -> {
                        currentCategory = CombinerRecipe.Category.RC;
                        currentView = ViewState.SUB_SELECT;
                        rebuildScreenWidgets();
                    }
            ).dimensions(x + 16, y + 53, 144, 23).tooltip(Tooltip.of(Text.literal("Fabricate RC drones, rovers, controllers & micro-transceivers"))).build());
        } else if (currentView == ViewState.SUB_SELECT) {
            // Stage 2: Sub-options
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("⬅ Back"),
                    btn -> {
                        currentView = ViewState.CATEGORY_SELECT;
                        rebuildScreenWidgets();
                    }
            ).dimensions(x + 6, y + 4, 44, 14).tooltip(Tooltip.of(Text.literal("Back to categories"))).build());

            if (currentCategory == CombinerRecipe.Category.VEHICLES) {
                // Ridable vehicles (Electric Car)
                List<CombinerRecipe> vehicleRecipes = CombinerRecipe.getRecipesForCategory(CombinerRecipe.Category.VEHICLES);
                for (int i = 0; i < vehicleRecipes.size(); i++) {
                    CombinerRecipe recipe = vehicleRecipes.get(i);
                    this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("🚗 " + recipe.getDisplayName()),
                            btn -> {
                                sendSelectRecipe(handler.syncId, CombinerRecipe.getIndexForRecipe(recipe));
                                currentView = ViewState.CRAFTING;
                            }
                    ).dimensions(x + 16, y + 36 + i * 26, 144, 24).tooltip(Tooltip.of(Text.literal(recipe.getDescription()))).build());
                }
            } else if (currentCategory == CombinerRecipe.Category.RC) {
                // RC Options: RC Car, RC Drone, RC Robot, RC Controller, RC Sender, RC Receiver
                List<CombinerRecipe> rcRecipes = CombinerRecipe.getRecipesForCategory(CombinerRecipe.Category.RC);
                for (int i = 0; i < rcRecipes.size(); i++) {
                    CombinerRecipe recipe = rcRecipes.get(i);
                    int col = i % 2;
                    int row = i / 2;
                    int bx = (col == 0) ? (x + 10) : (x + 90);
                    int by = y + 22 + row * 20;

                    String icon = switch (recipe) {
                        case RC_CAR -> "🏎️ ";
                        case RC_DRONE -> "🚁 ";
                        case RC_ROBOT -> "🤖 ";
                        case RC_CONTROLLER -> "🎮 ";
                        case RC_SENDER -> "📡 ";
                        case RC_RECEIVER -> "📟 ";
                        default -> "";
                    };

                    this.addDrawableChild(ButtonWidget.builder(
                            Text.literal(icon + recipe.getDisplayName()),
                            btn -> {
                                sendSelectRecipe(handler.syncId, CombinerRecipe.getIndexForRecipe(recipe));
                                currentView = ViewState.CRAFTING;
                            }
                    ).dimensions(bx, by, 76, 18).tooltip(Tooltip.of(Text.literal(recipe.getDescription()))).build());
                }
            }
        }
    }

    private void sendSelectRecipe(int syncId, int recipeIndex) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(syncId);
        buf.writeInt(recipeIndex);
        ClientPlayNetworking.send(EvecualMC.SELECT_COMBINER_RECIPE_PACKET_ID, buf);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int selectedRecipeIndex = handler.getSelectedRecipeIndex();

        // Check if recipe index synchronized from server changed
        if (selectedRecipeIndex != lastRecipeIndex) {
            lastRecipeIndex = selectedRecipeIndex;
            rebuildScreenWidgets();
        }

        if (selectedRecipeIndex == 0) {
            // Render Menu Stage Overlay covering the crafting slots
            context.fill(x + 7, y + 19, x + 169, y + 81, 0xEE18181B);
            context.drawBorder(x + 7, y + 19, 162, 62, 0xFF3F3F46);

            if (currentView == ViewState.CATEGORY_SELECT) {
                context.drawCenteredTextWithShadow(textRenderer, "§6⚡ Select Fabrication Category", x + backgroundWidth / 2, y + 7, 0xFFF59E0B);
            } else if (currentView == ViewState.SUB_SELECT) {
                String titleText = currentCategory == CombinerRecipe.Category.VEHICLES ? "§b🚗 Select Vehicle Blueprint" : "§b📡 Select RC Blueprint";
                context.drawCenteredTextWithShadow(textRenderer, titleText, x + backgroundWidth / 2 + 10, y + 7, 0xFF38BDF8);
            }
            return;
        }

        // CRAFTING VIEW
        CombinerRecipe recipe = handler.getSelectedRecipe();
        if (recipe == null) return;

        // Render Recipe Title Header
        context.drawCenteredTextWithShadow(textRenderer, "§6" + recipe.getDisplayName(), x + backgroundWidth / 2 + 10, y + 7, 0xFFF59E0B);

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
                context.fill(x + 114, y + 33, x + 120, y + 43, 0xFF38BDF8);
            }
        }

        // Render Ghost Items and Required Counts in Slots 0..5
        for (int i = 0; i < 6; i++) {
            Slot slot = handler.slots.get(i);
            CombinerRecipe.SlotRequirement req = recipe.getSlotRequirement(i);
            if (req != null) {
                if (!slot.hasStack()) {
                    ItemStack ghostStack = req.getDisplayIcon();
                    if (!ghostStack.isEmpty()) {
                        context.drawItem(ghostStack, x + slot.x, y + slot.y);
                        RenderSystem.enableBlend();
                        context.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0x77000000);
                        String countStr = String.valueOf(req.getRequiredCount());
                        context.drawTextWithShadow(textRenderer, countStr, x + slot.x + 16 - textRenderer.getWidth(countStr), y + slot.y + 9, 0xFFCBD5E1);
                    }
                } else {
                    if (slot.getStack().getCount() < req.getRequiredCount()) {
                        String countStr = slot.getStack().getCount() + "/" + req.getRequiredCount();
                        context.drawTextWithShadow(textRenderer, countStr, x + slot.x + 16 - textRenderer.getWidth(countStr), y + slot.y + 9, 0xFFF87171);
                    } else {
                        context.drawBorder(x + slot.x - 1, y + slot.y - 1, 18, 18, 0x8822C55E);
                    }
                }
            }
        }

        // Render Ghost Output item in slot 6 if empty
        Slot outSlot = handler.slots.get(6);
        if (!outSlot.hasStack()) {
            ItemStack ghostOut = recipe.getOutputTemplate();
            if (!ghostOut.isEmpty()) {
                context.drawItem(ghostOut, x + outSlot.x, y + outSlot.y);
                RenderSystem.enableBlend();
                context.fill(x + outSlot.x, y + outSlot.y, x + outSlot.x + 16, y + outSlot.y + 16, 0x77000000);
            }
        }

        // Status Line at y + 68
        boolean hasEnergy = handler.getEnergy() >= 1;
        boolean isCrafting = handler.isCrafting();
        if (isCrafting) {
            int pct = (int) (((double) handler.getProgress() / Math.max(1, handler.getMaxProgress())) * 100);
            context.drawTextWithShadow(textRenderer, "§b⚡ Assembling " + recipe.getDisplayName() + "... (" + pct + "%)", x + 24, y + 68, 0xFFFFFF);
        } else if (!hasEnergy) {
            context.drawTextWithShadow(textRenderer, "§c⚡ Needs Energy (Connect Solar/Battery)", x + 24, y + 68, 0xFFFFFF);
        } else {
            List<ItemStack> currentInputs = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                currentInputs.add(handler.slots.get(i).getStack());
            }
            if (recipe.canCraft(currentInputs)) {
                context.drawTextWithShadow(textRenderer, "§a✔ Ready to Fabricate!", x + 24, y + 68, 0xFFFFFF);
            } else {
                context.drawTextWithShadow(textRenderer, "§7Insert required parts into slots", x + 24, y + 68, 0xFFFFFF);
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

        if (handler.getSelectedRecipeIndex() > 0) {
            // Energy Tooltip
            if (mouseX >= x + 9 && mouseX <= x + 20 && mouseY >= y + 18 && mouseY <= y + 62) {
                context.drawTooltip(textRenderer, Text.literal("⚡ Energy: " + handler.getEnergy() + " / " + handler.getMaxEnergy() + " EU"), mouseX, mouseY);
            }

            // Slot hover tooltips
            CombinerRecipe recipe = handler.getSelectedRecipe();
            if (recipe != null) {
                Slot hoveredSlot = this.focusedSlot;
                if (hoveredSlot == null) {
                    for (int i = 0; i <= 6; i++) {
                        Slot s = handler.slots.get(i);
                        if (mouseX >= x + s.x && mouseX <= x + s.x + 16 && mouseY >= y + s.y && mouseY <= y + s.y + 16) {
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
                            tooltip.add(Text.literal("§e" + req.getName()));
                            tooltip.add(Text.literal("§7Required: §f" + req.getRequiredCount() + "x" + (req.isOptional() ? " §8(Optional)" : " §c(Mandatory)")));
                            if (hoveredSlot.hasStack()) {
                                int cur = hoveredSlot.getStack().getCount();
                                String curColor = cur >= req.getRequiredCount() ? "§a" : "§c";
                                tooltip.add(Text.literal("§7In Slot: " + curColor + cur + "§7 / §f" + req.getRequiredCount()));
                            } else {
                                tooltip.add(Text.literal("§7In Slot: §c0 / " + req.getRequiredCount() + " (Empty)"));
                            }
                            tooltip.add(Text.literal("§8Slot only accepts this specific component."));
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
