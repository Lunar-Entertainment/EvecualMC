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
        this.backgroundWidth = 240;
        this.backgroundHeight = 174;
        this.playerInventoryTitleX = 39;
        this.playerInventoryTitleY = 75;
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

        // Always add the Tips button in top right
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
            // Back / Change Blueprint button in top left
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
            ).dimensions(x + 30, y + 26, 180, 23).tooltip(Tooltip.of(Text.literal("Assemble drivable electric road vehicles"))).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("📡 Radio Control (RC)"),
                    btn -> {
                        currentCategory = CombinerRecipe.Category.RC;
                        currentView = ViewState.SUB_SELECT;
                        rebuildScreenWidgets();
                    }
            ).dimensions(x + 30, y + 52, 180, 23).tooltip(Tooltip.of(Text.literal("Fabricate RC drones, rovers, controllers & micro-transceivers"))).build());
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
                    ).dimensions(x + 30, y + 36 + i * 26, 180, 24).tooltip(Tooltip.of(Text.literal(recipe.getDescription()))).build());
                }
            } else if (currentCategory == CombinerRecipe.Category.RC) {
                // RC Options: RC Car, RC Drone, RC Robot, RC Controller, Stationary RC Controller, RC Sender, RC Receiver
                List<CombinerRecipe> rcRecipes = CombinerRecipe.getRecipesForCategory(CombinerRecipe.Category.RC);
                for (int i = 0; i < rcRecipes.size(); i++) {
                    CombinerRecipe recipe = rcRecipes.get(i);
                    int col = i % 2;
                    int row = i / 2;
                    int bx = (col == 0) ? (x + 16) : (x + 124);
                    int by = y + 22 + row * 20;

                    String icon = switch (recipe) {
                        case RC_CAR -> "🏎️ ";
                        case RC_DRONE -> "🚁 ";
                        case RC_ROBOT -> "🤖 ";
                        case RC_CONTROLLER -> "🎮 ";
                        case STATIONARY_RC_CONTROLLER -> "🖥️ ";
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
                    ).dimensions(bx, by, 100, 18).tooltip(Tooltip.of(Text.literal(recipe.getDescription()))).build());
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
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        int selectedRecipeIndex = handler.getSelectedRecipeIndex();

        // Check if recipe index synchronized from server changed
        if (selectedRecipeIndex != lastRecipeIndex) {
            lastRecipeIndex = selectedRecipeIndex;
            handler.updateSlotPositions(selectedRecipeIndex);
            rebuildScreenWidgets();
        }

        if (selectedRecipeIndex == 0) {
            // Render Menu Stage Overlay covering the crafting slots
            context.fill(x + 14, y + 19, x + 226, y + 71, 0xEE0F172A);
            context.drawBorder(x + 14, y + 19, 212, 52, 0xFF38BDF8);

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
        context.drawCenteredTextWithShadow(textRenderer, "§6⚡ " + recipe.getDisplayName() + " Blueprint", x + backgroundWidth / 2, y + 7, 0xFFF59E0B);

        // Render Energy Bar (8, 20 to 18, 68 - height 48)
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

        // Subtle blueprint background and technical grid
        context.fill(bx, by, bx + bw, by + bh, 0xEE0B132B);
        for (int gx = bx + 12; gx < bx + bw; gx += 14) {
            context.fill(gx, by, gx + 1, by + bh, 0x1438BDF8);
        }
        for (int gy = by + 8; gy < by + bh; gy += 10) {
            context.fill(bx, gy, bx + bw, gy + 1, 0x1438BDF8);
        }
        // CAD Technical border ticks
        context.drawBorder(bx, by, bw, bh, 0xFF1E293B);
        context.fill(bx, by, bx + 5, by + 1, 0xFF0EA5E9);
        context.fill(bx, by, bx + 1, by + 5, 0xFF0EA5E9);
        context.fill(bx + bw - 5, by, bx + bw, by + 1, 0xFF0EA5E9);
        context.fill(bx + bw - 1, by, bx + bw, by + 5, 0xFF0EA5E9);
        context.fill(bx, by + bh - 1, bx + 5, by + bh, 0xFF0EA5E9);
        context.fill(bx, by + bh - 5, bx + 1, by + bh, 0xFF0EA5E9);
        context.fill(bx + bw - 5, by + bh - 1, bx + bw, by + bh, 0xFF0EA5E9);
        context.fill(bx + bw - 1, by + bh - 5, bx + bw, by + bh, 0xFF0EA5E9);

        // Technical side-view CAD watermark text
        context.drawText(textRenderer, "SIDE PROFILE", bx + 3, by + 3, 0x3338BDF8, false);

        // Render CAD side-view vector illustration of the selected machine
        drawMachineSideView(context, recipe, bx, by, bw, bh);

        // Render Progress Arrow / Energy Coupling into Output Slot
        int progress = handler.getProgress();
        int maxProgress = handler.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) (8.0 * ((double) progress / maxProgress));
            context.fill(x + 201, y + 44, x + 201 + progressWidth, y + 47, 0xFF38BDF8);
        }

        // Render Slots 0..5 (Physical mounting positions on the machine)
        for (int i = 0; i < 6; i++) {
            Slot slot = handler.slots.get(i);
            if (slot.x < 0) continue;
            CombinerRecipe.SlotRequirement req = recipe.getSlotRequirement(i);
            if (req != null) {
                int sx = x + slot.x;
                int sy = y + slot.y;

                // Technical mounting socket frame (18x18)
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
                        // Small green check tick
                        context.fill(sx + 1, sy + 1, sx + 3, sy + 3, 0xFF22C55E);
                    }
                }
            }
        }

        // Render Ghost Output in slot 6 if empty
        Slot outSlot = handler.slots.get(6);
        if (!outSlot.hasStack()) {
            ItemStack ghostOut = recipe.getOutputTemplate();
            if (!ghostOut.isEmpty()) {
                context.drawItem(ghostOut, x + outSlot.x, y + outSlot.y);
                RenderSystem.enableBlend();
                context.fill(x + outSlot.x, y + outSlot.y, x + outSlot.x + 16, y + outSlot.y + 16, 0x77000000);
            }
        }

        // Dynamic Real-Time Slot Info & Status Display
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
            context.drawTextWithShadow(textRenderer, "§b⚡ Assembling " + recipe.getDisplayName() + "... (" + pct + "%)", x + 24, y + 74, 0xFFFFFF);
        } else if (!hasEnergy) {
            context.drawTextWithShadow(textRenderer, "§c⚡ Needs Energy (Connect Solar/Battery)", x + 24, y + 74, 0xFFFFFF);
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
                // Electric Car side profile: sleek coupe silhouette
                int floorY = by + 45;
                // Tires: front and rear
                drawCadWheel(context, bx + 42, floorY, 6);
                drawCadWheel(context, bx + 136, floorY, 6);
                // Lower chassis frame
                context.fill(bx + 48, floorY - 2, bx + 130, floorY, 0xFF475569);
                // Front hood and bumper
                context.fill(bx + 20, floorY - 14, bx + 54, floorY - 8, 0xFF334155);
                context.fill(bx + 16, floorY - 10, bx + 20, floorY - 6, 0xFF64748B);
                // Windshield rake
                context.fill(bx + 54, floorY - 24, bx + 78, floorY - 14, 0x5538BDF8);
                // Cabin roof line
                context.fill(bx + 78, floorY - 25, bx + 120, floorY - 23, 0xFF475569);
                // Cabin window tint
                context.fill(bx + 78, floorY - 23, bx + 122, floorY - 13, 0x4438BDF8);
                // Rear trunk slope and bumper
                context.fill(bx + 120, floorY - 20, bx + 148, floorY - 10, 0xFF334155);
                context.fill(bx + 148, floorY - 12, bx + 154, floorY - 8, 0xFF64748B);
                // Headlight LED (bright cyan)
                context.fill(bx + 16, floorY - 13, bx + 19, floorY - 11, 0xFF38BDF8);
                // Taillight LED (crimson red)
                context.fill(bx + 152, floorY - 13, bx + 155, floorY - 11, 0xFFEF4444);
            }
            case RC_CAR -> {
                // RC Buggy side profile
                int floorY = by + 45;
                // Knobby off-road tires
                drawCadWheel(context, bx + 38, floorY, 7);
                drawCadWheel(context, bx + 134, floorY, 8);
                // Front bumper bullbar
                context.fill(bx + 18, floorY - 12, bx + 24, floorY - 4, 0xFF0EA5E9);
                // Roll-cage tube frame
                context.fill(bx + 24, floorY - 8, bx + 64, floorY - 4, 0xFF38BDF8);
                context.fill(bx + 64, floorY - 20, bx + 104, floorY - 18, 0xFF0284C7);
                // Rear high-downforce spoiler wing
                context.fill(bx + 122, floorY - 26, bx + 146, floorY - 24, 0xFF38BDF8);
                context.fill(bx + 132, floorY - 24, bx + 135, floorY - 12, 0xFF0EA5E9);
                // Roof whip antenna
                context.fill(bx + 104, floorY - 32, bx + 105, floorY - 20, 0xFFE2E8F0);
            }
            case RC_DRONE -> {
                // RC Drone side profile: quadcopter
                int cy = by + 30;
                // Center flight avionics pod
                context.fill(bx + 74, cy - 8, bx + 112, cy + 6, 0xFF1E293B);
                context.drawBorder(bx + 74, cy - 8, 38, 14, 0xFF0EA5E9);
                // Top antenna dome
                context.fill(bx + 89, cy - 14, bx + 97, cy - 8, 0xFF38BDF8);
                // Underslung battery pack
                context.fill(bx + 80, cy + 6, bx + 106, cy + 12, 0xFFF59E0B);
                // Landing skids
                context.fill(bx + 64, cy + 15, bx + 122, cy + 17, 0xFF64748B);
                context.fill(bx + 74, cy + 6, bx + 76, cy + 15, 0xFF475569);
                context.fill(bx + 110, cy + 6, bx + 112, cy + 15, 0xFF475569);
                // Left & Right rotor boom arms
                context.fill(bx + 26, cy - 3, bx + 74, cy - 1, 0xFF475569);
                context.fill(bx + 112, cy - 3, bx + 160, cy - 1, 0xFF475569);
                // Motor bells
                context.fill(bx + 22, cy - 7, bx + 30, cy + 1, 0xFF0EA5E9);
                context.fill(bx + 156, cy - 7, bx + 164, cy + 1, 0xFF0EA5E9);
                // Spinning propeller blur disks
                context.fill(bx + 10, cy - 8, bx + 42, cy - 7, 0x8838BDF8);
                context.fill(bx + 144, cy - 8, bx + 176, cy - 7, 0x8838BDF8);
            }
            case RC_ROBOT -> {
                // RC Robot side profile: tracked excavation droid
                int floorY = by + 46;
                // Caterpillar tank treads
                drawCadTrack(context, bx + 42, floorY - 10, 102, 10);
                // Armored main torso
                context.fill(bx + 64, floorY - 26, bx + 118, floorY - 10, 0xFF1E293B);
                context.drawBorder(bx + 64, floorY - 26, 54, 16, 0xFF0EA5E9);
                // Sensor head with optical visor
                context.fill(bx + 48, floorY - 32, bx + 64, floorY - 20, 0xFF334155);
                context.fill(bx + 46, floorY - 28, bx + 48, floorY - 24, 0xFF00E5FF); // Glowing cyan visor
                // Front excavator arm
                context.fill(bx + 22, floorY - 22, bx + 48, floorY - 18, 0xFFF59E0B);
                context.fill(bx + 18, floorY - 26, bx + 24, floorY - 14, 0xFFE2E8F0);
                // Rear cargo storage hopper
                context.fill(bx + 118, floorY - 28, bx + 148, floorY - 10, 0xFF854D0E);
                context.drawBorder(bx + 118, floorY - 28, 30, 18, 0xFFEAB308);
            }
            case RC_CONTROLLER -> {
                // Handheld controller contour
                int cy = by + 28;
                // Controller main body
                context.fill(bx + 48, cy - 14, bx + 140, cy + 12, 0xFF1E293B);
                context.drawBorder(bx + 48, cy - 14, 92, 26, 0xFF38BDF8);
                // Left & Right grip wings
                context.fill(bx + 42, cy, bx + 48, cy + 18, 0xFF0F172A);
                context.fill(bx + 140, cy, bx + 146, cy + 18, 0xFF0F172A);
                // Central color telemetry screen
                context.fill(bx + 76, cy - 10, bx + 112, cy + 4, 0xFF0284C7);
                context.drawBorder(bx + 76, cy - 10, 36, 14, 0xFF38BDF8);
                // Top antenna mast
                context.fill(bx + 128, cy - 24, bx + 130, cy - 14, 0xFFE2E8F0);
                context.fill(bx + 127, cy - 26, bx + 131, cy - 24, 0xFFF59E0B);
            }
            case STATIONARY_RC_CONTROLLER -> {
                // Standing terminal console
                int floorY = by + 46;
                // Heavy floor pedestal
                context.fill(bx + 62, floorY - 4, bx + 132, floorY, 0xFF475569);
                context.drawBorder(bx + 62, floorY - 4, 70, 4, 0xFF94A3B8);
                // Central vertical support column
                context.fill(bx + 90, floorY - 22, bx + 104, floorY - 4, 0xFF334155);
                // Angled keyboard desk & terminal controls
                context.fill(bx + 68, floorY - 24, bx + 126, floorY - 20, 0xFF1E293B);
                // High-resolution display monitor
                context.fill(bx + 46, floorY - 38, bx + 84, floorY - 22, 0xFF0284C7);
                context.drawBorder(bx + 46, floorY - 38, 38, 16, 0xFF38BDF8);
                // Rear high-gain dish booster
                context.fill(bx + 126, floorY - 38, bx + 144, floorY - 22, 0xFF64748B);
                context.fill(bx + 122, floorY - 31, bx + 126, floorY - 29, 0xFFF59E0B);
            }
            case RC_SENDER -> {
                // Telecommand broadcast PCB module
                int cy = by + 28;
                // Circuit PCB board substrate
                context.fill(bx + 52, cy - 6, bx + 142, cy + 14, 0xFF065F46); // Green PCB
                context.drawBorder(bx + 52, cy - 6, 90, 20, 0xFF10B981);
                // Vertical broadcast antenna rod
                context.fill(bx + 92, cy - 24, bx + 95, cy - 6, 0xFFE2E8F0);
                context.fill(bx + 91, cy - 26, bx + 96, cy - 24, 0xFFF59E0B);
                // Logic IC processor chip
                context.fill(bx + 58, cy - 2, bx + 76, cy + 10, 0xFF1E293B);
                // RF induction copper coil
                context.fill(bx + 86, cy, bx + 102, cy + 8, 0xFFB45309);
                // Steel EMI shield box
                context.fill(bx + 112, cy - 2, bx + 134, cy + 10, 0xFF64748B);
            }
            case RC_RECEIVER -> {
                // Miniature crystal receiver PCB
                int cy = by + 28;
                // Circuit PCB substrate
                context.fill(bx + 52, cy - 8, bx + 142, cy + 12, 0xFF1E1B4B); // Cyber Navy PCB
                context.drawBorder(bx + 52, cy - 8, 90, 20, 0xFF6366F1);
                // Frequency quartz crystal
                context.fill(bx + 114, cy - 4, bx + 134, cy + 6, 0xFFE0E7FF);
                context.drawBorder(bx + 114, cy - 4, 20, 10, 0xFFA5B4FC);
                // Logic comparator IC
                context.fill(bx + 58, cy - 4, bx + 78, cy + 6, 0xFF0F172A);
                // Copper header pins
                for (int px = bx + 86; px <= bx + 106; px += 5) {
                    context.fill(px, cy - 14, px + 2, cy - 8, 0xFFD97706);
                }
            }
        }
    }

    private void drawCadWheel(DrawContext context, int cx, int cy, int radius) {
        // Outer rubber tire
        context.fill(cx - radius, cy - radius, cx + radius, cy + radius, 0xFF0F172A);
        // Inner alloy rim
        context.fill(cx - radius + 2, cy - radius + 2, cx + radius - 2, cy + radius - 2, 0xFF64748B);
        // Center hub axle
        context.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF38BDF8);
    }

    private void drawCadTrack(DrawContext context, int x, int y, int w, int h) {
        // Outer track belt outline
        context.fill(x, y, x + w, y + h, 0xFF0F172A);
        context.drawBorder(x, y, w, h, 0xFF475569);
        // Road wheels inside tread
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        if (handler.getSelectedRecipeIndex() > 0) {
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
