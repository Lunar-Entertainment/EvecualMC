package com.evecual.evecualmc.energy;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.*;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import com.evecual.evecualmc.item.ElectronicZapperItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemEnergyHelper {

    public static final int ZAPPER_MAX_ENERGY = 1000;

    /**
     * Checks if the given item stack can store EU energy and be charged.
     */
    public static boolean isChargeable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();

        if (item instanceof ElectronicZapperItem) return true;
        if (item instanceof com.evecual.evecualmc.item.crown.ElectriciansCrownItem) return true;
        if (stack.isOf(EvecualMC.RAILGUN_ITEM)) return true;
        if (stack.isOf(EvecualMC.RC_CAR_ITEM)) return true;
        if (stack.isOf(EvecualMC.RC_DRONE_ITEM)) return true;
        if (stack.isOf(EvecualMC.PICKUP_DRONE_ITEM)) return true;
        if (stack.isOf(EvecualMC.RC_ROBOT_ITEM)) return true;
        if (stack.isOf(EvecualMC.HELI_ITEM)) return true;

        if (stack.isOf(EvecualMC.BATTERY_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.ITEM_FABRICATOR_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.MATERIALIZER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.ELECTRIC_GRINDER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.ELECTRONIC_DUPER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.STATIONARY_TURRET_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.SOLAR_PANEL_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.ELECTRONIC_COMBINER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.AUTO_PICKUP_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.CHARGER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.RC_CHARGER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.HELI_CHARGER_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.ELECTRIC_CHUTE_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.STORAGE_UNIT_BLOCK.asItem())) return true;
        if (stack.isOf(EvecualMC.WIND_TURBINE_BLOCK.asItem())) return true;
        if (EvecualMC.ITEM_CHARGER_BLOCK != null && stack.isOf(EvecualMC.ITEM_CHARGER_BLOCK.asItem())) return true;

        // Generic fallback for any item containing Energy tag
        return stack.hasNbt() && (stack.getNbt().contains("Energy") || (stack.getSubNbt("BlockEntityTag") != null && stack.getSubNbt("BlockEntityTag").contains("Energy")));
    }

    /**
     * Gets the maximum energy capacity of the given item stack.
     */
    public static long getMaxEnergy(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();

        if (item instanceof ElectronicZapperItem) return ZAPPER_MAX_ENERGY;
        if (item instanceof com.evecual.evecualmc.item.crown.ElectriciansCrownItem) return com.evecual.evecualmc.item.crown.ElectriciansCrownItem.MAX_ENERGY;
        if (stack.isOf(EvecualMC.RAILGUN_ITEM)) return 2000;
        if (stack.isOf(EvecualMC.RC_CAR_ITEM)) return RcCarEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.RC_DRONE_ITEM) || stack.isOf(EvecualMC.PICKUP_DRONE_ITEM)) return RcDroneEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.RC_ROBOT_ITEM)) return RcRobotEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.HELI_ITEM)) {
            if (stack.hasNbt() && stack.getNbt().getBoolean("UpgradedEngine")) {
                return 3000;
            }
            return 2000;
        }

        if (stack.isOf(EvecualMC.BATTERY_BLOCK.asItem())) return BatteryBlockEntity.MAX_CAPACITY;
        if (stack.isOf(EvecualMC.ITEM_FABRICATOR_BLOCK.asItem())) return ItemFabricatorBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.MATERIALIZER_BLOCK.asItem())) return MaterializerBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.ELECTRIC_GRINDER_BLOCK.asItem())) return ElectricGrinderBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.ELECTRONIC_DUPER_BLOCK.asItem())) return ElectronicDuperBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.STATIONARY_TURRET_BLOCK.asItem())) return 2000;
        if (stack.isOf(EvecualMC.SOLAR_PANEL_BLOCK.asItem())) return SolarPanelBlockEntity.MAX_CAPACITY;
        if (stack.isOf(EvecualMC.ELECTRONIC_COMBINER_BLOCK.asItem())) return ElectronicCombinerBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.AUTO_PICKUP_BLOCK.asItem())) return 1000;
        if (stack.isOf(EvecualMC.CHARGER_BLOCK.asItem())) return ChargerBlockEntity.MAX_CAPACITY;
        if (stack.isOf(EvecualMC.RC_CHARGER_BLOCK.asItem())) return RcChargerBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.HELI_CHARGER_BLOCK.asItem())) return 4000;
        if (stack.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK.asItem())) return 10000;
        if (stack.isOf(EvecualMC.ELECTRIC_CHUTE_BLOCK.asItem())) return ElectricChuteBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.STORAGE_UNIT_BLOCK.asItem())) return StorageUnitBlockEntity.MAX_ENERGY;
        if (stack.isOf(EvecualMC.WIND_TURBINE_BLOCK.asItem())) return WindTurbineBlockEntity.MAX_CAPACITY;
        if (EvecualMC.ITEM_CHARGER_BLOCK != null && stack.isOf(EvecualMC.ITEM_CHARGER_BLOCK.asItem())) return ItemChargerBlockEntity.MAX_ENERGY;

        return 1000;
    }

    /**
     * Gets the current energy stored on this item.
     */
    public static long getEnergy(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasNbt()) return 0;
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return 0;

        if (nbt.contains("Energy")) {
            return nbt.getLong("Energy");
        }
        if (nbt.contains("BlockEntityTag")) {
            NbtCompound beTag = nbt.getCompound("BlockEntityTag");
            if (beTag.contains("Energy")) {
                return beTag.getLong("Energy");
            }
        }
        return 0;
    }

    /**
     * Sets the energy amount on the item stack, updating both root tag and BlockEntityTag for block placement.
     */
    public static void setEnergy(ItemStack stack, long energy) {
        if (stack.isEmpty()) return;
        long max = getMaxEnergy(stack);
        long clamped = Math.max(0, Math.min(energy, max));

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putLong("Energy", clamped);
        nbt.putInt("Energy", (int) clamped);

        if (stack.getItem() instanceof BlockItem) {
            NbtCompound beTag = stack.getOrCreateSubNbt("BlockEntityTag");
            beTag.putLong("Energy", clamped);
            beTag.putInt("Energy", (int) clamped);
        }
    }

    /**
     * Charges the item by the given amount. Returns the actual amount added.
     */
    public static long charge(ItemStack stack, long amount) {
        if (stack.isEmpty() || amount <= 0) return 0;
        long current = getEnergy(stack);
        long max = getMaxEnergy(stack);
        if (current >= max) return 0;

        long toAdd = Math.min(amount, max - current);
        setEnergy(stack, current + toAdd);
        return toAdd;
    }

    /**
     * Discharges the item by the given amount. Returns the actual amount extracted.
     */
    public static long discharge(ItemStack stack, long amount) {
        if (stack.isEmpty() || amount <= 0) return 0;
        long current = getEnergy(stack);
        if (current <= 0) return 0;

        long toExtract = Math.min(amount, current);
        setEnergy(stack, current - toExtract);
        return toExtract;
    }
}
