package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.screen.CrownSlot;
import com.evecual.evecualmc.util.CrownHelper;
import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
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
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingInventory> {

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addCrownSlotToPlayerScreen(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        if (owner instanceof CrownHolder holder) {
            // Positioned at x=8, y=-19: directly above the head armor slot (x=8, y=8)
            this.addSlot(new CrownSlot(holder.evecualmc$getCrownInventory(), 0, 8, -19, owner));
        }
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void handleCrownQuickMove(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size()) {
            return;
        }

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return;
        }

        ItemStack slotStack = slot.getStack();

        // 1. Shift-clicking out of the Crown Slot (transfer back to main inventory/hotbar)
        if (slot instanceof CrownSlot) {
            ItemStack copy = slotStack.copy();
            if (!this.insertItem(slotStack, 9, 45, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            cir.setReturnValue(copy);
            return;
        }

        // 2. Shift-clicking a Crown from main inventory/hotbar into the Crown Slot
        if (CrownHelper.isCrown(slotStack)) {
            for (Slot s : this.slots) {
                if (s instanceof CrownSlot crownSlot) {
                    if (!crownSlot.hasStack() && crownSlot.canInsert(slotStack)) {
                        ItemStack copy = slotStack.copy();
                        crownSlot.setStack(slotStack.split(1));
                        crownSlot.markDirty();
                        if (slotStack.isEmpty()) {
                            slot.setStack(ItemStack.EMPTY);
                        } else {
                            slot.markDirty();
                        }
                        cir.setReturnValue(copy);
                        return;
                    }
                }
            }
        }
    }
}
