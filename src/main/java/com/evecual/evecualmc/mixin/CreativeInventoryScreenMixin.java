package com.evecual.evecualmc.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Unique
    private static final Identifier EVECUAL_CROWN_SLOT_TEXTURE = new Identifier("evecualmc", "textures/gui/crown_slot.png");

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void evecual$adjustCreativeCrownSlot(ItemGroup tab, CallbackInfo ci) {
        if (tab.getType() == ItemGroup.Type.INVENTORY) {
            for (Slot slot : this.handler.slots) {
                if (slot.getIndex() == 46) {
                    ((SlotAccessor) slot).setX(35);
                    ((SlotAccessor) slot).setY(2);
                }
            }
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void evecual$drawCreativeCrownSlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        if (screen.isInventoryTabSelected()) {
            context.drawTexture(EVECUAL_CROWN_SLOT_TEXTURE, this.x + 34, this.y + 1, 0, 0, 18, 18, 18, 18);
        }
    }
}
