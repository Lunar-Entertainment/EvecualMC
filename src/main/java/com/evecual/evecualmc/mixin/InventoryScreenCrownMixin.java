package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.util.CrownHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenCrownMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique
    private int evecualmc$crownHoverTimer = 0;
    @Unique
    private boolean evecualmc$isRevealed = false;

    public InventoryScreenCrownMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void checkCrownSlotHover(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        // Check hover zones:
        // 1. Helmet slot (head armor slot): [x + 7 .. x + 25, y + 7 .. y + 25]
        boolean overHelmetSlot = mouseX >= this.x + 7 && mouseX <= this.x + 25 && mouseY >= this.y + 7 && mouseY <= this.y + 25;
        // 2. Crown slot & vertical connecting tab: [x + 6 .. x + 26, y - 22 .. y + 7]
        boolean overCrownSlotTab = mouseX >= this.x + 6 && mouseX <= this.x + 26 && mouseY >= this.y - 22 && mouseY <= this.y + 7;
        // 3. Player preview head area: [x + 40 .. x + 62, y + 8 .. y + 32]
        boolean overPlayerHead = mouseX >= this.x + 40 && mouseX <= this.x + 62 && mouseY >= this.y + 8 && mouseY <= this.y + 32;
        // 4. Cursor currently carrying/dragging a crown
        boolean holdingCrown = CrownHelper.isCrown(this.handler.getCursorStack());

        if (overHelmetSlot || overCrownSlotTab || overPlayerHead || holdingCrown) {
            this.evecualmc$crownHoverTimer = 30; // ~1.5s grace time for smooth cursor movement
            this.evecualmc$isRevealed = true;
        } else {
            if (this.evecualmc$crownHoverTimer > 0) {
                this.evecualmc$crownHoverTimer--;
            }
            this.evecualmc$isRevealed = this.evecualmc$crownHoverTimer > 0;
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void renderCrownSlotTabBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        ItemStack equippedCrown = CrownHelper.getCrown(this.client.player);
        boolean showSlot = this.evecualmc$isRevealed || !equippedCrown.isEmpty();
        if (!showSlot) {
            return;
        }

        int slotX = this.x + 8;
        int slotY = this.y - 19;

        // Draw container tab background
        int tabLeft = this.x + 6;
        int tabTop = this.y - 22;
        int tabRight = this.x + 26;
        int tabBottom = this.y;

        // Base container background
        context.fill(tabLeft, tabTop, tabRight, tabBottom, 0xFFC6C6C6);
        // Beveled 3D highlights (top & left)
        context.fill(tabLeft, tabTop, tabRight, tabTop + 1, 0xFFFFFFFF);
        context.fill(tabLeft, tabTop, tabLeft + 1, tabBottom, 0xFFFFFFFF);
        // Beveled 3D shadows (right)
        context.fill(tabRight - 1, tabTop, tabRight, tabBottom, 0xFF555555);
        // Outer dark frame
        context.fill(tabLeft - 1, tabTop - 1, tabRight + 1, tabTop, 0xFF000000);
        context.fill(tabLeft - 1, tabTop, tabLeft, tabBottom, 0xFF000000);
        context.fill(tabRight, tabTop, tabRight + 1, tabBottom, 0xFF000000);

        // Inset slot cutout [slotX - 1, slotY - 1, 18, 18]
        int sX = slotX - 1;
        int sY = slotY - 1;
        // Slot recessed background
        context.fill(sX + 1, sY + 1, sX + 17, sY + 17, 0xFF8B8B8B);
        // Inset dark shadow (top & left)
        context.fill(sX, sY, sX + 18, sY + 1, 0xFF373737);
        context.fill(sX, sY, sX + 1, sY + 18, 0xFF373737);
        // Inset light highlight (bottom & right)
        context.fill(sX, sY + 17, sX + 18, sY + 18, 0xFFFFFFFF);
        context.fill(sX + 17, sY, sX + 18, sY + 18, 0xFFFFFFFF);

        // When empty, draw golden crown watermark
        if (equippedCrown.isEmpty()) {
            evecualmc$drawCrownWatermark(context, slotX, slotY);
        } else {
            // Render equipped crown item
            context.drawItem(equippedCrown, slotX, slotY);
            context.drawItemInSlot(this.textRenderer, equippedCrown, slotX, slotY);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCrownSlotTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        ItemStack equippedCrown = CrownHelper.getCrown(this.client.player);
        boolean showSlot = this.evecualmc$isRevealed || !equippedCrown.isEmpty();
        if (!showSlot) {
            return;
        }

        int slotX = this.x + 8;
        int slotY = this.y - 19;
        if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
            if (!equippedCrown.isEmpty()) {
                context.drawItemTooltip(this.textRenderer, equippedCrown, mouseX, mouseY);
            } else if (this.handler.getCursorStack().isEmpty()) {
                context.drawTooltip(this.textRenderer, List.of(
                        Text.literal("§6👑 Crown Slot"),
                        Text.literal("§eLegendary Crowns Only"),
                        Text.literal("§8(The Mechanicals, The Electricians, The Castles)")
                ), mouseX, mouseY);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void handleCrownSlotMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        ItemStack equippedCrown = CrownHelper.getCrown(this.client.player);
        boolean showSlot = this.evecualmc$isRevealed || !equippedCrown.isEmpty();
        if (!showSlot) {
            return;
        }

        int slotX = this.x + 8;
        int slotY = this.y - 19;
        if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
            boolean isShift = hasShiftDown();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(isShift);
            buf.writeInt(button);
            ClientPlayNetworking.send(EvecualMC.CROWN_SLOT_CLICK_PACKET_ID, buf);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static void evecualmc$drawCrownWatermark(DrawContext context, int x, int y) {
        int cGold = 0xAAFFD700;
        int cDark = 0x88B8860B;
        int cGem = 0xAA00FFFF;

        // Bottom crown circlet band
        context.fill(x + 2, y + 12, x + 14, y + 14, cGold);
        context.fill(x + 3, y + 13, x + 13, y + 14, cDark);

        // Center spire
        context.fill(x + 7, y + 4, x + 9, y + 12, cGold);
        context.fill(x + 7, y + 3, x + 9, y + 4, cGem);

        // Left spire
        context.fill(x + 3, y + 6, x + 5, y + 12, cGold);
        context.fill(x + 3, y + 5, x + 5, y + 6, cGold);

        // Right spire
        context.fill(x + 11, y + 6, x + 13, y + 12, cGold);
        context.fill(x + 11, y + 5, x + 13, y + 6, cGold);

        // Cross accents
        context.fill(x + 5, y + 9, x + 7, y + 12, cDark);
        context.fill(x + 9, y + 9, x + 11, y + 12, cDark);
    }
}
