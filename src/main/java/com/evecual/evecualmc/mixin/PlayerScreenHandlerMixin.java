package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.item.CrownItem;
import com.evecual.evecualmc.screen.CrownSlot;
import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<net.minecraft.inventory.RecipeInputInventory> {
    public PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void evecual$addCrownSlot(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        if (owner instanceof CrownHolder holder) {
            this.addSlot(new CrownSlot(holder.evecual$getCrownInventory(), 0, 77, 8, owner));
        }
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void evecual$quickMoveCrown(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size()) return;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            if (index == 46) {
                // Crown slot -> Move into main inventory or hotbar (slots 9..45)
                ItemStack copy = stack.copy();
                if (!this.insertItem(stack, 9, 45, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                if (stack.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    slot.markDirty();
                }
                cir.setReturnValue(copy);
            } else if (index >= 9 && index < 45 && stack.getItem() instanceof CrownItem) {
                // Inventory -> Try inserting into Crown slot (slot 46)
                if (this.slots.size() > 46) {
                    Slot crownSlot = this.slots.get(46);
                    if (crownSlot != null && !crownSlot.hasStack()) {
                        ItemStack copy = stack.copy();
                        if (this.insertItem(stack, 46, 47, false)) {
                            if (stack.isEmpty()) {
                                slot.setStack(ItemStack.EMPTY);
                            } else {
                                slot.markDirty();
                            }
                            crownSlot.markDirty();
                            cir.setReturnValue(copy);
                        }
                    }
                }
            }
        }
    }
}
