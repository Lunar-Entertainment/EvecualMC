package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElectronicCombinerBlockEntity extends BlockEntity implements EnergyStorage, NamedScreenHandlerFactory, Inventory {
    public static final int MAX_ENERGY = 500;
    public static final int MAX_PROGRESS = 100;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int energy = 0;
    private int progress = 0;

    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ElectronicCombinerBlockEntity.this.progress;
                case 1 -> MAX_PROGRESS;
                case 2 -> ElectronicCombinerBlockEntity.this.energy;
                case 3 -> MAX_ENERGY;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ElectronicCombinerBlockEntity.this.progress = value;
                case 2 -> ElectronicCombinerBlockEntity.this.energy = value;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ElectronicCombinerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ELECTRONIC_COMBINER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectronicCombinerBlockEntity be) {
        if (world.isClient) return;

        if (be.hasValidRecipe() && be.energy >= 1) {
            be.progress++;
            if (be.progress % 2 == 0) {
                be.energy--; // Drains energy during assembly
            }

            if (be.progress >= MAX_PROGRESS) {
                be.craftItem();
                be.progress = 0;
            }
            be.markDirty();
            be.sync();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.markDirty();
                be.sync();
            }
        }
    }

    private boolean hasValidRecipe() {
        ItemStack engine = this.inventory.get(0);
        ItemStack hull = this.inventory.get(1);
        ItemStack glass = this.inventory.get(2);
        ItemStack leather = this.inventory.get(3);
        ItemStack output = this.inventory.get(4);

        boolean hasEngine = engine.isOf(EvecualMC.ENGINE);
        boolean hasHull = hull.isOf(EvecualMC.STEEL_INGOT) || hull.isOf(Items.IRON_INGOT);
        boolean hasGlass = glass.isOf(Items.GLASS) || glass.isOf(Blocks.GLASS.asItem()) || glass.isOf(Items.TINTED_GLASS);
        boolean hasLeather = leather.isOf(Items.LEATHER);

        boolean canOutput = output.isEmpty() || (output.isOf(EvecualMC.CAR_ITEM) && output.getCount() < output.getMaxCount());

        return hasEngine && hasHull && hasGlass && hasLeather && canOutput;
    }

    private void craftItem() {
        this.removeStack(0, 1);
        this.removeStack(1, 1);
        this.removeStack(2, 1);
        this.removeStack(3, 1);

        ItemStack output = this.inventory.get(4);
        if (output.isEmpty()) {
            this.setStack(4, new ItemStack(EvecualMC.CAR_ITEM, 1));
        } else if (output.isOf(EvecualMC.CAR_ITEM)) {
            output.increment(1);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.evecualmc.electronic_combiner");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ElectronicCombinerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public long getEnergy() {
        return energy;
    }

    @Override
    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long canInsert = Math.min(amount, MAX_ENERGY - energy);
        if (!simulate && canInsert > 0) {
            energy += canInsert;
            markDirty();
            sync();
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long canExtract = Math.min(amount, energy);
        if (!simulate && canExtract > 0) {
            energy -= canExtract;
            markDirty();
            sync();
        }
        return canExtract;
    }

    public int getProgress() {
        return progress;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    // Inventory Implementation
    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
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
        this.inventory.clear();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        this.energy = nbt.getInt("Energy");
        this.progress = nbt.getInt("Progress");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("Progress", this.progress);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
