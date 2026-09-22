package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.screen.StationaryTurretScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StationaryTurretScreen extends HandledScreen<StationaryTurretScreenHandler> {

    private int activeTab = 0; // 0 = General Settings, 1 = Player Whitelist/Blacklist
    private TextFieldWidget playerInput;
    private final Set<String> playerList = new LinkedHashSet<>();
    private String currentFilterMode = "WHITELIST";
    private int playerPage = 0;

    public StationaryTurretScreen(StationaryTurretScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 186;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        if (handler.getBlockEntity() != null) {
            this.playerList.addAll(handler.getBlockEntity().getTargetFilter().getPlayerList());
            this.currentFilterMode = handler.getBlockEntity().getTargetFilter().getFilterMode().name();
        }
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        this.playerInput = new TextFieldWidget(this.textRenderer, x + 8, y + 35, 108, 14, Text.literal("Player Name"));
        this.playerInput.setMaxLength(32);
        this.playerInput.setPlaceholder(Text.literal("§8Player Gametag..."));
        this.playerInput.setVisible(this.activeTab == 1);
        this.addSelectableChild(this.playerInput);
    }

    public void updateFilterData(String mode, List<String> list) {
        this.currentFilterMode = mode;
        this.playerList.clear();
        this.playerList.addAll(list);
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
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF00E5FF);

        // Header Bar
        context.fill(x + 1, y + 1, x + this.backgroundWidth - 1, y + 15, 0xFF142238);
        context.drawText(this.textRenderer, "🛡️ TURRET", x + 6, y + 4, 0xFF00E5FF, false);

        // Tab Navigation Buttons
        drawTabButton(context, x + 72, y + 2, 48, 12, "⚙ Targets", this.activeTab == 0, mouseX, mouseY);
        drawTabButton(context, x + 122, y + 2, 48, 12, "👥 Filter", this.activeTab == 1, mouseX, mouseY);

        if (this.activeTab == 0) {
            this.playerInput.setVisible(false);
            renderGeneralTab(context, x, y, mouseX, mouseY);
        } else {
            this.playerInput.setVisible(true);
            renderFilterTab(context, x, y, mouseX, mouseY);
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

    private void renderGeneralTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        // 1. Area Coverage Button
        int r = this.handler.getRadius();
        int areaSize = r * 2;
        drawModernButton(context, x + 8, y + 18, 160, 14, "🌐 Area: " + areaSize + "x" + areaSize + " (" + r + "m radius)", 0xFF38BDF8, 0xFF0F172A, mouseX, mouseY);

        // 2. Targeting Filter Toggles (2 rows x 2 columns)
        boolean targetPlayers = this.handler.isTargetPlayers();
        String pText = targetPlayers ? "👤 Players: TARGET" : "👤 Players: ALLOWED";
        int pColor = targetPlayers ? 0xFFEF4444 : 0xFF22C55E;
        drawModernButton(context, x + 8, y + 35, 78, 14, pText, pColor, 0xFF0F172A, mouseX, mouseY);

        boolean targetMonsters = this.handler.isTargetMonsters();
        String mText = targetMonsters ? "🧟 Mobs: TARGET" : "🧟 Mobs: IGNORE";
        int mColor = targetMonsters ? 0xFFEF4444 : 0xFF94A3B8;
        drawModernButton(context, x + 90, y + 35, 78, 14, mText, mColor, 0xFF0F172A, mouseX, mouseY);

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
            context.drawText(this.textRenderer, "Copper, Iron, Steel, Diamond Ammo", x + 12, y + 83, 0xFF94A3B8, false);
        } else {
            context.drawText(this.textRenderer, "⚠️ No Ammo Container Linked!", x + 12, y + 73, 0xFFFBBF24, false);
            context.drawText(this.textRenderer, "Right-click container with Linker Tool", x + 12, y + 83, 0xFF94A3B8, false);
        }
    }

    private void renderFilterTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        // Mode toggle button
        boolean isWhitelist = "WHITELIST".equalsIgnoreCase(this.currentFilterMode);
        String modeText = isWhitelist ? "🛡️ Mode: WHITELIST (Friends Safe)" : "🎯 Mode: BLACKLIST (Enemies Targeted)";
        int modeColor = isWhitelist ? 0xFF22C55E : 0xFFEF4444;
        drawModernButton(context, x + 8, y + 18, 160, 14, modeText, modeColor, 0xFF0F172A, mouseX, mouseY);

        // Add Button next to text input
        drawModernButton(context, x + 120, y + 35, 48, 14, "+ Add", 0xFF00E5FF, 0xFF0F172A, mouseX, mouseY);

        // Gametags list container
        context.fill(x + 8, y + 52, x + 168, y + 96, 0xFF080D1A);
        context.drawBorder(x + 8, y + 52, 160, 44, 0xFF1E293B);

        List<String> list = new ArrayList<>(this.playerList);
        int total = list.size();
        int perPage = 3;
        int maxPages = Math.max(1, (int) Math.ceil((double) total / perPage));
        if (this.playerPage >= maxPages) this.playerPage = maxPages - 1;
        if (this.playerPage < 0) this.playerPage = 0;

        if (list.isEmpty()) {
            context.drawText(this.textRenderer, "No players in filter.", x + 14, y + 58, 0xFF64748B, false);
            context.drawText(this.textRenderer, isWhitelist ? "All players subject to target flag." : "No players will be targeted.", x + 14, y + 68, 0xFF475569, false);
        } else {
            int startIdx = this.playerPage * perPage;
            for (int i = 0; i < perPage && (startIdx + i) < list.size(); i++) {
                String name = list.get(startIdx + i);
                int rowY = y + 55 + i * 11;
                context.drawText(this.textRenderer, "• " + name, x + 12, rowY, 0xFFE2E8F0, false);

                // [✕] remove button
                boolean hDel = mouseX >= x + 152 && mouseX <= x + 164 && mouseY >= rowY - 1 && mouseY <= rowY + 9;
                context.drawText(this.textRenderer, "✕", x + 154, rowY, hDel ? 0xFFEF4444 : 0xFF94A3B8, false);
            }

            // Pagination info & Clear
            String pInfo = (this.playerPage + 1) + "/" + maxPages;
            context.drawText(this.textRenderer, pInfo, x + 12, y + 86, 0xFF64748B, false);
            if (maxPages > 1) {
                drawModernButton(context, x + 40, y + 84, 14, 10, "◀", 0xFF38BDF8, 0xFF0F172A, mouseX, mouseY);
                drawModernButton(context, x + 56, y + 84, 14, 10, "▶", 0xFF38BDF8, 0xFF0F172A, mouseX, mouseY);
            }
            drawModernButton(context, x + 114, y + 84, 52, 10, "Clear All", 0xFFEF4444, 0xFF0F172A, mouseX, mouseY);
        }
    }

    private void drawTabButton(DrawContext context, int x, int y, int w, int h, String text, boolean active, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = active ? 0xFF00E5FF : (hovered ? 0xFF1E293B : 0xFF0F172A);
        int tc = active ? 0xFF0A0F1D : (hovered ? 0xFFFFFFFF : 0xFF94A3B8);
        context.fill(x, y, x + w, y + h, bg);
        context.drawBorder(x, y, w, h, active ? 0xFF00E5FF : 0xFF334155);
        int tw = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, x + (w - tw) / 2, y + (h - 8) / 2, tc, false);
    }

    private void drawModernButton(DrawContext context, int x, int y, int w, int h, String text, int textColor, int bgColor, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? 0xFF1E293B : bgColor);
        context.drawBorder(x, y, w, h, hovered ? 0xFF00E5FF : 0xFF334155);
        int tw = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, x + (w - tw) / 2, y + (h - 8) / 2, textColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Tab Switches
        if (mouseX >= x + 72 && mouseX <= x + 120 && mouseY >= y + 2 && mouseY <= y + 14) {
            this.activeTab = 0;
            return true;
        }
        if (mouseX >= x + 122 && mouseX <= x + 170 && mouseY >= y + 2 && mouseY <= y + 14) {
            this.activeTab = 1;
            return true;
        }

        if (this.activeTab == 0) {
            // General Settings Buttons
            // Button 4: Cycle Area Radius
            if (mouseX >= x + 8 && mouseX <= x + 168 && mouseY >= y + 18 && mouseY <= y + 32) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, 4);
                    return true;
                }
            }

            // Button 0: Toggle Target Players
            if (mouseX >= x + 8 && mouseX <= x + 86 && mouseY >= y + 35 && mouseY <= y + 49) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, 0);
                    return true;
                }
            }

            // Button 1: Toggle Target Monsters
            if (mouseX >= x + 90 && mouseX <= x + 168 && mouseY >= y + 35 && mouseY <= y + 49) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, 1);
                    return true;
                }
            }

            // Button 2: Toggle Target Animals
            if (mouseX >= x + 8 && mouseX <= x + 86 && mouseY >= y + 52 && mouseY <= y + 66) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, 2);
                    return true;
                }
            }

            // Button 3: Toggle Target Bosses
            if (mouseX >= x + 90 && mouseX <= x + 168 && mouseY >= y + 52 && mouseY <= y + 66) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, 3);
                    return true;
                }
            }
        } else {
            // Filter Tab Controls
            // Mode Toggle Button
            if (mouseX >= x + 8 && mouseX <= x + 168 && mouseY >= y + 18 && mouseY <= y + 32) {
                if (this.client != null && this.client.interactionManager != null) {
                    this.client.interactionManager.clickButton(this.handler.syncId, 6);
                    boolean isW = "WHITELIST".equalsIgnoreCase(this.currentFilterMode);
                    this.currentFilterMode = isW ? "BLACKLIST" : "WHITELIST";
                    return true;
                }
            }

            // Add Button
            if (mouseX >= x + 120 && mouseX <= x + 168 && mouseY >= y + 35 && mouseY <= y + 49) {
                addPlayerFromInput();
                return true;
            }

            // Page navigation & Clear All
            List<String> list = new ArrayList<>(this.playerList);
            int perPage = 3;
            int maxPages = Math.max(1, (int) Math.ceil((double) list.size() / perPage));

            if (maxPages > 1) {
                if (mouseX >= x + 40 && mouseX <= x + 54 && mouseY >= y + 84 && mouseY <= y + 94) {
                    if (this.playerPage > 0) this.playerPage--;
                    return true;
                }
                if (mouseX >= x + 56 && mouseX <= x + 70 && mouseY >= y + 84 && mouseY <= y + 94) {
                    if (this.playerPage < maxPages - 1) this.playerPage++;
                    return true;
                }
            }

            // Clear All Button
            if (mouseX >= x + 114 && mouseX <= x + 166 && mouseY >= y + 84 && mouseY <= y + 94) {
                sendFilterUpdate(3, "");
                this.playerList.clear();
                return true;
            }

            // Remove button for visible players
            int startIdx = this.playerPage * perPage;
            for (int i = 0; i < perPage && (startIdx + i) < list.size(); i++) {
                int rowY = y + 55 + i * 11;
                if (mouseX >= x + 150 && mouseX <= x + 166 && mouseY >= rowY - 1 && mouseY <= rowY + 9) {
                    String toRemove = list.get(startIdx + i);
                    sendFilterUpdate(1, toRemove);
                    this.playerList.remove(toRemove);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void addPlayerFromInput() {
        if (this.playerInput == null) return;
        String name = this.playerInput.getText().trim();
        if (!name.isEmpty()) {
            sendFilterUpdate(0, name);
            this.playerList.add(name.toLowerCase());
            this.playerInput.setText("");
        }
    }

    private void sendFilterUpdate(int action, String data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(action);
        buf.writeString(data);
        ClientPlayNetworking.send(EvecualMC.TURRET_FILTER_UPDATE_PACKET_ID, buf);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.activeTab == 1 && this.playerInput != null && this.playerInput.isFocused()) {
            if (keyCode == 257 || keyCode == 335) { // ENTER
                addPlayerFromInput();
                return true;
            }
            if (this.playerInput.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Prevent vanilla from drawing default title over our custom headers
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x94A3B8, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        if (this.activeTab == 1 && this.playerInput != null) {
            this.playerInput.render(context, mouseX, mouseY, delta);
        }
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
