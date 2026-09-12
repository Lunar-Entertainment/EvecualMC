package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.MaterializerBlock;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.screen.MaterializerScreenHandler;
import net.minecraft.block.Block;
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

public class MaterializerBlockEntity extends BlockEntity implements EnergyStorage, NamedScreenHandlerFactory, SidedInventory {
    public static final int MAX_ENERGY = 4000;
    public static final int ELACTORITE_TICKS = 2400; // 2 minutes
    public static final int STEEL_TICKS = 400;       // 20 seconds

    public static final int RECIPE_NONE = 0;
    public static final int RECIPE_ELACTORITE = 1;
    public static final int RECIPE_STEEL = 2;

    private static final int[] TOP_SIDE_SLOTS = new int[]{0, 1};
    private static final int[] BOTTOM_SLOTS = new int[]{2};

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private int energy = 0;
    private int progressTicks = 0;
    private int totalTicks = ELACTORITE_TICKS;
    private int recipeType = RECIPE_NONE;
    private boolean active = false;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy & 0xFFFF;
                case 1 -> (energy >> 16) & 0xFFFF;
                case 2 -> MAX_ENERGY & 0xFFFF;
                case 3 -> (MAX_ENERGY >> 16) & 0xFFFF;
                case 4 -> progressTicks & 0xFFFF;
                case 5 -> (progressTicks >> 16) & 0xFFFF;
                case 6 -> totalTicks & 0xFFFF;
                case 7 -> (totalTicks >> 16) & 0xFFFF;
                case 8 -> active ? 1 : 0;
                case 9 -> recipeType;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = (energy & 0xFFFF0000) | (value & 0xFFFF);
                case 1 -> energy = (energy & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case 4 -> progressTicks = (progressTicks & 0xFFFF0000) | (value & 0xFFFF);
                case 5 -> progressTicks = (progressTicks & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case 6 -> totalTicks = (totalTicks & 0xFFFF0000) | (value & 0xFFFF);
                case 7 -> totalTicks = (totalTicks & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case 8 -> active = (value == 1);
                case 9 -> recipeType = value;
            }
        }

        @Override
        public int size() {
            return 10;
        }
    };

    public MaterializerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.MATERIALIZER_BLOCK_ENTITY, pos, state);
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public int getProgressTicks() {
        return this.progressTicks;
    }

    public int getTotalTicks() {
        return this.totalTicks;
    }

    public int getRecipeType() {
        return this.recipeType;
    }

    public boolean isActive() {
        return this.active;
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

    public static void tick(World world, BlockPos pos, BlockState state, MaterializerBlockEntity be) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ItemStack stack0 = be.getStack(0);
        ItemStack stack1 = be.getStack(1);

        int currentRecipe = be.detectRecipe(stack0, stack1);
        be.recipeType = currentRecipe;

        if (currentRecipe != RECIPE_NONE) {
            int requiredTime = (currentRecipe == RECIPE_ELACTORITE) ? ELACTORITE_TICKS : STEEL_TICKS;
            be.totalTicks = requiredTime;
            ItemStack targetOutput = (currentRecipe == RECIPE_ELACTORITE)
                    ? new ItemStack(EvecualMC.ELACTORITE)
                    : new ItemStack(EvecualMC.STEEL_INGOT);

            boolean canAccept = be.canAcceptOutput(targetOutput);

            if (canAccept && be.energy >= 1) {
                be.active = true;
                be.progressTicks++;
                be.energy -= 1; // 1 EU per tick consumption

                // Quantum plasma synthesis particle FX
                if (world.getTime() % 3 == 0) {
                    double px = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5;
                    double py = pos.getY() + 0.4 + world.random.nextDouble() * 0.5;
                    double pz = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5;

                    if (currentRecipe == RECIPE_ELACTORITE) {
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 2, 0.05, 0.05, 0.05, 0.02);
                        serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, px, py, pz, 1, 0.02, 0.03, 0.02, 0.01);
                    } else {
                        serverWorld.spawnParticles(ParticleTypes.CRIT, px, py, pz, 2, 0.04, 0.04, 0.04, 0.02);
                        serverWorld.spawnParticles(ParticleTypes.SMOKE, px, py, pz, 1, 0.01, 0.03, 0.01, 0.01);
                    }
                }

                // Periodic resonant sound
                if (world.getTime() % 40 == 0) {
                    world.playSound(null, pos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 0.4F,
                            currentRecipe == RECIPE_ELACTORITE ? 1.6F : 1.2F);
                }

                // Completion check
                if (be.progressTicks >= be.totalTicks) {
                    be.depositOutput(targetOutput.copy());
                    be.consumeInputs(currentRecipe);

                    // High-tech synthesized completion audio & particles
                    world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.8F, 1.4F);
                    world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.6F, 1.8F);
                    serverWorld.spawnParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 18, 0.2, 0.2, 0.2, 0.05);

                    be.progressTicks = 0;
                    be.markDirty();
                    be.sync();
                }
            } else {
                // Out of power or output blocked -> pause progress
                be.active = false;
            }
        } else {
            // Recipe invalid / missing ingredients
            be.active = false;
            if (be.progressTicks > 0) {
                be.progressTicks = 0;
                be.markDirty();
                be.sync();
            }
        }

        // Update blockstate active property if changed
        if (state.get(MaterializerBlock.ACTIVE) != be.active) {
            world.setBlockState(pos, state.with(MaterializerBlock.ACTIVE, be.active), Block.NOTIFY_LISTENERS);
        }

        if (world.getTime() % 20 == 0) {
            be.markDirty();
            be.sync();
        }
    }

    private int detectRecipe(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return RECIPE_NONE;

        // Elactorite: 2 Iron Ingots + 1 Diamond
        boolean ironInA = a.isOf(Items.IRON_INGOT) && a.getCount() >= 2;
        boolean diamondInB = b.isOf(Items.DIAMOND) && b.getCount() >= 1;
        boolean ironInB = b.isOf(Items.IRON_INGOT) && b.getCount() >= 2;
        boolean diamondInA = a.isOf(Items.DIAMOND) && a.getCount() >= 1;

        if ((ironInA && diamondInB) || (diamondInA && ironInB)) {
            return RECIPE_ELACTORITE;
        }

        // Steel Ingot: 2 Iron Ingots + 1 Coal/Charcoal
        boolean isCoalA = (a.isOf(Items.COAL) || a.isOf(Items.CHARCOAL)) && a.getCount() >= 1;
        boolean isCoalB = (b.isOf(Items.COAL) || b.isOf(Items.CHARCOAL)) && b.getCount() >= 1;

        if ((ironInA && isCoalB) || (isCoalA && ironInB)) {
            return RECIPE_STEEL;
        }

        return RECIPE_NONE;
    }

    private boolean canAcceptOutput(ItemStack item) {
        if (item.isEmpty()) return false;
        ItemStack output = getStack(2);
        if (output.isEmpty()) return true;
        if (!ItemStack.canCombine(output, item)) return false;
        return (output.getCount() + item.getCount()) <= output.getMaxCount();
    }

    private void depositOutput(ItemStack result) {
        ItemStack output = getStack(2);
        if (output.isEmpty()) {
            setStack(2, result);
        } else if (ItemStack.canCombine(output, result)) {
            output.increment(result.getCount());
        }
    }

    private void consumeInputs(int recipe) {
        ItemStack stack0 = getStack(0);
        ItemStack stack1 = getStack(1);

        if (recipe == RECIPE_ELACTORITE) {
            if (stack0.isOf(Items.IRON_INGOT)) {
                stack0.decrement(2);
                stack1.decrement(1);
            } else {
                stack0.decrement(1);
                stack1.decrement(2);
            }
        } else if (recipe == RECIPE_STEEL) {
            if (stack0.isOf(Items.IRON_INGOT)) {
                stack0.decrement(2);
                stack1.decrement(1);
            } else {
                stack0.decrement(1);
                stack1.decrement(2);
            }
        }

        if (stack0.isEmpty()) setStack(0, ItemStack.EMPTY);
        if (stack1.isEmpty()) setStack(1, ItemStack.EMPTY);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("⚡ Quantum Materializer");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MaterializerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        return TOP_SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 2) return false;
        return stack.isOf(Items.IRON_INGOT) || stack.isOf(Items.DIAMOND) || stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 2 && dir == Direction.DOWN;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, inventory);
        this.energy = nbt.getInt("Energy");
        this.progressTicks = nbt.getInt("ProgressTicks");
        this.totalTicks = nbt.getInt("TotalTicks");
        this.recipeType = nbt.getInt("RecipeType");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("ProgressTicks", this.progressTicks);
        nbt.putInt("TotalTicks", this.totalTicks);
        nbt.putInt("RecipeType", this.recipeType);
        nbt.putBoolean("Active", this.active);
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

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }
}
