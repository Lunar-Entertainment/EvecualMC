package com.evecual.evecualmc.util;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.item.crown.CastlesCrownItem;
import com.evecual.evecualmc.item.crown.ElectriciansCrownItem;
import com.evecual.evecualmc.item.crown.MechanicalsCrownItem;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class CrownHelper {

    /**
     * Checks if an item stack is one of the three legendary crowns.
     * Absolutely nothing else is permitted in the crown slot.
     */
    public static boolean isCrown(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.isOf(EvecualMC.THE_MECHANICALS_CROWN)
                || stack.isOf(EvecualMC.THE_ELECTRICIANS_CROWN)
                || stack.isOf(EvecualMC.THE_CASTLES_CROWN)
                || stack.getItem() instanceof MechanicalsCrownItem
                || stack.getItem() instanceof ElectriciansCrownItem
                || stack.getItem() instanceof CastlesCrownItem;
    }

    /**
     * Retrieves the crown currently equipped in the dedicated crown slot.
     */
    public static ItemStack getCrown(PlayerEntity player) {
        if (player instanceof CrownHolder holder) {
            return holder.evecualmc$getCrown();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Sets the crown in the dedicated crown slot.
     */
    public static void setCrown(PlayerEntity player, ItemStack stack) {
        if (player instanceof CrownHolder holder) {
            holder.evecualmc$setCrown(stack);
        }
    }

    /**
     * Checks if this specific stack is currently equipped either on the head armor slot
     * or in the dedicated crown slot.
     */
    public static boolean isEquipped(PlayerEntity player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (player.getEquippedStack(EquipmentSlot.HEAD) == stack) {
            return true;
        }
        if (player instanceof CrownHolder holder) {
            ItemStack crown = holder.evecualmc$getCrown();
            return crown == stack;
        }
        return false;
    }

    /**
     * Syncs the equipped crown to the player and all players tracking them.
     */
    public static void syncCrownToTracking(ServerPlayerEntity player) {
        ItemStack crown = getCrown(player);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(player.getId());
        buf.writeItemStack(crown);

        // Send to player themselves
        ServerPlayNetworking.send(player, EvecualMC.CROWN_SYNC_S2C_PACKET_ID, buf);

        // Send to all players tracking this player
        for (ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
            PacketByteBuf trackBuf = PacketByteBufs.create();
            trackBuf.writeInt(player.getId());
            trackBuf.writeItemStack(crown);
            ServerPlayNetworking.send(tracker, EvecualMC.CROWN_SYNC_S2C_PACKET_ID, trackBuf);
        }
    }
}
