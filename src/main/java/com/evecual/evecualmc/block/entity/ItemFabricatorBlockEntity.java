package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ItemFabricatorBlock;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.screen.ItemFabricatorScreenHandler;
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

public class ItemFabricatorBlockEntity extends BlockEntity implements EnergyStorage, NamedScreenHandlerFactory, SidedInventory {
    public static final int INVENTORY_SIZE = 7; // Slots 0..5: Inputs, Slot 6: Output
    public static final int MAX_ENERGY = 4000;

    public static final int RECIPE_ZAPPER = 1;
    public static final int RECIPE_RAILGUN = 2;
    public static final int RECIPE_DUPER = 3;

    public static final int ZAPPER_TICKS = 100;    // 5 seconds
    public static final int ZAPPER_EU_TICK = 5;    // 500 EU total

    public static final int RAILGUN_TICKS = 250;   // 12.5 seconds
    public static final int RAILGUN_EU_TICK = 10;  // 2500 EU total

    public static final int DUPER_TICKS = 300;     // 15 seconds
    public static final int DUPER_EU_TICK = 10;    // 3000 EU total

    private static final int[] INPUT_SLOTS = new int[]{0, 1, 2, 3, 4, 5};
    private static final int[] OUTPUT_SLOTS = new int[]{6};

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int energy = 0;
    private int progressTicks = 0;
    private int totalTicks = ZAPPER_TICKS;
    private int selectedRecipe = RECIPE_ZAPPER;
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
                case 9 -> selectedRecipe;
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
                case 9 -> selectedRecipe = value;
            }
        }

        @Override
        public int size() {
            return 10;
        }
    };

    public ItemFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ITEM_FABRICATOR_BLOCK_ENTITY, pos, state);
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public int getSelectedRecipe() {
        return this.selectedRecipe;
    }

    public void setSelectedRecipe(int recipe) {
        if (this.selectedRecipe != recipe) {
            this.selectedRecipe = recipe;
            this.progressTicks = 0;
            this.totalTicks = (recipe == RECIPE_DUPER) ? DUPER_TICKS : ((recipe == RECIPE_RAILGUN) ? RAILGUN_TICKS : ZAPPER_TICKS);
            markDirty();
            sync();
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ItemFabricatorBlockEntity be) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        int targetRecipe = be.selectedRecipe;
        be.totalTicks = (targetRecipe == RECIPE_DUPER) ? DUPER_TICKS : ((targetRecipe == RECIPE_RAILGUN) ? RAILGUN_TICKS : ZAPPER_TICKS);
        int euPerTick = (targetRecipe == RECIPE_DUPER) ? DUPER_EU_TICK : ((targetRecipe == RECIPE_RAILGUN) ? RAILGUN_EU_TICK : ZAPPER_EU_TICK);

        boolean canCraft = be.canCraft(targetRecipe);
        boolean canAccept = be.canAcceptOutput(targetRecipe);

        if (canCraft && canAccept && be.energy >= euPerTick) {
            be.energy -= euPerTick;
            be.progressTicks++;

            if (!be.active) {
                be.active = true;
                world.setBlockState(pos, state.with(ItemFabricatorBlock.ACTIVE, true), 3);
            }

            // High-voltage fabrication particle emissions
            if (world.getTime() % 4 == 0) {
                double px = pos.getX() + 0.3 + world.random.nextDouble() * 0.4;
                double py = pos.getY() + 0.8 + world.random.nextDouble() * 0.3;
                double pz = pos.getZ() + 0.3 + world.random.nextDouble() * 0.4;
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 2, 0.05, 0.05, 0.05, 0.02);
                if (targetRecipe == RECIPE_RAILGUN) {
                    serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, px, py, pz, 1, 0.01, 0.02, 0.01, 0.01);
                }
            }

            if (world.getTime() % 30 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 0.4F, 1.8F);
            }

            if (be.progressTicks >= be.totalTicks) {
                be.craftItem(targetRecipe);
                be.progressTicks = 0;

                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.7F, 1.4F);
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.6F, 1.6F);

                double px = pos.getX() + 0.5;
                double py = pos.getY() + 0.9;
                double pz = pos.getZ() + 0.5;
                serverWorld.spawnParticles(ParticleTypes.FLASH, px, py, pz, 1, 0, 0, 0, 0);
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 20, 0.2, 0.2, 0.2, 0.05);
            }

            be.markDirty();
            be.sync();
        } else {
            if (be.active) {
                be.active = false;
                world.setBlockState(pos, state.with(ItemFabricatorBlock.ACTIVE, false), 3);
            }
            if (be.progressTicks > 0 && !canCraft) {
                be.progressTicks = 0;
            }
            be.markDirty();
            be.sync();
        }
    }

    public boolean canCraft(int recipe) {
        if (recipe == RECIPE_ZAPPER) {
            // Slot 0: Elactorite (1)
            // Slot 1: Steel Rod (2)
            // Slot 2: Battery (1)
            // Slot 3: Wire (2)
            ItemStack s0 = this.inventory.get(0);
            ItemStack s1 = this.inventory.get(1);
            ItemStack s2 = this.inventory.get(2);
            ItemStack s3 = this.inventory.get(3);

            return s0.isOf(EvecualMC.ELACTORITE) && s0.getCount() >= 1 &&
                   s1.isOf(EvecualMC.STEEL_ROD) && s1.getCount() >= 2 &&
                   s2.isOf(EvecualMC.BATTERY_ITEM) && s2.getCount() >= 1 &&
                   s3.isOf(EvecualMC.WIRE_ITEM) && s3.getCount() >= 2;
        } else if (recipe == RECIPE_RAILGUN) {
            // Hard Survival Recipe:
            // Slot 0: Netherite Ingot (2)
            // Slot 1: Elactorite (4)
            // Slot 2: Steel Rod (4)
            // Slot 3: Upgraded Electric Engine (1)
            // Slot 4: Copper Plate (4)
            // Slot 5: Wire (4)
            ItemStack s0 = this.inventory.get(0);
            ItemStack s1 = this.inventory.get(1);
            ItemStack s2 = this.inventory.get(2);
            ItemStack s3 = this.inventory.get(3);
            ItemStack s4 = this.inventory.get(4);
            ItemStack s5 = this.inventory.get(5);

            return s0.isOf(Items.NETHERITE_INGOT) && s0.getCount() >= 2 &&
                   s1.isOf(EvecualMC.ELACTORITE) && s1.getCount() >= 4 &&
                   s2.isOf(EvecualMC.STEEL_ROD) && s2.getCount() >= 4 &&
                   s3.isOf(EvecualMC.UPGRADED_ENGINE) && s3.getCount() >= 1 &&
                   s4.isOf(EvecualMC.COPPER_PLATE) && s4.getCount() >= 4 &&
                   s5.isOf(EvecualMC.WIRE_ITEM) && s5.getCount() >= 4;
        } else if (recipe == RECIPE_DUPER) {
            // Hard Survival Recipe for Electronic Duper:
            // Slot 0: Quantum Materializer (1)
            // Slot 1: Netherite Ingot (1)
            // Slot 2: Elactorite (4)
            // Slot 3: Upgraded Electric Engine (1)
            // Slot 4: Diamond Block (1)
            // Slot 5: Wire (8)
            ItemStack s0 = this.inventory.get(0);
            ItemStack s1 = this.inventory.get(1);
            ItemStack s2 = this.inventory.get(2);
            ItemStack s3 = this.inventory.get(3);
            ItemStack s4 = this.inventory.get(4);
            ItemStack s5 = this.inventory.get(5);

            return s0.isOf(EvecualMC.MATERIALIZER_ITEM) && s0.getCount() >= 1 &&
                   s1.isOf(Items.NETHERITE_INGOT) && s1.getCount() >= 1 &&
                   s2.isOf(EvecualMC.ELACTORITE) && s2.getCount() >= 4 &&
                   s3.isOf(EvecualMC.UPGRADED_ENGINE) && s3.getCount() >= 1 &&
                   s4.isOf(Items.DIAMOND_BLOCK) && s4.getCount() >= 1 &&
                   s5.isOf(EvecualMC.WIRE_ITEM) && s5.getCount() >= 8;
        }
        return false;
    }

    public boolean canAcceptOutput(int recipe) {
        ItemStack currentOut = this.inventory.get(6);
        if (currentOut.isEmpty()) return true;
        ItemStack expected = getResultStack(recipe);
        return ItemStack.canCombine(currentOut, expected) &&
               (currentOut.getCount() + expected.getCount() <= currentOut.getMaxCount());
    }

    public ItemStack getResultStack(int recipe) {
        if (recipe == RECIPE_ZAPPER) {
            return new ItemStack(EvecualMC.ELECTRONIC_ZAPPER, 1);
        } else if (recipe == RECIPE_RAILGUN) {
            return new ItemStack(EvecualMC.RAILGUN_ITEM, 1);
        } else if (recipe == RECIPE_DUPER) {
            return new ItemStack(EvecualMC.ELECTRONIC_DUPER_ITEM, 1);
        }
        return ItemStack.EMPTY;
    }

    private void craftItem(int recipe) {
        if (!canCraft(recipe) || !canAcceptOutput(recipe)) return;

        if (recipe == RECIPE_ZAPPER) {
            this.inventory.get(0).decrement(1);
            this.inventory.get(1).decrement(2);
            this.inventory.get(2).decrement(1);
            this.inventory.get(3).decrement(2);
        } else if (recipe == RECIPE_RAILGUN) {
            this.inventory.get(0).decrement(2);
            this.inventory.get(1).decrement(4);
            this.inventory.get(2).decrement(4);
            this.inventory.get(3).decrement(1);
            this.inventory.get(4).decrement(4);
            this.inventory.get(5).decrement(4);
        } else if (recipe == RECIPE_DUPER) {
            this.inventory.get(0).decrement(1);
            this.inventory.get(1).decrement(1);
            this.inventory.get(2).decrement(4);
            this.inventory.get(3).decrement(1);
            this.inventory.get(4).decrement(1);
            this.inventory.get(5).decrement(8);
        }

        ItemStack result = getResultStack(recipe);
        ItemStack out = this.inventory.get(6);
        if (out.isEmpty()) {
            this.inventory.set(6, result);
        } else {
            out.increment(result.getCount());
        }
    }

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
        ItemStack result = Inventories.removeStack(this.inventory, slot);
        if (!result.isEmpty()) markDirty();
        return result;
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
        markDirty();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return OUTPUT_SLOTS;
        }
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot >= 0 && slot <= 5;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 6;
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
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("ProgressTicks", this.progressTicks);
        nbt.putInt("SelectedRecipe", this.selectedRecipe);
        nbt.putBoolean("Active", this.active);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        this.energy = nbt.getInt("Energy");
        this.progressTicks = nbt.getInt("ProgressTicks");
        this.selectedRecipe = nbt.contains("SelectedRecipe") ? nbt.getInt("SelectedRecipe") : RECIPE_ZAPPER;
        this.active = nbt.getBoolean("Active");
        this.totalTicks = (this.selectedRecipe == RECIPE_RAILGUN) ? RAILGUN_TICKS : ZAPPER_TICKS;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("⚡ Item Fabricator");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ItemFabricatorScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }
}
