package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public class PickupDroneEntity extends RcDroneEntity {

    public static final int CARGO_SIZE = 27;
    private final net.minecraft.inventory.SimpleInventory cargo = new net.minecraft.inventory.SimpleInventory(CARGO_SIZE);

    public PickupDroneEntity(EntityType<? extends PickupDroneEntity> type, World world) {
        super(type, world);
        this.setColorVariant(6); // 6 = Tactical Defense (Pure Gunmetal & Hazard)
    }

    @Override
    public net.minecraft.inventory.SimpleInventory getTrunk() {
        return this.cargo;
    }

    @Override
    public void tick() {
        super.tick();

        // Server-side automated item pickup / vacuum into 27-slot cargo bay
        if (!this.getWorld().isClient && this.isAlive() && !this.isRemoved()) {
            List<ItemEntity> nearbyItems = this.getWorld().getEntitiesByClass(
                    ItemEntity.class,
                    this.getBoundingBox().expand(1.8),
                    ItemEntity::isAlive
            );
            for (ItemEntity item : nearbyItems) {
                if (item.cannotPickup()) continue;
                ItemStack stack = item.getStack();
                ItemStack remainder = this.getTrunk().addStack(stack);
                if (remainder.getCount() != stack.getCount()) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.4f, 1.4f);
                }
                if (remainder.isEmpty()) {
                    item.discard();
                } else {
                    item.setStack(remainder);
                }
            }
        }
    }

    @Override
    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, this.cargo, 3),
                    Text.literal("Pickup Drone Cargo (27 Slots)")
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.6f, 2.0f);
        }
    }

    @Override
    protected boolean isParkingSpotBlock(net.minecraft.block.BlockState bs) {
        return bs.isOf(EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK) || bs.isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("CargoItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("CargoItems"), list);
            for (int i = 0; i < list.size(); ++i) {
                this.cargo.setStack(i, list.get(i));
            }
        } else if (nbt.contains("TrunkItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
            for (int i = 0; i < list.size(); ++i) {
                this.cargo.setStack(i, list.get(i));
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < CARGO_SIZE; ++i) {
            list.set(i, this.cargo.getStack(i));
        }
        NbtCompound cargoNbt = new NbtCompound();
        Inventories.writeNbt(cargoNbt, list);
        nbt.put("CargoItems", cargoNbt);
        nbt.put("TrunkItems", cargoNbt);
    }

    @Override
    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(EvecualMC.PICKUP_DRONE_ITEM);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        if (!getPairedPlayerUuid().isEmpty()) {
            nbt.putString("PairedPlayer", getPairedPlayerUuid());
        }

        // Save cargo items into item stack NBT
        DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
        boolean hasItems = false;
        for (int i = 0; i < CARGO_SIZE; ++i) {
            ItemStack s = this.cargo.getStack(i);
            list.set(i, s);
            if (!s.isEmpty()) hasItems = true;
        }
        if (hasItems) {
            NbtCompound trunkNbt = new NbtCompound();
            Inventories.writeNbt(trunkNbt, list);
            nbt.put("TrunkItems", trunkNbt);
            nbt.put("CargoItems", trunkNbt);
        }

        stack.setNbt(nbt);
        return stack;
    }
}
