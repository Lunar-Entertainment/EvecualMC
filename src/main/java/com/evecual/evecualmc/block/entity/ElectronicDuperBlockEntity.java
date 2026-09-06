package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.screen.ElectronicDuperScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
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

public class ElectronicDuperBlockEntity extends BlockEntity implements EnergyStorage, NamedScreenHandlerFactory, SidedInventory {
    public static final int MAX_ENERGY = 3000;
    public static final int CYCLE_ENERGY_COST = 1500;
    public static final int CYCLE_TOTAL_TICKS = 2400; // 2 minutes (120 sec * 20 tps)

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int energy = 0;
    private int progressTicks = 0;
    private boolean duplicating = false;
    private ItemStack duplicatingTarget = ItemStack.EMPTY;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy & 0xFFFF;
                case 1 -> (energy >> 16) & 0xFFFF;
                case 2 -> progressTicks;
                case 3 -> duplicating ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = (energy & 0xFFFF0000) | (value & 0xFFFF);
                case 1 -> energy = (energy & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case 2 -> progressTicks = value;
                case 3 -> duplicating = (value == 1);
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ElectronicDuperBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ELECTRONIC_DUPER_BLOCK_ENTITY, pos, state);
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public int getProgressTicks() {
        return this.progressTicks;
    }

    public boolean isDuplicating() {
        return this.duplicating;
    }

    public ItemStack getDuplicatingTarget() {
        return this.duplicatingTarget;
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
        long toInsert = Math.min(amount, MAX_ENERGY - this.energy);
        if (!simulate && toInsert > 0) {
            this.energy += (int) toInsert;
            markDirty();
            sync();
        }
        return toInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long toExtract = Math.min(amount, this.energy);
        if (!simulate && toExtract > 0) {
            this.energy -= (int) toExtract;
            markDirty();
            sync();
        }
        return toExtract;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectronicDuperBlockEntity be) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ItemStack inputStack = be.getStack(0);

        // Check if output slot can accept a duplicate unit
        boolean canAcceptOutput = be.canAcceptOutput(be.duplicating ? be.duplicatingTarget : inputStack);

        // Start new duplication cycle if conditions are met
        if (!be.duplicating && !inputStack.isEmpty() && be.energy >= CYCLE_ENERGY_COST && canAcceptOutput) {
            be.energy -= CYCLE_ENERGY_COST;
            be.duplicating = true;
            be.progressTicks = 0;
            be.duplicatingTarget = inputStack.copy();
            be.duplicatingTarget.setCount(1);
            be.markDirty();
            be.sync();
        }

        // Active Duplication Progress
        if (be.duplicating) {
            be.progressTicks++;

            // Quantum particle emissions around top of duplication chamber
            if (world.getTime() % 4 == 0) {
                double px = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.6;
                double py = pos.getY() + 0.9 + world.random.nextDouble() * 0.4;
                double pz = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.6;
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 2, 0.05, 0.05, 0.05, 0.01);
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, px, py, pz, 1, 0.02, 0.05, 0.02, 0.01);
            }

            // Periodic Quantum Duplicator Hum
            if (world.getTime() % 40 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 0.3F, 1.8F);
            }

            // Completion check (2 minutes / 2400 ticks)
            if (be.progressTicks >= CYCLE_TOTAL_TICKS) {
                if (!be.duplicatingTarget.isEmpty() && be.canAcceptOutput(be.duplicatingTarget)) {
                    be.depositDuplicate(be.duplicatingTarget.copy());
                    world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.8F, 1.5F);
                    world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5F, 1.8F);

                    // Quantum burst particle effect
                    serverWorld.spawnParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 12, 0.15, 0.15, 0.15, 0.05);
                }

                be.duplicating = false;
                be.progressTicks = 0;
                be.duplicatingTarget = ItemStack.EMPTY;
                be.markDirty();
                be.sync();
            }
        }
    }

    private boolean canAcceptOutput(ItemStack itemToDup) {
        if (itemToDup.isEmpty()) return false;
        ItemStack output = getStack(1);
        if (output.isEmpty()) return true;
        if (!ItemStack.canCombine(output, itemToDup)) return false;
        return output.getCount() < output.getMaxCount();
    }

    private void depositDuplicate(ItemStack duplicate) {
        ItemStack output = getStack(1);
        if (output.isEmpty()) {
            setStack(1, duplicate);
        } else if (ItemStack.canCombine(output, duplicate)) {
            output.increment(1);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("⚡ Electronic Duper");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ElectronicDuperScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    // --- Sided Inventory Implementation ---
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
        if (side == Direction.DOWN) {
            return new int[]{1}; // Extract from Output (Slot 1)
        }
        return new int[]{0}; // Insert into Input (Slot 0)
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 1;
    }

    public void sync() {
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
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

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        energy = nbt.getInt("Energy");
        progressTicks = nbt.getInt("ProgressTicks");
        duplicating = nbt.getBoolean("Duplicating");
        if (nbt.contains("DuplicatingTarget", 10)) {
            duplicatingTarget = ItemStack.fromNbt(nbt.getCompound("DuplicatingTarget"));
        } else {
            duplicatingTarget = ItemStack.EMPTY;
        }
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Energy", energy);
        nbt.putInt("ProgressTicks", progressTicks);
        nbt.putBoolean("Duplicating", duplicating);
        if (!duplicatingTarget.isEmpty()) {
            NbtCompound itemNbt = new NbtCompound();
            duplicatingTarget.writeNbt(itemNbt);
            nbt.put("DuplicatingTarget", itemNbt);
        }
        Inventories.writeNbt(nbt, inventory);
    }
}
