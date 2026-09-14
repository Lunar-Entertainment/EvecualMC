package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ItemFabricatorBlockEntity;
import com.evecual.evecualmc.screen.ItemFabricatorScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ItemFabricatorScreen extends HandledScreen<ItemFabricatorScreenHandler> {

    private static final int[] SLOT_XS = new int[]{44, 62, 80, 44, 62, 80};
    private static final int[] SLOT_YS = new int[]{34, 34, 34, 52, 52, 52};

    public ItemFabricatorScreen(ItemFabricatorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 194;
        this.playerInventoryTitleY = 101;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Suppress default this.title to prevent duplicate overlapping header text
        // Cleanly render player inventory title above player inventory grid
        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.playerInventoryTitleY, 0xFF94A3B8, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        int recipe = this.handler.getSelectedRecipe();
        boolean zapperSelected = (recipe == ItemFabricatorBlockEntity.RECIPE_ZAPPER);
        boolean railgunSelected = (recipe == ItemFabricatorBlockEntity.RECIPE_RAILGUN);
        boolean duperSelected = (recipe == ItemFabricatorBlockEntity.RECIPE_DUPER);

        int themeBorder = zapperSelected ? 0xFF00E5FF : (railgunSelected ? 0xFFA855F7 : 0xFFF59E0B);
        int themeFill = zapperSelected ? 0xFF38BDF8 : (railgunSelected ? 0xFFC084FC : 0xFFFCD34D);

        // 1. Dark Glassmorphic Backdrop
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xF0070B14);
        // Outer Glowing Border (Cyan tech glow)
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF00E5FF);

        // 2. Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 15, 0xFF101B2E);
        context.drawText(this.textRenderer, "⚡ ITEM FABRICATOR", x + 8, y + 4, 0xFF00E5FF, false);
        context.drawText(this.textRenderer, "MK-II", x + 140, y + 4, 0xFF38BDF8, false);

        // 3. Blueprint Selection Tabs (y = 17 to 28, height 11)
        // Tab 1: ZAPPER (x: 8..58, w: 50)
        int zapperBg = zapperSelected ? 0xFF0284C7 : 0xFF0F172A;
        int zapperBorder = zapperSelected ? 0xFF38BDF8 : 0xFF334155;
        context.fill(x + 8, y + 17, x + 58, y + 28, zapperBg);
        context.drawBorder(x + 8, y + 17, 50, 11, zapperBorder);
        context.drawText(this.textRenderer, "⚡ ZAPPER", x + 10, y + 19, zapperSelected ? 0xFFFFFFFF : 0xFF94A3B8, false);

        // Tab 2: RAILGUN (x: 62..114, w: 52)
        int railgunBg = railgunSelected ? 0xFF7E22CE : 0xFF0F172A;
        int railgunBorder = railgunSelected ? 0xFFC084FC : 0xFF334155;
        context.fill(x + 62, y + 17, x + 114, y + 28, railgunBg);
        context.drawBorder(x + 62, y + 17, 52, 11, railgunBorder);
        context.drawText(this.textRenderer, "💥 RAILGUN", x + 64, y + 19, railgunSelected ? 0xFFFFFFFF : 0xFF94A3B8, false);

        // Tab 3: DUPER (x: 118..168, w: 50)
        int duperBg = duperSelected ? 0xFFB45309 : 0xFF0F172A;
        int duperBorder = duperSelected ? 0xFFFBBF24 : 0xFF334155;
        context.fill(x + 118, y + 17, x + 168, y + 28, duperBg);
        context.drawBorder(x + 118, y + 17, 50, 11, duperBorder);
        context.drawText(this.textRenderer, "💠 DUPER", x + 122, y + 19, duperSelected ? 0xFFFFFFFF : 0xFF94A3B8, false);

        // 4. Energy Storage Gauge Bar (x: 10, y: 34, w: 14, h: 36)
        int energy = this.handler.getEnergy();
        int maxEnergy = this.handler.getMaxEnergy();
        float energyRatio = Math.min(1.0F, (float) energy / (float) Math.max(1, maxEnergy));
        int energyFillHeight = (int) (34 * energyRatio);

        context.fill(x + 10, y + 34, x + 24, y + 70, 0xFF080D1A);
        context.drawBorder(x + 10, y + 34, 14, 36, 0xFF00E5FF);

        // Energy tick marks
        context.fill(x + 11, y + 43, x + 14, y + 44, 0x5500E5FF);
        context.fill(x + 11, y + 52, x + 14, y + 53, 0x5500E5FF);
        context.fill(x + 11, y + 61, x + 14, y + 62, 0x5500E5FF);

        if (energyFillHeight > 0) {
            context.fill(x + 11, y + 69 - energyFillHeight, x + 23, y + 69, 0xFF00E5FF);
        }

        // 5. Input Slots (3 columns x 2 rows at x=44,62,80, y=34,52)
        for (int i = 0; i < 6; i++) {
            int sx = x + SLOT_XS[i];
            int sy = y + SLOT_YS[i];
            Slot slot = this.handler.slots.get(i);
            ItemStack req = getRequiredStackForSlot(recipe, i);

            // Slot base background
            context.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF0E1726);

            int slotBorder;
            if (req.isEmpty()) {
                slotBorder = 0xFF1E293B; // Unused slot for this blueprint
            } else if (!slot.hasStack()) {
                slotBorder = themeBorder; // Empty required slot
            } else {
                ItemStack current = slot.getStack();
                if (current.isOf(req.getItem())) {
                    slotBorder = (current.getCount() >= req.getCount()) ? 0xFF22C55E : 0xFFF59E0B;
                } else {
                    slotBorder = 0xFFEF4444; // Wrong item inserted
                }
            }
            context.drawBorder(sx - 1, sy - 1, 18, 18, slotBorder);

            // Render Ghost Item in empty required slot
            if (!slot.hasStack() && !req.isEmpty()) {
                context.drawItem(req, sx, sy);
                RenderSystem.enableBlend();
                context.fill(sx, sy, sx + 16, sy + 16, 0x70000000);
                String countStr = String.valueOf(req.getCount());
                context.drawTextWithShadow(this.textRenderer, countStr, sx + 16 - this.textRenderer.getWidth(countStr), sy + 8, 0xFFE2E8F0);
            } else if (slot.hasStack() && !req.isEmpty()) {
                ItemStack current = slot.getStack();
                if (current.isOf(req.getItem())) {
                    if (current.getCount() >= req.getCount()) {
                        // Green Check Dot at top-left
                        context.fill(sx + 1, sy + 1, sx + 3, sy + 3, 0xFF22C55E);
                    } else {
                        // Red missing amount indicator
                        String countStr = current.getCount() + "/" + req.getCount();
                        context.drawTextWithShadow(this.textRenderer, countStr, sx + 16 - this.textRenderer.getWidth(countStr), sy + 8, 0xFFF87171);
                    }
                }
            }
        }

        // 6. Progress Arrow Bar (x: 102, y: 43, w: 26, h: 18)
        int progress = this.handler.getProgressTicks();
        int maxProgress = this.handler.getTotalTicks();
        boolean isActive = this.handler.isActive();
        float progressRatio = (maxProgress > 0) ? Math.min(1.0F, (float) progress / (float) maxProgress) : 0.0F;
        int progressFillWidth = (int) (24 * progressRatio);

        context.fill(x + 102, y + 43, x + 128, y + 61, 0xFF080D1A);
        context.drawBorder(x + 102, y + 43, 26, 18, themeBorder);

        if (progressFillWidth > 0) {
            context.fill(x + 103, y + 44, x + 103 + progressFillWidth, y + 60, themeFill);
        }
        // Center chevron
        context.drawText(this.textRenderer, "▶", x + 112, y + 48, isActive ? 0xFFFFFFFF : 0xFF475569, false);

        // 7. Output Slot Frame (Slot 6 at x: 134, y: 43)
        Slot outSlot = this.handler.slots.get(6);
        context.fill(x + 133, y + 42, x + 151, y + 60, 0xFF1C142E);
        context.drawBorder(x + 133, y + 42, 18, 18, 0xFFFFD700);

        // Render Ghost Output if empty
        if (!outSlot.hasStack()) {
            ItemStack ghostOut = getGhostOutput(recipe);
            if (!ghostOut.isEmpty()) {
                context.drawItem(ghostOut, x + 134, y + 43);
                RenderSystem.enableBlend();
                context.fill(x + 134, y + 43, x + 150, y + 59, 0x77000000);
            }
        }

        // 8. Recipe Status / Requirements Panel Card (x: 8..168, y: 73..97, w: 160, h: 24)
        context.fill(x + 8, y + 73, x + 168, y + 97, 0xCC090E1A);
        context.drawBorder(x + 8, y + 73, 160, 24, themeBorder);

        if (isActive) {
            int pct = (int) (progressRatio * 100);
            int remTicks = Math.max(0, maxProgress - progress);
            int remSec = remTicks / 20;
            String itemName = zapperSelected ? "Electronic Zapper" : (railgunSelected ? "Hypervelocity Railgun" : "Electronic Duper");
            context.drawText(this.textRenderer, "🌀 Fabricating: " + pct + "% (" + remSec + "s)", x + 12, y + 76, 0xFFFFD700, false);
            context.drawText(this.textRenderer, itemName + " assembly active...", x + 12, y + 86, 0xFF67E8F9, false);
        } else {
            if (zapperSelected) {
                context.drawText(this.textRenderer, "⚡ Zapper [500 EU | 5.0s]", x + 12, y + 76, 0xFF38BDF8, false);
            } else if (railgunSelected) {
                context.drawText(this.textRenderer, "💥 Railgun [2500 EU | 12.5s]", x + 12, y + 76, 0xFFC084FC, false);
            } else {
                context.drawText(this.textRenderer, "💠 Duper [3000 EU | 15.0s]", x + 12, y + 76, 0xFFFCD34D, false);
            }

            // Check if all input slots are currently ready
            boolean allSatisfied = isRecipeSatisfied(recipe);
            if (allSatisfied) {
                context.drawText(this.textRenderer, "✔ Parts Ready! Machine will auto-craft", x + 12, y + 86, 0xFF22C55E, false);
            } else {
                context.drawText(this.textRenderer, "Place required parts in highlighted slots", x + 12, y + 86, 0xFF94A3B8, false);
            }
        }

        // 9. Player Inventory Background Slots (y = 112, 3 rows x 9 columns)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                context.fill(x + 7 + j * 18, y + 111 + i * 18, x + 23 + j * 18, y + 127 + i * 18, 0xFF0E1626);
                context.drawBorder(x + 7 + j * 18, y + 111 + i * 18, 16, 16, 0xFF1C2C45);
            }
        }

        // 10. Player Hotbar Background Slots (y = 170, 1 row x 9 columns)
        for (int i = 0; i < 9; ++i) {
            context.fill(x + 7 + i * 18, y + 169, x + 23 + i * 18, y + 185, 0xFF0E1626);
            context.drawBorder(x + 7 + i * 18, y + 169, 16, 16, 0xFF00E5FF);
        }
    }

    private boolean isRecipeSatisfied(int recipe) {
        for (int i = 0; i < 6; i++) {
            ItemStack req = getRequiredStackForSlot(recipe, i);
            if (!req.isEmpty()) {
                Slot s = this.handler.slots.get(i);
                if (!s.hasStack() || !s.getStack().isOf(req.getItem()) || s.getStack().getCount() < req.getCount()) {
                    return false;
                }
            }
        }
        return true;
    }

    private ItemStack getRequiredStackForSlot(int recipe, int slotIndex) {
        if (recipe == ItemFabricatorBlockEntity.RECIPE_ZAPPER) {
            return switch (slotIndex) {
                case 0 -> new ItemStack(EvecualMC.ELACTORITE, 1);
                case 1 -> new ItemStack(EvecualMC.STEEL_ROD, 2);
                case 2 -> new ItemStack(EvecualMC.BATTERY_ITEM, 1);
                case 3 -> new ItemStack(EvecualMC.WIRE_ITEM, 2);
                default -> ItemStack.EMPTY;
            };
        } else if (recipe == ItemFabricatorBlockEntity.RECIPE_RAILGUN) {
            return switch (slotIndex) {
                case 0 -> new ItemStack(Items.NETHERITE_INGOT, 2);
                case 1 -> new ItemStack(EvecualMC.ELACTORITE, 4);
                case 2 -> new ItemStack(EvecualMC.STEEL_ROD, 4);
                case 3 -> new ItemStack(EvecualMC.UPGRADED_ENGINE, 1);
                case 4 -> new ItemStack(EvecualMC.COPPER_PLATE, 4);
                case 5 -> new ItemStack(EvecualMC.WIRE_ITEM, 4);
                default -> ItemStack.EMPTY;
            };
        } else if (recipe == ItemFabricatorBlockEntity.RECIPE_DUPER) {
            return switch (slotIndex) {
                case 0 -> new ItemStack(EvecualMC.MATERIALIZER_ITEM, 1);
                case 1 -> new ItemStack(Items.NETHERITE_INGOT, 1);
                case 2 -> new ItemStack(EvecualMC.ELACTORITE, 4);
                case 3 -> new ItemStack(EvecualMC.UPGRADED_ENGINE, 1);
                case 4 -> new ItemStack(Items.DIAMOND_BLOCK, 1);
                case 5 -> new ItemStack(EvecualMC.WIRE_ITEM, 8);
                default -> ItemStack.EMPTY;
            };
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getGhostOutput(int recipe) {
        return switch (recipe) {
            case ItemFabricatorBlockEntity.RECIPE_ZAPPER -> new ItemStack(EvecualMC.ELECTRONIC_ZAPPER);
            case ItemFabricatorBlockEntity.RECIPE_RAILGUN -> new ItemStack(EvecualMC.RAILGUN_ITEM);
            case ItemFabricatorBlockEntity.RECIPE_DUPER -> new ItemStack(EvecualMC.ELECTRONIC_DUPER_ITEM);
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        int relX = (int) mouseX - x;
        int relY = (int) mouseY - y;

        if (relY >= 17 && relY <= 28) {
            // Check Button 1: ZAPPER (x: 8..58)
            if (relX >= 8 && relX <= 58) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, ItemFabricatorBlockEntity.RECIPE_ZAPPER);
                    return true;
                }
            }
            // Check Button 2: RAILGUN (x: 62..114)
            if (relX >= 62 && relX <= 114) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, ItemFabricatorBlockEntity.RECIPE_RAILGUN);
                    return true;
                }
            }
            // Check Button 3: DUPER (x: 118..168)
            if (relX >= 118 && relX <= 168) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, ItemFabricatorBlockEntity.RECIPE_DUPER);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        int relX = mouseX - x;
        int relY = mouseY - y;

        int recipe = this.handler.getSelectedRecipe();

        // Energy Gauge Tooltip
        if (relX >= 10 && relX <= 24 && relY >= 34 && relY <= 70) {
            int energy = this.handler.getEnergy();
            int max = this.handler.getMaxEnergy();
            int needed = (recipe == ItemFabricatorBlockEntity.RECIPE_DUPER) ? 3000 : ((recipe == ItemFabricatorBlockEntity.RECIPE_RAILGUN) ? 2500 : 500);
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal("§b⚡ Stored Energy: §f" + energy + " / " + max + " EU"));
            lines.add(Text.literal("§7Fabrication Cost: §e" + needed + " EU"));
            lines.add(Text.literal("§8Accepts power from Cable / Generator / Battery"));
            context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
            return;
        }

        // Tab Tooltips
        if (relY >= 17 && relY <= 28) {
            if (relX >= 8 && relX <= 58) {
                List<Text> lines = new ArrayList<>();
                lines.add(Text.literal("§b⚡ Electronic Zapper Blueprint"));
                lines.add(Text.literal("§7Required Parts:"));
                lines.add(Text.literal(" §f• 1x Elactorite"));
                lines.add(Text.literal(" §f• 2x Steel Rod"));
                lines.add(Text.literal(" §f• 1x Small Battery"));
                lines.add(Text.literal(" §f• 2x Insulated Wire"));
                lines.add(Text.literal("§eEnergy: 500 EU §7| §6Time: 5.0s"));
                context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
                return;
            } else if (relX >= 62 && relX <= 114) {
                List<Text> lines = new ArrayList<>();
                lines.add(Text.literal("§d💥 Hypervelocity Railgun Blueprint"));
                lines.add(Text.literal("§7Required Parts:"));
                lines.add(Text.literal(" §f• 2x Netherite Ingot"));
                lines.add(Text.literal(" §f• 4x Elactorite"));
                lines.add(Text.literal(" §f• 4x Steel Rod"));
                lines.add(Text.literal(" §f• 1x Upgraded Engine"));
                lines.add(Text.literal(" §f• 4x Copper Plate"));
                lines.add(Text.literal(" §f• 4x Insulated Wire"));
                lines.add(Text.literal("§eEnergy: 2500 EU §7| §6Time: 12.5s"));
                context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
                return;
            } else if (relX >= 118 && relX <= 168) {
                List<Text> lines = new ArrayList<>();
                lines.add(Text.literal("§6💠 Electronic Duper Blueprint"));
                lines.add(Text.literal("§7Required Parts:"));
                lines.add(Text.literal(" §f• 1x Quantum Materializer"));
                lines.add(Text.literal(" §f• 1x Netherite Ingot"));
                lines.add(Text.literal(" §f• 4x Elactorite"));
                lines.add(Text.literal(" §f• 1x Upgraded Engine"));
                lines.add(Text.literal(" §f• 1x Diamond Block"));
                lines.add(Text.literal(" §f• 8x Insulated Wire"));
                lines.add(Text.literal("§eEnergy: 3000 EU §7| §6Time: 15.0s"));
                context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
                return;
            }
        }

        // Empty Input Slot Tooltips
        for (int i = 0; i < 6; i++) {
            int sx = SLOT_XS[i];
            int sy = SLOT_YS[i];
            if (relX >= sx && relX <= sx + 16 && relY >= sy && relY <= sy + 16) {
                Slot s = this.handler.slots.get(i);
                if (!s.hasStack()) {
                    ItemStack req = getRequiredStackForSlot(recipe, i);
                    if (!req.isEmpty()) {
                        context.drawTooltip(this.textRenderer,
                                Text.literal("§bSlot " + (i + 1) + " Required: §f" + req.getCount() + "x " + req.getName().getString()),
                                mouseX, mouseY);
                    }
                }
                break;
            }
        }
    }
}
