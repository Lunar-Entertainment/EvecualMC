package com.evecual.evecualmc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class ModTipScreen extends Screen {
    private static final int SCREEN_WIDTH = 420;
    private static final int SCREEN_HEIGHT = 260;
    private static final int SIDEBAR_WIDTH = 125;
    private static final int ENTRY_HEIGHT = 18;

    private TipTopic currentTopic;
    private int activeTab = 0; // 0: Setup & Overview, 1: Controls, 2: Pro Tips
    private int sidebarScroll = 0;

    // Optional live telemetry if opened from a block or entity
    private Integer liveEnergy = null;
    private Integer liveMaxEnergy = null;
    private String liveStatus = null;

    public ModTipScreen(TipTopic initialTopic) {
        super(Text.literal("EvecualMC Field Guide & Tips"));
        this.currentTopic = initialTopic != null ? initialTopic : TipTopic.SOLAR_PANEL;
    }

    public ModTipScreen(TipTopic initialTopic, Integer energy, Integer maxEnergy, String status) {
        this(initialTopic);
        this.liveEnergy = energy;
        this.liveMaxEnergy = maxEnergy;
        this.liveStatus = status;
    }

    public void setTopic(TipTopic topic) {
        this.currentTopic = topic;
        this.activeTab = 0;
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't freeze world in multiplayer
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        int left = (width - SCREEN_WIDTH) / 2;
        int top = (height - SCREEN_HEIGHT) / 2;
        int right = left + SCREEN_WIDTH;
        int bottom = top + SCREEN_HEIGHT;

        // 1. Dark Glassmorphic Window Background
        context.fill(left, top, right, bottom, 0xFA0A111E); // Deep Slate
        // Golden Amber Glowing Outline Border (Matching the HUD aesthetic)
        context.drawBorder(left, top, SCREEN_WIDTH, SCREEN_HEIGHT, 0xFFF59E0B);

        // 2. Sidebar Header
        context.fill(left, top, left + SIDEBAR_WIDTH, top + 26, 0xFF1E293B);
        context.drawText(textRenderer, Text.literal("📚 TOPICS").formatted(Formatting.BOLD, Formatting.GOLD),
                left + 10, top + 9, 0xFFFDE047, false);
        context.drawBorder(left, top, SIDEBAR_WIDTH, SCREEN_HEIGHT, 0xFF334155);

        // 3. Render Topics List in Sidebar (Scrollable & Scissored to prevent overlap)
        TipTopic[] allTopics = TipTopic.values();
        int visibleEntries = (SCREEN_HEIGHT - 32) / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, allTopics.length - visibleEntries);
        sidebarScroll = MathHelper.clamp(sidebarScroll, 0, maxScroll);

        int listY = top + 28;
        context.enableScissor(left + 1, listY, left + SIDEBAR_WIDTH - 1, bottom - 1);

        for (int i = 0; i < allTopics.length; i++) {
            int entryIndex = i;
            TipTopic t = allTopics[entryIndex];
            int entryY = listY + (entryIndex - sidebarScroll) * ENTRY_HEIGHT;

            if (entryY + ENTRY_HEIGHT < listY || entryY > bottom) continue;

            boolean isHovered = mouseX >= left + 2 && mouseX < left + SIDEBAR_WIDTH - 2 && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
            boolean isSelected = t == currentTopic;

            if (isSelected) {
                context.fill(left + 2, entryY, left + SIDEBAR_WIDTH - 2, entryY + ENTRY_HEIGHT - 1, 0xFFB45309); // Warm Gold Selection
            } else if (isHovered) {
                context.fill(left + 2, entryY, left + SIDEBAR_WIDTH - 2, entryY + ENTRY_HEIGHT - 1, 0x55334155);
            }

            // Small item icon
            ItemStack icon = t.getIcon();
            context.getMatrices().push();
            context.getMatrices().translate(left + 5, entryY + 1, 0);
            context.getMatrices().scale(0.85F, 0.85F, 1.0F);
            context.drawItem(icon, 0, 0);
            context.getMatrices().pop();

            String name = t.getTitle();
            int maxNameWidth = SIDEBAR_WIDTH - 28;
            if (textRenderer.getWidth(name) > maxNameWidth) {
                while (name.length() > 3 && textRenderer.getWidth(name + "..") > maxNameWidth) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name + "..";
            }
            int textColor = isSelected ? 0xFFFFFF : (isHovered ? 0xFDE047 : 0x94A3B8);
            context.drawText(textRenderer, name, left + 22, entryY + 4, textColor, false);
        }
        context.disableScissor();

        // 4. Main Content Header
        int contentLeft = left + SIDEBAR_WIDTH + 10;
        int contentWidth = SCREEN_WIDTH - SIDEBAR_WIDTH - 18;
        int contentTop = top + 8;

        // Big Item Icon
        context.getMatrices().push();
        context.getMatrices().translate(contentLeft, contentTop, 0);
        context.getMatrices().scale(1.5F, 1.5F, 1.0F);
        context.drawItem(currentTopic.getIcon(), 0, 0);
        context.getMatrices().pop();

        // Title and Category Badge
        context.drawText(textRenderer, Text.literal(currentTopic.getTitle()).formatted(Formatting.BOLD, Formatting.GOLD),
                contentLeft + 28, contentTop + 1, 0xFFFDE047, false);
        context.drawText(textRenderer, Text.literal(currentTopic.getCategory()).formatted(Formatting.AQUA),
                contentLeft + 28, contentTop + 13, 0x38BDF8, false);

        // Close Button 'X'
        boolean closeHovered = mouseX >= right - 18 && mouseX <= right - 6 && mouseY >= top + 6 && mouseY <= top + 18;
        context.fill(right - 18, top + 6, right - 6, top + 18, closeHovered ? 0xFFDC2626 : 0xFF334155);
        context.drawText(textRenderer, "✕", right - 15, top + 8, 0xFFFFFF, false);

        // 5. Optional Live Diagnostics Bar (if open on active block/entity)
        int tabY = contentTop + 28;
        if (liveEnergy != null && liveMaxEnergy != null && liveMaxEnergy > 0) {
            int barY = contentTop + 27;
            int barWidth = contentWidth - 4;
            context.fill(contentLeft, barY, contentLeft + barWidth, barY + 12, 0xFF1E293B);
            double pct = MathHelper.clamp((double) liveEnergy / liveMaxEnergy, 0.0, 1.0);
            int fillW = (int) (barWidth * pct);
            context.fill(contentLeft, barY, contentLeft + fillW, barY + 12, 0xFF10B981); // Emerald Green
            context.drawBorder(contentLeft, barY, barWidth, 12, 0xFF475569);

            String statusStr = liveStatus != null ? liveStatus : (liveEnergy + " / " + liveMaxEnergy + " EU (" + (int)(pct * 100) + "%)");
            int textW = textRenderer.getWidth(statusStr);
            context.drawText(textRenderer, statusStr, contentLeft + (barWidth - textW) / 2, barY + 2, 0xFFFFFF, true);
            tabY = barY + 16;
        }

        // 6. Navigation Tabs
        String[] tabs = {"📋 Setup & Usage", "🎮 Key Controls", "💡 Pro Tips"};
        int tabW = (contentWidth - 6) / 3;
        for (int i = 0; i < 3; i++) {
            int tx = contentLeft + i * (tabW + 3);
            boolean isTabSelected = (i == activeTab);
            boolean isTabHovered = mouseX >= tx && mouseX < tx + tabW && mouseY >= tabY && mouseY < tabY + 16;

            int bg = isTabSelected ? 0xFFB45309 : (isTabHovered ? 0xFF334155 : 0xFF1E293B);
            context.fill(tx, tabY, tx + tabW, tabY + 16, bg);
            context.drawBorder(tx, tabY, tabW, 16, isTabSelected ? 0xFFF59E0B : 0xFF475569);

            int tw = textRenderer.getWidth(tabs[i]);
            context.drawText(textRenderer, tabs[i], tx + (tabW - tw) / 2, tabY + 4, isTabSelected ? 0xFFFFFF : 0x94A3B8, false);
        }

        // 7. Tab Content Area
        int bodyY = tabY + 19;
        int bodyHeight = bottom - bodyY - 16;
        context.fill(contentLeft, bodyY, contentLeft + contentWidth, bodyY + bodyHeight, 0xFF0B132B);
        context.drawBorder(contentLeft, bodyY, contentWidth, bodyHeight, 0xFF1E293B);

        int textX = contentLeft + 8;
        int currentY = bodyY + 6;
        int maxBodyRight = contentLeft + contentWidth - 8;

        context.enableScissor(contentLeft + 1, bodyY + 1, contentLeft + contentWidth - 1, bodyY + bodyHeight - 1);

        if (activeTab == 0) {
            // Overview & Setup
            context.drawText(textRenderer, Text.literal("Overview:").formatted(Formatting.BOLD, Formatting.GOLD), textX, currentY, 0xFDE047, false);
            currentY += 11;
            for (String line : wrapLines(currentTopic.getOverview(), contentWidth - 16)) {
                context.drawText(textRenderer, line, textX, currentY, 0xCBD5E1, false);
                currentY += 10;
            }
            currentY += 6;

            context.drawText(textRenderer, Text.literal("Setup & Operation:").formatted(Formatting.BOLD, Formatting.AQUA), textX, currentY, 0x38BDF8, false);
            currentY += 11;
            for (String step : currentTopic.getSetupSteps()) {
                if (currentY + 10 > bodyY + bodyHeight) break;
                java.util.List<String> wrappedStep = wrapLines("• " + step, contentWidth - 16);
                for (int si = 0; si < wrappedStep.size(); si++) {
                    if (currentY + 10 > bodyY + bodyHeight) break;
                    String stepLine = (si > 0 ? "  " : "") + wrappedStep.get(si);
                    context.drawText(textRenderer, stepLine, textX, currentY, 0xE2E8F0, false);
                    currentY += 10;
                }
            }
        } else if (activeTab == 1) {
            // Controls & Keybindings
            context.drawText(textRenderer, Text.literal("Key Controls & Operation:").formatted(Formatting.BOLD, Formatting.GOLD), textX, currentY, 0xFDE047, false);
            currentY += 13;

            for (String[] ctrl : currentTopic.getControls()) {
                if (currentY + 12 > bodyY + bodyHeight) break;
                // Key Badge
                String keyBadge = "[" + ctrl[0] + "]";
                int badgeWidth = textRenderer.getWidth(keyBadge) + 4;
                context.fill(textX, currentY - 1, textX + badgeWidth, currentY + 9, 0xFF334155);
                context.drawBorder(textX, currentY - 1, badgeWidth, 10, 0xFF64748B);
                context.drawText(textRenderer, keyBadge, textX + 2, currentY, 0x38BDF8, false);

                // Multi-line word wrapped description for long control texts
                int descX = textX + badgeWidth + 6;
                int descWidth = maxBodyRight - descX;
                java.util.List<String> descLines = wrapLines(ctrl[1], descWidth);
                for (int di = 0; di < descLines.size(); di++) {
                    if (currentY + 10 > bodyY + bodyHeight) break;
                    context.drawText(textRenderer, descLines.get(di), descX, currentY, 0xE2E8F0, false);
                    currentY += 11;
                }
                currentY += 1;
            }
        } else if (activeTab == 2) {
            // Pro Tips
            context.drawText(textRenderer, Text.literal("Field Guide & Pro Tips:").formatted(Formatting.BOLD, Formatting.YELLOW), textX, currentY, 0xFDE047, false);
            currentY += 13;

            for (String tip : currentTopic.getProTips()) {
                if (currentY + 15 > bodyY + bodyHeight) break;
                context.drawText(textRenderer, "⭐", textX, currentY, 0xFBBF24, false);
                for (String line : wrapLines(tip, contentWidth - 28)) {
                    if (currentY + 10 > bodyY + bodyHeight) break;
                    context.drawText(textRenderer, line, textX + 14, currentY, 0xE2E8F0, false);
                    currentY += 10;
                }
                currentY += 4;
            }
        }
        context.disableScissor();

        // 8. Bottom Hint
        String hint = "💡 Tip: Press 'H' to inspect anything in crosshair | Scroll wheel to browse";
        context.drawText(textRenderer, hint, left + 10, bottom - 11, 0x64748B, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private java.util.List<String> wrapLines(String text, int maxPixelWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            if (textRenderer.getWidth(test) <= maxPixelWidth) {
                current = new StringBuilder(test);
            } else {
                if (current.length() > 0) lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int left = (width - SCREEN_WIDTH) / 2;
        if (mouseX >= left && mouseX <= left + SIDEBAR_WIDTH) {
            TipTopic[] allTopics = TipTopic.values();
            int visibleEntries = (SCREEN_HEIGHT - 32) / ENTRY_HEIGHT;
            int maxScroll = Math.max(0, allTopics.length - visibleEntries);
            if (amount > 0) {
                sidebarScroll = Math.max(0, sidebarScroll - 1);
            } else if (amount < 0) {
                sidebarScroll = Math.min(maxScroll, sidebarScroll + 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // LMB
            int left = (width - SCREEN_WIDTH) / 2;
            int top = (height - SCREEN_HEIGHT) / 2;
            int right = left + SCREEN_WIDTH;

            // Close Button
            if (mouseX >= right - 18 && mouseX <= right - 6 && mouseY >= top + 6 && mouseY <= top + 18) {
                close();
                return true;
            }

            // Click Topic in Sidebar
            TipTopic[] allTopics = TipTopic.values();
            int listY = top + 28;

            for (int i = 0; i < allTopics.length; i++) {
                int entryY = listY + (i - sidebarScroll) * ENTRY_HEIGHT;
                if (mouseX >= left + 2 && mouseX < left + SIDEBAR_WIDTH - 2 && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT) {
                    setTopic(allTopics[i]);
                    // Clear live data when browsing other topics
                    this.liveEnergy = null;
                    this.liveMaxEnergy = null;
                    this.liveStatus = null;
                    MinecraftClient.getInstance().getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.master(
                                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    return true;
                }
            }

            // Click Content Tabs
            int contentLeft = left + SIDEBAR_WIDTH + 10;
            int contentWidth = SCREEN_WIDTH - SIDEBAR_WIDTH - 18;
            int tabY = top + 8 + 28;
            if (liveEnergy != null && liveMaxEnergy != null && liveMaxEnergy > 0) {
                tabY += 16;
            }
            int tabW = (contentWidth - 6) / 3;

            for (int i = 0; i < 3; i++) {
                int tx = contentLeft + i * (tabW + 3);
                if (mouseX >= tx && mouseX < tx + tabW && mouseY >= tabY && mouseY < tabY + 16) {
                    this.activeTab = i;
                    MinecraftClient.getInstance().getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.master(
                                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_H || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
