package com.evecual.evecualmc.recipe;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TintedGlassBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum CombinerRecipe {
    ELECTRIC_CAR(
            Category.VEHICLES,
            "electric_car",
            "Electric Car",
            "Full-sized drivable EV with custom paint, glass tint, and trunk capacity.",
            () -> new ItemStack(EvecualMC.CAR_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("Electric Engine", 1, false,
                            s -> s.isOf(EvecualMC.ENGINE) || s.isOf(EvecualMC.UPGRADED_ENGINE),
                            () -> new ItemStack(EvecualMC.ENGINE)),
                    new SlotRequirement("Steel Chassis", 4, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Windshield Glass", 1, false,
                            s -> isGlassItem(s.getItem()),
                            () -> new ItemStack(Items.GLASS)),
                    new SlotRequirement("Leather Interior", 2, false,
                            s -> s.isOf(Items.LEATHER),
                            () -> new ItemStack(Items.LEATHER)),
                    new SlotRequirement("Body Paint Dye", 1, true,
                            s -> s.getItem() instanceof DyeItem,
                            () -> new ItemStack(Items.RED_DYE)),
                    new SlotRequirement("Trunk Upgrade", 1, true,
                            s -> s.isOf(EvecualMC.TRUNK_UPGRADE) || s.isOf(Items.CHEST),
                            () -> new ItemStack(EvecualMC.TRUNK_UPGRADE))
            }
    ) {
        @Override
        public ItemStack createOutput(List<ItemStack> inputs) {
            ItemStack engine = inputs.get(0);
            ItemStack glass = inputs.get(2);
            ItemStack color = inputs.get(4);
            ItemStack trunk = inputs.get(5);

            boolean isUpgradedEngine = engine.isOf(EvecualMC.UPGRADED_ENGINE);
            int glassColor = getGlassColorFromItem(glass.getItem());
            int carColor = getCarColorFromDye(color);
            int trunkTier = (!trunk.isEmpty() && (trunk.isOf(EvecualMC.TRUNK_UPGRADE) || trunk.isOf(Items.CHEST))) ? 1 : 0;

            ItemStack car = new ItemStack(EvecualMC.CAR_ITEM, 1);
            NbtCompound nbt = car.getOrCreateNbt();
            nbt.putInt("ColorVariant", carColor);
            nbt.putInt("GlassColor", glassColor);
            nbt.putBoolean("UpgradedEngine", isUpgradedEngine);
            nbt.putInt("TrunkTier", trunkTier);
            return car;
        }
    },

    RC_CAR(
            Category.RC,
            "rc_car",
            "RC Car",
            "High-speed remote ground rover with responsive suspension.",
            () -> new ItemStack(EvecualMC.RC_CAR_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("RC Sender", 1, false,
                            s -> s.isOf(EvecualMC.RC_SENDER_ITEM),
                            () -> new ItemStack(EvecualMC.RC_SENDER_ITEM)),
                    new SlotRequirement("Electric Engine", 1, false,
                            s -> s.isOf(EvecualMC.ENGINE) || s.isOf(EvecualMC.UPGRADED_ENGINE),
                            () -> new ItemStack(EvecualMC.ENGINE)),
                    new SlotRequirement("Steel Frame", 2, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Tread Wheels", 2, false,
                            s -> s.isOf(Items.LEATHER) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(Items.LEATHER)),
                    new SlotRequirement("Battery Cell", 1, false,
                            s -> s.isOf(EvecualMC.BATTERY_ITEM) || s.isOf(Items.REDSTONE),
                            () -> new ItemStack(EvecualMC.BATTERY_ITEM)),
                    new SlotRequirement("Shell Dye", 1, true,
                            s -> s.getItem() instanceof DyeItem,
                            () -> new ItemStack(Items.CYAN_DYE))
            }
    ),

    RC_DRONE(
            Category.RC,
            "rc_drone",
            "RC Drone",
            "Agile quadcopter capable of autonomous hover and 512m flight.",
            () -> new ItemStack(EvecualMC.RC_DRONE_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("RC Sender", 1, false,
                            s -> s.isOf(EvecualMC.RC_SENDER_ITEM),
                            () -> new ItemStack(EvecualMC.RC_SENDER_ITEM)),
                    new SlotRequirement("High-RPM Motor", 1, false,
                            s -> s.isOf(EvecualMC.ENGINE) || s.isOf(EvecualMC.UPGRADED_ENGINE),
                            () -> new ItemStack(EvecualMC.ENGINE)),
                    new SlotRequirement("Carbon/Steel Frame", 2, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Propellers (x4)", 4, false,
                            s -> s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(Items.IRON_INGOT)),
                    new SlotRequirement("Battery Cell", 1, false,
                            s -> s.isOf(EvecualMC.BATTERY_ITEM) || s.isOf(Items.REDSTONE),
                            () -> new ItemStack(EvecualMC.BATTERY_ITEM)),
                    new SlotRequirement("Gyro Stabilizer", 1, true,
                            s -> s.isOf(Items.ENDER_PEARL) || s.isOf(EvecualMC.WIRE_ITEM),
                            () -> new ItemStack(Items.ENDER_PEARL))
            }
    ),

    RC_ROBOT(
            Category.RC,
            "rc_robot",
            "RC Robot",
            "Tracked excavation & utility droid with tool mount and cargo hold.",
            () -> new ItemStack(EvecualMC.RC_ROBOT_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("RC Sender", 1, false,
                            s -> s.isOf(EvecualMC.RC_SENDER_ITEM),
                            () -> new ItemStack(EvecualMC.RC_SENDER_ITEM)),
                    new SlotRequirement("Heavy Engine", 1, false,
                            s -> s.isOf(EvecualMC.ENGINE) || s.isOf(EvecualMC.UPGRADED_ENGINE),
                            () -> new ItemStack(EvecualMC.ENGINE)),
                    new SlotRequirement("Steel Chassis", 4, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Cargo Hold", 2, false,
                            s -> s.isOf(Items.CHEST),
                            () -> new ItemStack(Items.CHEST)),
                    new SlotRequirement("Battery Cell", 1, false,
                            s -> s.isOf(EvecualMC.BATTERY_ITEM) || s.isOf(Items.REDSTONE),
                            () -> new ItemStack(EvecualMC.BATTERY_ITEM)),
                    new SlotRequirement("Tool Mount Arm", 1, true,
                            s -> s.isOf(Items.DISPENSER) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(Items.DISPENSER))
            }
    ),

    RC_CONTROLLER(
            Category.RC,
            "rc_controller",
            "RC Controller",
            "Universal handheld transmitter capable of linking with all RC vehicles.",
            () -> new ItemStack(EvecualMC.RC_CONTROLLER_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("RC Receiver", 1, false,
                            s -> s.isOf(EvecualMC.RC_RECEIVER_ITEM),
                            () -> new ItemStack(EvecualMC.RC_RECEIVER_ITEM)),
                    new SlotRequirement("Casing Shell", 2, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Monitor Screen", 1, false,
                            s -> isGlassItem(s.getItem()),
                            () -> new ItemStack(Items.GLASS_PANE)),
                    new SlotRequirement("Transmitter Logic", 2, false,
                            s -> s.isOf(Items.REDSTONE),
                            () -> new ItemStack(Items.REDSTONE)),
                    new SlotRequirement("Antenna & Wire", 2, false,
                            s -> s.isOf(EvecualMC.WIRE_ITEM) || s.isOf(Items.COPPER_INGOT),
                            () -> new ItemStack(EvecualMC.WIRE_ITEM)),
                    new SlotRequirement("Analog Sticks", 2, true,
                            s -> s.isOf(Items.LEVER) || s.isOf(Items.STONE_BUTTON),
                            () -> new ItemStack(Items.LEVER))
            }
    ),

    STATIONARY_RC_CONTROLLER(
            Category.RC,
            "stationary_rc_controller",
            "Stationary RC Controller",
            "Ground-mounted teleoperation terminal. Locks player in place with continuous RC vehicle telemetry.",
            () -> new ItemStack(EvecualMC.STATIONARY_RC_CONTROLLER_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("RC Controller", 1, false,
                            s -> s.isOf(EvecualMC.RC_CONTROLLER_ITEM),
                            () -> new ItemStack(EvecualMC.RC_CONTROLLER_ITEM)),
                    new SlotRequirement("Terminal Stand", 3, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Monitor Screen", 1, false,
                            s -> isGlassItem(s.getItem()),
                            () -> new ItemStack(Items.GLASS_PANE)),
                    new SlotRequirement("Relay Circuit", 2, false,
                            s -> s.isOf(Items.REDSTONE),
                            () -> new ItemStack(Items.REDSTONE)),
                    new SlotRequirement("Terminal Pedestal", 1, false,
                            s -> s.isOf(Items.SMOOTH_STONE) || s.isOf(Items.STONE) || s.isOf(Items.COBBLESTONE),
                            () -> new ItemStack(Items.SMOOTH_STONE)),
                    new SlotRequirement("Signal Booster", 2, true,
                            s -> s.isOf(EvecualMC.WIRE_ITEM) || s.isOf(Items.COPPER_INGOT),
                            () -> new ItemStack(EvecualMC.WIRE_ITEM))
            }
    ),

    RC_SENDER(
            Category.RC,
            "rc_sender",
            "RC Sender",
            "Long-range telecommand antenna and wireless telemetry transceiver.",
            () -> new ItemStack(EvecualMC.RC_SENDER_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("Broadcast Antenna", 1, false,
                            s -> s.isOf(Items.LIGHTNING_ROD) || s.isOf(Items.COPPER_INGOT),
                            () -> new ItemStack(Items.LIGHTNING_ROD)),
                    new SlotRequirement("Signal Logic", 2, false,
                            s -> s.isOf(Items.REDSTONE),
                            () -> new ItemStack(Items.REDSTONE)),
                    new SlotRequirement("Steel Shield", 1, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("RF Inductor Coil", 1, false,
                            s -> s.isOf(EvecualMC.WIRE_ITEM),
                            () -> new ItemStack(EvecualMC.WIRE_ITEM)),
                    null,
                    null
            }
    ),

    RC_RECEIVER(
            Category.RC,
            "rc_receiver",
            "RC Receiver",
            "Precision command decoder and multi-channel crystal receiver.",
            () -> new ItemStack(EvecualMC.RC_RECEIVER_ITEM),
            new SlotRequirement[]{
                    new SlotRequirement("Signal Decoder", 1, false,
                            s -> s.isOf(Items.COMPARATOR) || s.isOf(Items.REPEATER),
                            () -> new ItemStack(Items.COMPARATOR)),
                    new SlotRequirement("Crystal Oscillator", 1, false,
                            s -> s.isOf(Items.QUARTZ),
                            () -> new ItemStack(Items.QUARTZ)),
                    new SlotRequirement("Ground Shield", 1, false,
                            s -> s.isOf(EvecualMC.STEEL_INGOT) || s.isOf(Items.IRON_INGOT),
                            () -> new ItemStack(EvecualMC.STEEL_INGOT)),
                    new SlotRequirement("Copper Bus", 1, false,
                            s -> s.isOf(EvecualMC.WIRE_ITEM),
                            () -> new ItemStack(EvecualMC.WIRE_ITEM)),
                    null,
                    null
            }
    );

    public enum Category {
        VEHICLES("Vehicles", "🚗 Ridable Vehicles", "Assemble full-size drivable electric road vehicles"),
        RC("RC", "📡 Radio Control (RC)", "Craft drones, rovers, controllers & micro-transceivers");

        private final String id;
        private final String title;
        private final String description;

        Category(String id, String title, String description) {
            this.id = id;
            this.title = title;
            this.description = description;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    public static class SlotRequirement {
        private final String name;
        private final int requiredCount;
        private final boolean optional;
        private final Predicate<ItemStack> validator;
        private final Supplier<ItemStack> displayIcon;

        public SlotRequirement(String name, int requiredCount, boolean optional,
                               Predicate<ItemStack> validator, Supplier<ItemStack> displayIcon) {
            this.name = name;
            this.requiredCount = requiredCount;
            this.optional = optional;
            this.validator = validator;
            this.displayIcon = displayIcon;
        }

        public String getName() { return name; }
        public int getRequiredCount() { return requiredCount; }
        public boolean isOptional() { return optional; }
        public boolean isValid(ItemStack stack) { return !stack.isEmpty() && validator.test(stack); }
        public ItemStack getDisplayIcon() { return displayIcon != null ? displayIcon.get() : ItemStack.EMPTY; }
    }

    private final Category category;
    private final String id;
    private final String displayName;
    private final String description;
    private final Supplier<ItemStack> outputSupplier;
    private final SlotRequirement[] slotRequirements;

    CombinerRecipe(Category category, String id, String displayName, String description,
                   Supplier<ItemStack> outputSupplier, SlotRequirement[] slotRequirements) {
        this.category = category;
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.outputSupplier = outputSupplier;
        this.slotRequirements = slotRequirements;
    }

    public Category getCategory() { return category; }
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ItemStack getOutputTemplate() { return outputSupplier.get(); }

    public SlotRequirement getSlotRequirement(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < slotRequirements.length) {
            return slotRequirements[slotIndex];
        }
        return null;
    }

    public boolean isValidInput(int slotIndex, ItemStack stack) {
        SlotRequirement req = getSlotRequirement(slotIndex);
        if (req == null) return false;
        return req.isValid(stack);
    }

    public boolean canCraft(List<ItemStack> inputs) {
        for (int i = 0; i < slotRequirements.length; i++) {
            SlotRequirement req = slotRequirements[i];
            if (req == null) continue;
            ItemStack current = i < inputs.size() ? inputs.get(i) : ItemStack.EMPTY;
            if (!req.isOptional()) {
                if (current.isEmpty() || !req.isValid(current) || current.getCount() < req.getRequiredCount()) {
                    return false;
                }
            } else if (!current.isEmpty() && !req.isValid(current)) {
                return false;
            }
        }
        return true;
    }

    public ItemStack createOutput(List<ItemStack> inputs) {
        return getOutputTemplate().copy();
    }

    public void consumeInputs(List<ItemStack> inputs) {
        for (int i = 0; i < slotRequirements.length; i++) {
            SlotRequirement req = slotRequirements[i];
            if (req == null) continue;
            ItemStack current = i < inputs.size() ? inputs.get(i) : ItemStack.EMPTY;
            if (!current.isEmpty() && req.isValid(current)) {
                int toConsume = Math.min(current.getCount(), req.getRequiredCount());
                current.decrement(toConsume);
            }
        }
    }

    public static List<CombinerRecipe> getRecipesForCategory(Category cat) {
        List<CombinerRecipe> list = new ArrayList<>();
        for (CombinerRecipe r : values()) {
            if (r.getCategory() == cat) list.add(r);
        }
        return list;
    }

    public static CombinerRecipe getRecipeByIndex(int index) {
        if (index > 0 && index <= values().length) {
            return values()[index - 1];
        }
        return null;
    }

    public static int getIndexForRecipe(CombinerRecipe recipe) {
        if (recipe == null) return 0;
        return recipe.ordinal() + 1;
    }

    public static CombinerRecipe getRecipeById(String id) {
        if (id == null) return null;
        for (CombinerRecipe r : values()) {
            if (r.getId().equalsIgnoreCase(id)) return r;
        }
        return null;
    }

    private static boolean isGlassItem(Item item) {
        if (item == Items.GLASS || item == Items.GLASS_PANE || item == Items.TINTED_GLASS) return true;
        if (item instanceof BlockItem bi) {
            Block b = bi.getBlock();
            return b instanceof GlassBlock || b instanceof StainedGlassBlock || b instanceof StainedGlassPaneBlock || b instanceof TintedGlassBlock;
        }
        return false;
    }

    public static int getGlassColorFromItem(Item item) {
        if (item == Items.TINTED_GLASS) return 1;
        if (item == Items.WHITE_STAINED_GLASS || item == Items.WHITE_STAINED_GLASS_PANE) return 2;
        if (item == Items.LIGHT_GRAY_STAINED_GLASS || item == Items.GRAY_STAINED_GLASS) return 3;
        if (item == Items.BLACK_STAINED_GLASS || item == Items.BLACK_STAINED_GLASS_PANE) return 1;
        if (item == Items.RED_STAINED_GLASS || item == Items.RED_STAINED_GLASS_PANE) return 4;
        if (item == Items.ORANGE_STAINED_GLASS || item == Items.ORANGE_STAINED_GLASS_PANE) return 5;
        if (item == Items.YELLOW_STAINED_GLASS || item == Items.YELLOW_STAINED_GLASS_PANE) return 6;
        if (item == Items.LIME_STAINED_GLASS || item == Items.LIME_STAINED_GLASS_PANE || item == Items.GREEN_STAINED_GLASS) return 7;
        if (item == Items.CYAN_STAINED_GLASS || item == Items.LIGHT_BLUE_STAINED_GLASS) return 8;
        if (item == Items.BLUE_STAINED_GLASS || item == Items.BLUE_STAINED_GLASS_PANE) return 9;
        if (item == Items.PURPLE_STAINED_GLASS || item == Items.MAGENTA_STAINED_GLASS) return 10;
        if (item == Items.PINK_STAINED_GLASS || item == Items.PINK_STAINED_GLASS_PANE) return 11;
        return 0;
    }

    public static int getCarColorFromDye(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        if (item == Items.BLUE_DYE || item == Items.CYAN_DYE || item == Items.LIGHT_BLUE_DYE) return 1;
        if (item == Items.BLACK_DYE || item == Items.GRAY_DYE || item == Items.LIGHT_GRAY_DYE) return 2;
        if (item == Items.LIME_DYE || item == Items.GREEN_DYE) return 3;
        if (item == Items.WHITE_DYE) return 4;
        if (item == Items.YELLOW_DYE || item == Items.ORANGE_DYE) return 5;
        return 0;
    }
}
