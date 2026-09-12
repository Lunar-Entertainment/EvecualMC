package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ElectricGrinderBlock;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.screen.ElectricGrinderScreenHandler;
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

public class ElectricGrinderBlockEntity extends BlockEntity implements EnergyStorage, NamedScreenHandlerFactory, SidedInventory {
    public static final int MAX_ENERGY = 1000;
    public static final int GRIND_TICKS = 100; // 5 seconds per item
    public static final int ENERGY_PER_TICK = 2;

    private static final int[] INPUT_SLOTS = new int[]{0};
    private static final int[] OUTPUT_SLOTS = new int[]{1};

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int energy = 0;
    private int progressTicks = 0;
    private boolean active = false;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy & 0xFFFF;
                case 1 -> (energy >> 16) & 0xFFFF;
                case 2 -> MAX_ENERGY & 0xFFFF;
                case 3 -> (MAX_ENERGY >> 16) & 0xFFFF;
                case 4 -> progressTicks;
                case 5 -> GRIND_TICKS;
                case 6 -> active ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = (energy & 0xFFFF0000) | (value & 0xFFFF);
                case 1 -> energy = (energy & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case 4 -> progressTicks = value;
                case 6 -> active = (value == 1);
            }
        }

        @Override
        public int size() {
            return 7;
        }
    };

    public ElectricGrinderBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ELECTRIC_GRINDER_BLOCK_ENTITY, pos, state);
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public int getProgressTicks() {
        return this.progressTicks;
    }

    public int getTotalTicks() {
        return GRIND_TICKS;
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

    public static void tick(World world, BlockPos pos, BlockState state, ElectricGrinderBlockEntity be) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ItemStack input = be.getStack(0);

        boolean isCopper = be.isValidCopperInput(input);

        if (isCopper) {
            int outputCount = input.isOf(Items.COPPER_BLOCK) ? 9 : 1;
            ItemStack targetOutput = new ItemStack(EvecualMC.COPPER_PLATE, outputCount);
            boolean canAccept = be.canAcceptOutput(targetOutput);

            if (canAccept && be.energy >= ENERGY_PER_TICK) {
                be.active = true;
                be.progressTicks++;
                be.energy -= ENERGY_PER_TICK;

                // Grinding sparks and mechanical dust
                if (world.getTime() % 3 == 0) {
                    double px = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.4;
                    double py = pos.getY() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3;
                    double pz = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.4;
                    serverWorld.spawnParticles(ParticleTypes.CRIT, px, py, pz, 2, 0.05, 0.05, 0.05, 0.03);
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.02, 0.02, 0.02, 0.01);
                }

                // Grinder rumble audio
                if (world.getTime() % 20 == 0) {
                    world.playSound(null, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.35F, 1.25F);
                }

                if (be.progressTicks >= GRIND_TICKS) {
                    be.depositOutput(targetOutput.copy());
                    input.decrement(1);
                    if (input.isEmpty()) {
                        be.setStack(0, ItemStack.EMPTY);
                    }

                    // Completion audio & particle burst
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.4F, 1.8F);
                    world.playSound(null, pos, SoundEvents.BLOCK_COPPER_PLACE, SoundCategory.BLOCKS, 0.7F, 1.3F);
                    serverWorld.spawnParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 12, 0.15, 0.15, 0.15, 0.05);

                    be.progressTicks = 0;
                    be.markDirty();
                    be.sync();
                }
            } else {
                be.active = false;
            }
        } else {
            be.active = false;
            if (be.progressTicks > 0) {
                be.progressTicks = 0;
                be.markDirty();
                be.sync();
            }
        }

        if (state.get(ElectricGrinderBlock.ACTIVE) != be.active) {
            world.setBlockState(pos, state.with(ElectricGrinderBlock.ACTIVE, be.active), Block.NOTIFY_LISTENERS);
        }

        if (world.getTime() % 20 == 0) {
            be.markDirty();
            be.sync();
        }
    }

    private boolean isValidCopperInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.isOf(Items.COPPER_INGOT) || stack.isOf(Items.RAW_COPPER) || stack.isOf(Items.COPPER_BLOCK);
    }

    private boolean canAcceptOutput(ItemStack item) {
        if (item.isEmpty()) return false;
        ItemStack output = getStack(1);
        if (output.isEmpty()) return true;
        if (!ItemStack.canCombine(output, item)) return false;
        return (output.getCount() + item.getCount()) <= output.getMaxCount();
    }

    private void depositOutput(ItemStack result) {
        ItemStack output = getStack(1);
        if (output.isEmpty()) {
            setStack(1, result);
        } else if (ItemStack.canCombine(output, result)) {
            output.increment(result.getCount());
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("⚙️ Electric Grinder");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ElectricGrinderScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
            return OUTPUT_SLOTS;
        }
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 0 && isValidCopperInput(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 1 && dir == Direction.DOWN;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, inventory);
        this.energy = nbt.getInt("Energy");
        this.progressTicks = nbt.getInt("ProgressTicks");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("ProgressTicks", this.progressTicks);
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
