package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.screen.CrownSlot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique
    private static final Identifier EVECUAL_CROWN_SLOT_TEXTURE = new Identifier("evecualmc", "textures/gui/crown_slot.png");

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void evecual$drawCrownSlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        context.drawTexture(EVECUAL_CROWN_SLOT_TEXTURE, this.x + 76, this.y + 7, 0, 0, 18, 18, 18, 18);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void evecual$drawCrownSlotTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.focusedSlot instanceof CrownSlot && !this.focusedSlot.hasStack()) {
            context.drawTooltip(this.textRenderer, Text.literal("§6👑 Crown Accessory Slot"), mouseX, mouseY);
        }
    }
}
