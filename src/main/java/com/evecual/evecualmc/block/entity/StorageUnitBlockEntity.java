package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.StorageUnitBlock;
import com.evecual.evecualmc.screen.StorageUnitScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StorageUnitBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory, com.evecual.evecualmc.energy.EnergyStorage {
    public static final int SLOTS_PER_UNIT = 108;
    public static final int SHULKER_CHARGE_THRESHOLD = 200;
    public static final int MAX_ENERGY = 10000;

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOTS_PER_UNIT, ItemStack.EMPTY);
    private int energy = 0;
    private boolean lockedDueToPower = false;
    private int totalItemCount = 0;
    private int filledSlotCount = 0;

    public StorageUnitBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.STORAGE_UNIT_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StorageUnitBlockEntity be) {
        if (world.isClient) return;

        // 1. Draw energy from adjacent power sources
        be.drawAdjacentEnergy(world, pos);

        // 2. Unlock if locked and sufficient power received
        if (be.lockedDueToPower && be.energy >= SHULKER_CHARGE_THRESHOLD) {
            be.energy -= SHULKER_CHARGE_THRESHOLD;
            be.lockedDueToPower = false;
            be.markDirty();
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.7F, 1.8F);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.05);
            }
        }

        // Update powered blockstate
        boolean isPowered = (be.energy > 0);
        if (state.get(StorageUnitBlock.POWERED) != isPowered) {
            world.setBlockState(pos, state.with(StorageUnitBlock.POWERED, isPowered), 3);
        }
    }

    private void drawAdjacentEnergy(World world, BlockPos pos) {
        if (this.energy >= MAX_ENERGY) return;

        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> wireQueue = new java.util.LinkedList<>();
        visited.add(pos);

        // 1. Direct adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            if (!visited.add(neighborPos)) continue;

            BlockState neighborState = world.getBlockState(neighborPos);
            BlockEntity neighbor = world.getBlockEntity(neighborPos);

            if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                wireQueue.add(neighborPos);
            } else if (neighbor instanceof com.evecual.evecualmc.energy.EnergyStorage storage
                    && !(neighbor instanceof StorageUnitBlockEntity)) {
                int needed = MAX_ENERGY - this.energy;
                long extracted = storage.extractEnergy(Math.min(needed, 50), false);
                if (extracted > 0) {
                    this.energy += (int) extracted;
                    markDirty();
                    if (this.energy >= MAX_ENERGY) return;
                }
            }
        }

        // 2. BFS traverse connected wire network
        while (!wireQueue.isEmpty() && visited.size() <= 64) {
            BlockPos wirePos = wireQueue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = wirePos.offset(dir);
                if (!visited.add(next)) continue;

                BlockState nextState = world.getBlockState(next);
                BlockEntity nextBe = world.getBlockEntity(next);

                if (nextState.isOf(EvecualMC.WIRE_BLOCK)) {
                    wireQueue.add(next);
                } else if (nextBe instanceof com.evecual.evecualmc.energy.EnergyStorage storage
                        && !(nextBe instanceof StorageUnitBlockEntity)) {
                    int needed = MAX_ENERGY - this.energy;
                    long extracted = storage.extractEnergy(Math.min(needed, 50), false);
                    if (extracted > 0) {
                        this.energy += (int) extracted;
                        BlockEntity wireBe = world.getBlockEntity(wirePos);
                        if (wireBe instanceof com.evecual.evecualmc.block.entity.WireBlockEntity wbe) {
                            wbe.recordEnergyTransfer(extracted);
                        }
                        markDirty();
                        if (this.energy >= MAX_ENERGY) return;
                    }
                }
            }
        }
    }

    public boolean isElectricallyCharged() {
        return this.energy >= SHULKER_CHARGE_THRESHOLD;
    }

    public boolean isLockedDueToPower() {
        return this.lockedDueToPower;
    }

    @Override
    public long getEnergy() {
        return this.energy;
    }

    @Override
    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long needed = MAX_ENERGY - this.energy;
        long canInsert = Math.min(amount, needed);
        if (!simulate && canInsert > 0) {
            this.energy += (int) canInsert;
            markDirty();
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long canExtract = Math.min(amount, this.energy);
        if (!simulate && canExtract > 0) {
            this.energy -= (int) canExtract;
            markDirty();
        }
        return canExtract;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(MAX_ENERGY, Math.max(0, energy));
        markDirty();
    }

    public int receiveEnergy(int amount) {
        int accepted = Math.min(amount, MAX_ENERGY - this.energy);
        this.energy += accepted;
        if (accepted > 0) markDirty();
        return accepted;
    }

    public void onPlacedFromItem(ItemStack stack) {
        if (stack.hasNbt() && stack.getSubNbt("BlockEntityTag") != null) {
            NbtCompound tag = stack.getSubNbt("BlockEntityTag");
            this.items = DefaultedList.ofSize(SLOTS_PER_UNIT, ItemStack.EMPTY);
            Inventories.readNbt(tag, this.items);
            this.energy = tag.getInt("Energy");

            // Check if it has items inside: taking electricity when placed down!
            boolean hasItems = false;
            for (ItemStack s : this.items) {
                if (!s.isEmpty()) {
                    hasItems = true;
                    break;
                }
            }

            if (hasItems) {
                if (this.energy >= SHULKER_CHARGE_THRESHOLD) {
                    this.energy -= SHULKER_CHARGE_THRESHOLD;
                    this.lockedDueToPower = false;
                } else {
                    this.lockedDueToPower = true;
                }
            } else {
                this.lockedDueToPower = false;
            }
        } else {
            this.lockedDueToPower = false;
        }
        markDirty();
    }

    public ItemStack createShulkerDropItem() {
        ItemStack stack = new ItemStack(EvecualMC.STORAGE_UNIT_ITEM);
        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, this.items);
        tag.putInt("Energy", Math.max(0, this.energy - 100)); // Packing dissipation
        stack.setSubNbt("BlockEntityTag", tag);
        return stack;
    }

    public void clearSilently() {
        this.items.clear();
        markDirty();
    }

    /**
     * Finds all adjacent connected Storage Units forming the multi-block storage bank.
     */
    public List<StorageUnitBlockEntity> findConnectedCluster() {
        List<StorageUnitBlockEntity> cluster = new ArrayList<>();
        if (this.world == null) {
            cluster.add(this);
            return cluster;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(this.pos);
        visited.add(this.pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity be = this.world.getBlockEntity(current);
            if (be instanceof StorageUnitBlockEntity storage) {
                cluster.add(storage);

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (!visited.contains(neighbor) && this.world.getBlockState(neighbor).isOf(EvecualMC.STORAGE_UNIT_BLOCK)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return cluster;
    }

    /**
     * Adds an item into this Storage Unit or its connected cluster.
     */
    public ItemStack insertItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        List<StorageUnitBlockEntity> cluster = findConnectedCluster();

        ItemStack remaining = stack.copy();
        // 1. Try stacking into existing identical items
        for (StorageUnitBlockEntity unit : cluster) {
            for (int i = 0; i < unit.size(); i++) {
                ItemStack slot = unit.getStack(i);
                if (ItemStack.canCombine(slot, remaining)) {
                    int space = Math.min(slot.getMaxCount(), unit.getMaxCountPerStack()) - slot.getCount();
                    int toAdd = Math.min(space, remaining.getCount());
                    if (toAdd > 0) {
                        slot.increment(toAdd);
                        remaining.decrement(toAdd);
                        unit.markDirty();
                        if (remaining.isEmpty()) return ItemStack.EMPTY;
                    }
                }
            }
        }

        // 2. Try placing into empty slots
        for (StorageUnitBlockEntity unit : cluster) {
            for (int i = 0; i < unit.size(); i++) {
                ItemStack slot = unit.getStack(i);
                if (slot.isEmpty()) {
                    int toAdd = Math.min(remaining.getMaxCount(), remaining.getCount());
                    ItemStack placed = remaining.split(toAdd);
                    unit.setStack(i, placed);
                    if (remaining.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        return remaining;
    }

    @Override
    public int size() {
        return SLOTS_PER_UNIT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return (slot >= 0 && slot < this.items.size()) ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.items, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(this.items, slot);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.items.size()) {
            this.items.set(slot, stack);
            if (stack.getCount() > getMaxCountPerStack()) {
                stack.setCount(getMaxCountPerStack());
            }
            markDirty();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        this.items.clear();
        markDirty();
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Quantum Storage Vault");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageUnitScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        recalculateCounts();
        if (this.world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(this.pos);
        }
    }

    public void recalculateCounts() {
        int total = 0;
        int filled = 0;
        for (ItemStack s : this.items) {
            if (!s.isEmpty()) {
                total += s.getCount();
                filled++;
            }
        }
        this.totalItemCount = total;
        this.filledSlotCount = filled;
    }

    public int getTotalItemCount() {
        return this.totalItemCount;
    }

    public int getFilledSlotCount() {
        return this.filledSlotCount;
    }

    public void sync() {
        markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.items);
        nbt.putInt("Energy", this.energy);
        nbt.putBoolean("LockedDueToPower", this.lockedDueToPower);
        recalculateCounts();
        nbt.putInt("TotalItems", this.totalItemCount);
        nbt.putInt("FilledSlots", this.filledSlotCount);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.items = DefaultedList.ofSize(SLOTS_PER_UNIT, ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.items);
        this.energy = nbt.getInt("Energy");
        this.lockedDueToPower = nbt.getBoolean("LockedDueToPower");
        if (nbt.contains("TotalItems")) {
            this.totalItemCount = nbt.getInt("TotalItems");
        } else {
            recalculateCounts();
        }
        if (nbt.contains("FilledSlots")) {
            this.filledSlotCount = nbt.getInt("FilledSlots");
        }
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
