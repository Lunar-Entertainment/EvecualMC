package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.screen.TurretAmmoContainerScreenHandler;
import com.evecual.evecualmc.turret.AmmoType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TurretAmmoContainerBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    public static final int INVENTORY_SIZE = 27;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final Set<BlockPos> linkedStationaryTurrets = new HashSet<>();
    public TurretAmmoContainerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.TURRET_AMMO_CONTAINER_BLOCK_ENTITY, pos, state);
    }

    public synchronized void linkStationaryTurret(BlockPos turretPos) {
        linkedStationaryTurrets.add(turretPos);
        markDirty();
        sync();
    }

    public synchronized void unlinkStationaryTurret(BlockPos turretPos) {
        linkedStationaryTurrets.remove(turretPos);
        markDirty();
        sync();
    }

    public Set<BlockPos> getLinkedStationaryTurrets() {
        return linkedStationaryTurrets;
    }

    public synchronized boolean hasAmmo() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && AmmoType.fromStack(stack) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public synchronized AmmoType consumeBestAmmo() {
        // Preference: Diamond -> Iron -> Copper
        for (AmmoType priority : new AmmoType[]{AmmoType.DIAMOND, AmmoType.IRON, AmmoType.COPPER}) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty() && AmmoType.fromStack(stack) == priority) {
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        inventory.set(i, ItemStack.EMPTY);
                    }
                    markDirty();
                    sync();
                    return priority;
                }
            }
        }
        return null;
    }

    public synchronized int getAmmoCount(AmmoType type) {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && AmmoType.fromStack(stack) == type) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("📦 Turret Ammo Container");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TurretAmmoContainerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(inventory, slot);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] slots = new int[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return AmmoType.fromStack(stack) != null;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);

        linkedStationaryTurrets.clear();
        if (nbt.contains("LinkedStationary", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("LinkedStationary", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                linkedStationaryTurrets.add(NbtHelper.toBlockPos(list.getCompound(i)));
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);

        NbtList stationaryList = new NbtList();
        for (BlockPos p : linkedStationaryTurrets) {
            stationaryList.add(NbtHelper.fromBlockPos(p));
        }
        nbt.put("LinkedStationary", stationaryList);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
