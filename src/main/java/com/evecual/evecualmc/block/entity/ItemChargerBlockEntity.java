package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ItemChargerBlock;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.energy.ItemEnergyHelper;
import com.evecual.evecualmc.screen.ItemChargerScreenHandler;
import net.minecraft.block.Block;
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

public class ItemChargerBlockEntity extends BlockEntity implements EnergyStorage, NamedScreenHandlerFactory, SidedInventory {
    public static final int MAX_ENERGY = 4000;
    public static final int CHARGE_PER_TICK = 1; // Slow, steady high-tech trickle charging (1 EU/t = 20 EU/s)

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int energy = 0;
    private boolean active = false;
    private int soundCooldown = 0;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            ItemStack stack = inventory.get(0);
            long itemE = ItemEnergyHelper.getEnergy(stack);
            long itemMaxE = ItemEnergyHelper.getMaxEnergy(stack);

            return switch (index) {
                case 0 -> energy & 0xFFFF;
                case 1 -> (energy >> 16) & 0xFFFF;
                case 2 -> MAX_ENERGY & 0xFFFF;
                case 3 -> (MAX_ENERGY >> 16) & 0xFFFF;
                case 4 -> (int) (itemE & 0xFFFF);
                case 5 -> (int) ((itemE >> 16) & 0xFFFF);
                case 6 -> (int) (itemMaxE & 0xFFFF);
                case 7 -> (int) ((itemMaxE >> 16) & 0xFFFF);
                case 8 -> active ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = (energy & 0xFFFF0000) | (value & 0xFFFF);
                case 1 -> energy = (energy & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                case 8 -> active = (value == 1);
            }
        }

        @Override
        public int size() {
            return 9;
        }
    };

    public ItemChargerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ITEM_CHARGER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ItemChargerBlockEntity be) {
        if (world.isClient) return;

        boolean wasActive = be.active;
        ItemStack stack = be.inventory.get(0);

        if (!stack.isEmpty() && ItemEnergyHelper.isChargeable(stack)) {
            long currentItemEnergy = ItemEnergyHelper.getEnergy(stack);
            long maxItemEnergy = ItemEnergyHelper.getMaxEnergy(stack);

            if (currentItemEnergy < maxItemEnergy && be.energy >= CHARGE_PER_TICK) {
                // Slowly transfer power into the item
                long charged = ItemEnergyHelper.charge(stack, CHARGE_PER_TICK);
                if (charged > 0) {
                    be.energy -= charged;
                    be.active = true;

                    if (be.soundCooldown-- <= 0) {
                        be.soundCooldown = 50;
                        world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.2F, 1.9F);
                    }

                    if (world instanceof ServerWorld serverWorld && serverWorld.random.nextFloat() < 0.35F) {
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                                pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 2, 0.1, 0.05, 0.1, 0.02);
                    }
                    be.markDirty();
                } else {
                    be.active = false;
                }
            } else {
                be.active = false;
            }
        } else {
            be.active = false;
        }

        if (wasActive != be.active) {
            if (state.contains(ItemChargerBlock.ACTIVE)) {
                world.setBlockState(pos, state.with(ItemChargerBlock.ACTIVE, be.active), Block.NOTIFY_ALL);
            }
            be.markDirty();
            be.sync();
        }
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public boolean isActive() {
        return this.active;
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

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
        this.energy = nbt.getInt("Energy");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putBoolean("Active", this.active);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("⚡ Item Charger");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ItemChargerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
        ItemStack res = Inventories.splitStack(this.inventory, slot, amount);
        if (!res.isEmpty()) {
            markDirty();
            sync();
        }
        return res;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack res = Inventories.removeStack(this.inventory, slot);
        if (!res.isEmpty()) {
            markDirty();
            sync();
        }
        return res;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
        sync();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        this.inventory.clear();
        markDirty();
        sync();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return ItemEnergyHelper.isChargeable(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // Extract from bottom if fully charged
        if (dir == Direction.DOWN) {
            return !ItemEnergyHelper.isChargeable(stack) || ItemEnergyHelper.getEnergy(stack) >= ItemEnergyHelper.getMaxEnergy(stack);
        }
        return true;
    }
}
