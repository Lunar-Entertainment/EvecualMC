package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import com.evecual.evecualmc.energy.EnergyStorage;
import net.minecraft.screen.NamedScreenHandlerFactory;

public class ElectronicCombinerBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory, EnergyStorage {
    public static final int INVENTORY_SIZE = 7;
    public static final int MAX_ENERGY = 500;
    public static final int MAX_PROGRESS = 100;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int energy = 0;
    private int progress = 0;
    private int selectedRecipeIndex = 1;

    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ElectronicCombinerBlockEntity.this.progress;
                case 1 -> MAX_PROGRESS;
                case 2 -> ElectronicCombinerBlockEntity.this.energy;
                case 3 -> MAX_ENERGY;
                case 4 -> ElectronicCombinerBlockEntity.this.selectedRecipeIndex;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ElectronicCombinerBlockEntity.this.progress = value;
                case 2 -> ElectronicCombinerBlockEntity.this.energy = value;
                case 4 -> ElectronicCombinerBlockEntity.this.selectedRecipeIndex = value;
            }
        }

        @Override
        public int size() {
            return 5;
        }
    };

    public ElectronicCombinerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ELECTRONIC_COMBINER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectronicCombinerBlockEntity be) {
        if (world.isClient) return;

        com.evecual.evecualmc.recipe.CombinerRecipe recipe = com.evecual.evecualmc.recipe.CombinerRecipe.getRecipeByIndex(be.selectedRecipeIndex);
        if (recipe != null && be.canCraftCurrentRecipe(recipe) && be.energy >= 1) {
            be.progress++;
            if (be.progress % 2 == 0) {
                be.energy--; // Drains energy during assembly
            }

            if (be.progress >= MAX_PROGRESS) {
                be.craftCurrentRecipe(recipe);
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

    private boolean canCraftCurrentRecipe(com.evecual.evecualmc.recipe.CombinerRecipe recipe) {
        if (!recipe.canCraft(this.inventory.subList(0, 6))) {
            return false;
        }
        ItemStack targetOutput = recipe.createOutput(this.inventory.subList(0, 6));
        ItemStack currentOutput = this.inventory.get(6);
        if (currentOutput.isEmpty()) return true;
        if (!ItemStack.canCombine(currentOutput, targetOutput)) return false;
        return (currentOutput.getCount() + targetOutput.getCount()) <= currentOutput.getMaxCount();
    }

    private void craftCurrentRecipe(com.evecual.evecualmc.recipe.CombinerRecipe recipe) {
        ItemStack output = recipe.createOutput(this.inventory.subList(0, 6));
        recipe.consumeInputs(this.inventory.subList(0, 6));

        ItemStack currentOutput = this.inventory.get(6);
        if (currentOutput.isEmpty()) {
            this.setStack(6, output);
        } else if (ItemStack.canCombine(currentOutput, output)) {
            currentOutput.increment(output.getCount());
        }
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex;
    }

    public void setSelectedRecipeIndex(int index) {
        this.selectedRecipeIndex = index;
        this.progress = 0;
        markDirty();
        sync();
    }

    public static int getGlassColorFromItem(Item item) {
        return com.evecual.evecualmc.recipe.CombinerRecipe.getGlassColorFromItem(item);
    }

    public static int getCarColorFromDye(ItemStack stack) {
        return com.evecual.evecualmc.recipe.CombinerRecipe.getCarColorFromDye(stack);
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

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    public boolean isCrafting() {
        return progress > 0;
    }

    public void receiveEnergy(int amount) {
        insertEnergy(amount, false);
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long needed = MAX_ENERGY - energy;
        long toInsert = Math.min(amount, needed);
        if (!simulate && toInsert > 0) {
            this.energy += (int) toInsert;
            markDirty();
            sync();
        }
        return toInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long toExtract = Math.min(amount, energy);
        if (!simulate && toExtract > 0) {
            this.energy -= (int) toExtract;
            markDirty();
            sync();
        }
        return toExtract;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("Progress", this.progress);
        nbt.putInt("SelectedRecipe", this.selectedRecipeIndex);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        this.energy = nbt.getInt("Energy");
        this.progress = nbt.getInt("Progress");
        this.selectedRecipeIndex = nbt.contains("SelectedRecipe") ? Math.max(1, nbt.getInt("SelectedRecipe")) : 1;
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
    public int size() {
        return INVENTORY_SIZE;
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
        if (!result.isEmpty()) {
            markDirty();
        }
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
        return this.world != null && this.world.getBlockEntity(this.pos) == this && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{6}; // Output slot
        }
        return new int[]{0, 1, 2, 3, 4, 5}; // Input slots
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 6) return false;
        com.evecual.evecualmc.recipe.CombinerRecipe recipe = com.evecual.evecualmc.recipe.CombinerRecipe.getRecipeByIndex(this.selectedRecipeIndex);
        if (recipe == null) return false;
        return recipe.isValidInput(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 6;
    }
}
