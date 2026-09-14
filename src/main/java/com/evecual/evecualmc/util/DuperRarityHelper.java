package com.evecual.evecualmc.util;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import com.evecual.evecualmc.EvecualMC;

import java.util.Set;

public class DuperRarityHelper {

    // Tier 1: Common / Basic Bulk Materials (15s = 300 ticks, 100 EU)
    public static final int TICKS_COMMON = 300;
    public static final int ENERGY_COMMON = 100;

    // Tier 2: Uncommon / Basic Ores & Resources (60s = 1200 ticks, 400 EU)
    public static final int TICKS_UNCOMMON = 1200;
    public static final int ENERGY_UNCOMMON = 400;

    // Tier 3: Intermediate / Mob Drops & Precious Metals (3m = 3600 ticks, 800 EU)
    public static final int TICKS_INTERMEDIATE = 3600;
    public static final int ENERGY_INTERMEDIATE = 800;

    // Tier 4: Rare / Diamonds & Emeralds (15m = 18000 ticks, 1500 EU)
    public static final int TICKS_RARE = 18000;
    public static final int ENERGY_RARE = 1500;

    // Tier 5: High-Tier / Artifacts & Beacons (25m = 30000 ticks, 2000 EU)
    public static final int TICKS_EPIC = 30000;
    public static final int ENERGY_EPIC = 2000;

    // Tier 6: Shulkers / Transport (35m = 42000 ticks, 2500 EU)
    public static final int TICKS_SHULKER = 42000;
    public static final int ENERGY_SHULKER = 2500;

    // Tier 7: End Game / Elytra & Boss Trophies (60m = 72000 ticks, 3000 EU)
    public static final int TICKS_END_GAME = 72000;
    public static final int ENERGY_END_GAME = 3000;

    // Tier 8: Netherite / Ultimate (75m = 90000 ticks, 3000 EU)
    public static final int TICKS_NETHERITE = 90000;
    public static final int ENERGY_NETHERITE = 3000;

    private static final Set<Item> NETHERITE_ITEMS = Set.of(
            Items.NETHERITE_INGOT,
            Items.NETHERITE_SCRAP,
            Items.NETHERITE_BLOCK,
            Items.NETHERITE_SWORD,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_AXE,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_HOE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,
            Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE
    );

    private static final Set<Item> END_GAME_ITEMS = Set.of(
            Items.ELYTRA,
            Items.DRAGON_EGG,
            Items.DRAGON_HEAD,
            Items.NETHER_STAR
    );

    private static final Set<Item> SHULKER_ITEMS = Set.of(
            Items.SHULKER_SHELL,
            Items.SHULKER_BOX,
            Items.WHITE_SHULKER_BOX,
            Items.ORANGE_SHULKER_BOX,
            Items.MAGENTA_SHULKER_BOX,
            Items.LIGHT_BLUE_SHULKER_BOX,
            Items.YELLOW_SHULKER_BOX,
            Items.LIME_SHULKER_BOX,
            Items.PINK_SHULKER_BOX,
            Items.GRAY_SHULKER_BOX,
            Items.LIGHT_GRAY_SHULKER_BOX,
            Items.CYAN_SHULKER_BOX,
            Items.PURPLE_SHULKER_BOX,
            Items.BLUE_SHULKER_BOX,
            Items.BROWN_SHULKER_BOX,
            Items.GREEN_SHULKER_BOX,
            Items.RED_SHULKER_BOX,
            Items.BLACK_SHULKER_BOX
    );

    private static final Set<Item> EPIC_ITEMS = Set.of(
            Items.BEACON,
            Items.TOTEM_OF_UNDYING,
            Items.TRIDENT,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.CONDUIT,
            Items.WITHER_SKELETON_SKULL,
            Items.DRAGON_BREATH,
            Items.HEART_OF_THE_SEA
    );

    private static final Set<Item> RARE_ITEMS = Set.of(
            Items.DIAMOND,
            Items.DIAMOND_BLOCK,
            Items.DIAMOND_ORE,
            Items.DEEPSLATE_DIAMOND_ORE,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_AXE,
            Items.DIAMOND_SHOVEL,
            Items.DIAMOND_HOE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,
            Items.DIAMOND_HORSE_ARMOR,
            Items.EMERALD,
            Items.EMERALD_BLOCK,
            Items.EMERALD_ORE,
            Items.DEEPSLATE_EMERALD_ORE,
            Items.ANCIENT_DEBRIS,
            Items.GOLDEN_APPLE,
            Items.ECHO_SHARD,
            Items.NAUTILUS_SHELL,
            Items.SPONGE,
            Items.WET_SPONGE,
            Items.MUSIC_DISC_OTHERSIDE,
            Items.MUSIC_DISC_5,
            Items.MUSIC_DISC_PIGSTEP,
            Items.MUSIC_DISC_RELIC
    );

    private static final Set<Item> INTERMEDIATE_ITEMS = Set.of(
            Items.GOLD_INGOT,
            Items.GOLD_BLOCK,
            Items.RAW_GOLD,
            Items.RAW_GOLD_BLOCK,
            Items.GOLD_ORE,
            Items.DEEPSLATE_GOLD_ORE,
            Items.NETHER_GOLD_ORE,
            Items.GOLDEN_SWORD,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_AXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_HOE,
            Items.GOLDEN_HELMET,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_BOOTS,
            Items.BLAZE_ROD,
            Items.BLAZE_POWDER,
            Items.ENDER_PEARL,
            Items.ENDER_EYE,
            Items.GHAST_TEAR,
            Items.SLIME_BALL,
            Items.SLIME_BLOCK,
            Items.MAGMA_CREAM,
            Items.MAGMA_BLOCK,
            Items.PHANTOM_MEMBRANE,
            Items.OBSIDIAN,
            Items.CRYING_OBSIDIAN,
            Items.EXPERIENCE_BOTTLE,
            Items.GOAT_HORN,
            Items.NAME_TAG,
            Items.SADDLE
    );

    private static final Set<Item> UNCOMMON_ITEMS = Set.of(
            Items.IRON_INGOT,
            Items.IRON_BLOCK,
            Items.RAW_IRON,
            Items.RAW_IRON_BLOCK,
            Items.IRON_ORE,
            Items.DEEPSLATE_IRON_ORE,
            Items.IRON_SWORD,
            Items.IRON_PICKAXE,
            Items.IRON_AXE,
            Items.IRON_SHOVEL,
            Items.IRON_HOE,
            Items.IRON_HELMET,
            Items.IRON_CHESTPLATE,
            Items.IRON_LEGGINGS,
            Items.IRON_BOOTS,
            Items.COPPER_INGOT,
            Items.COPPER_BLOCK,
            Items.RAW_COPPER,
            Items.RAW_COPPER_BLOCK,
            Items.COPPER_ORE,
            Items.DEEPSLATE_COPPER_ORE,
            Items.COAL,
            Items.COAL_BLOCK,
            Items.COAL_ORE,
            Items.DEEPSLATE_COAL_ORE,
            Items.CHARCOAL,
            Items.REDSTONE,
            Items.REDSTONE_BLOCK,
            Items.REDSTONE_ORE,
            Items.DEEPSLATE_REDSTONE_ORE,
            Items.LAPIS_LAZULI,
            Items.LAPIS_BLOCK,
            Items.LAPIS_ORE,
            Items.DEEPSLATE_LAPIS_ORE,
            Items.QUARTZ,
            Items.QUARTZ_BLOCK,
            Items.NETHER_QUARTZ_ORE,
            Items.AMETHYST_SHARD,
            Items.AMETHYST_BLOCK,
            Items.GLOWSTONE_DUST,
            Items.GLOWSTONE,
            Items.GUNPOWDER,
            Items.TNT,
            Items.LEATHER,
            Items.SUGAR_CANE,
            Items.PAPER,
            Items.BOOK,
            Items.BRICK,
            Items.BRICKS,
            Items.NETHER_BRICK,
            Items.NETHER_BRICKS,
            Items.PRISMARINE_SHARD,
            Items.PRISMARINE_CRYSTALS,
            Items.PRISMARINE,
            Items.INK_SAC,
            Items.GLOW_INK_SAC,
            Items.ARROW,
            Items.FIREWORK_ROCKET
    );

    private static final Set<Item> COMMON_ITEMS = Set.of(
            Items.DIRT,
            Items.COARSE_DIRT,
            Items.ROOTED_DIRT,
            Items.MUD,
            Items.MUD_BRICKS,
            Items.GRASS_BLOCK,
            Items.PODZOL,
            Items.MYCELIUM,
            Items.MOSS_BLOCK,
            Items.STONE,
            Items.COBBLESTONE,
            Items.MOSSY_COBBLESTONE,
            Items.COBBLED_DEEPSLATE,
            Items.DEEPSLATE,
            Items.ANDESITE,
            Items.DIORITE,
            Items.GRANITE,
            Items.TUFF,
            Items.CALCITE,
            Items.DRIPSTONE_BLOCK,
            Items.POINTED_DRIPSTONE,
            Items.SAND,
            Items.RED_SAND,
            Items.SANDSTONE,
            Items.RED_SANDSTONE,
            Items.GRAVEL,
            Items.CLAY,
            Items.CLAY_BALL,
            Items.STICK,
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.MELON_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.TORCH,
            Items.LADDER,
            Items.SCAFFOLDING,
            Items.WHEAT,
            Items.CARROT,
            Items.POTATO,
            Items.BEETROOT,
            Items.PUMPKIN,
            Items.MELON,
            Items.MELON_SLICE,
            Items.APPLE,
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.OAK_SAPLING,
            Items.SPRUCE_SAPLING,
            Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING,
            Items.ACACIA_SAPLING,
            Items.DARK_OAK_SAPLING,
            Items.MANGROVE_PROPAGULE,
            Items.CHERRY_SAPLING,
            Items.OAK_LOG,
            Items.SPRUCE_LOG,
            Items.BIRCH_LOG,
            Items.JUNGLE_LOG,
            Items.ACACIA_LOG,
            Items.DARK_OAK_LOG,
            Items.MANGROVE_LOG,
            Items.CHERRY_LOG,
            Items.BAMBOO_BLOCK,
            Items.BAMBOO,
            Items.OAK_PLANKS,
            Items.SPRUCE_PLANKS,
            Items.BIRCH_PLANKS,
            Items.JUNGLE_PLANKS,
            Items.ACACIA_PLANKS,
            Items.DARK_OAK_PLANKS,
            Items.MANGROVE_PLANKS,
            Items.CHERRY_PLANKS,
            Items.BAMBOO_PLANKS,
            Items.ROTTEN_FLESH,
            Items.BONE,
            Items.BONE_MEAL,
            Items.FEATHER,
            Items.STRING,
            Items.SNOWBALL,
            Items.SNOW_BLOCK,
            Items.ICE,
            Items.DIRT_PATH,
            Items.GLASS,
            Items.GLASS_PANE,
            Items.CRAFTING_TABLE,
            Items.FURNACE,
            Items.CHEST,
            Items.BARREL
    );

    public static boolean isDuplicable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();

        // 1. Dupers (cannot duplicate duplicating machines)
        if (stack.isOf(EvecualMC.ELECTRONIC_DUPER_ITEM)) return false;
        if (item instanceof BlockItem bi && bi.getBlock() == EvecualMC.ELECTRONIC_DUPER_BLOCK) return false;

        // 2. Lightning item
        if (stack.isOf(EvecualMC.LIGHTNING_ITEM)) return false;

        // 3. Zappers
        if (stack.isOf(EvecualMC.ELECTRONIC_ZAPPER)) return false;

        // 4. RC Vehicles & full vehicles
        if (stack.isOf(EvecualMC.RC_CAR_ITEM)) return false;
        if (stack.isOf(EvecualMC.RC_DRONE_ITEM)) return false;
        if (stack.isOf(EvecualMC.RC_ROBOT_ITEM)) return false;
        if (stack.isOf(EvecualMC.PICKUP_DRONE_ITEM)) return false;
        if (stack.isOf(EvecualMC.CAR_ITEM)) return false;
        if (stack.isOf(EvecualMC.HELI_ITEM)) return false;

        // 5. Railgun
        if (EvecualMC.RAILGUN_ITEM != null && stack.isOf(EvecualMC.RAILGUN_ITEM)) return false;

        return true;
    }

    public static int getRequiredTicks(ItemStack stack) {
        if (stack.isEmpty() || !isDuplicable(stack)) return TICKS_COMMON;
        Item item = stack.getItem();

        if (NETHERITE_ITEMS.contains(item)) return TICKS_NETHERITE;
        if (END_GAME_ITEMS.contains(item)) return TICKS_END_GAME;
        if (SHULKER_ITEMS.contains(item)) return TICKS_SHULKER;
        if (EPIC_ITEMS.contains(item)) return TICKS_EPIC;
        if (RARE_ITEMS.contains(item)) return TICKS_RARE;
        if (INTERMEDIATE_ITEMS.contains(item)) return TICKS_INTERMEDIATE;
        if (UNCOMMON_ITEMS.contains(item)) return TICKS_UNCOMMON;
        if (COMMON_ITEMS.contains(item)) return TICKS_COMMON;

        // Fallback based on vanilla Rarity enum
        Rarity rarity = stack.getRarity();
        return switch (rarity) {
            case EPIC -> TICKS_END_GAME;
            case RARE -> TICKS_RARE;
            case UNCOMMON -> TICKS_INTERMEDIATE;
            case COMMON -> (item instanceof BlockItem) ? TICKS_COMMON : TICKS_UNCOMMON;
        };
    }

    public static int getRequiredEnergy(ItemStack stack) {
        if (stack.isEmpty()) return ENERGY_COMMON;
        Item item = stack.getItem();

        if (NETHERITE_ITEMS.contains(item)) return ENERGY_NETHERITE;
        if (END_GAME_ITEMS.contains(item)) return ENERGY_END_GAME;
        if (SHULKER_ITEMS.contains(item)) return ENERGY_SHULKER;
        if (EPIC_ITEMS.contains(item)) return ENERGY_EPIC;
        if (RARE_ITEMS.contains(item)) return ENERGY_RARE;
        if (INTERMEDIATE_ITEMS.contains(item)) return ENERGY_INTERMEDIATE;
        if (UNCOMMON_ITEMS.contains(item)) return ENERGY_UNCOMMON;
        if (COMMON_ITEMS.contains(item)) return ENERGY_COMMON;

        Rarity rarity = stack.getRarity();
        return switch (rarity) {
            case EPIC -> ENERGY_END_GAME;
            case RARE -> ENERGY_RARE;
            case UNCOMMON -> ENERGY_INTERMEDIATE;
            case COMMON -> (item instanceof BlockItem) ? ENERGY_COMMON : ENERGY_UNCOMMON;
        };
    }

    public static String getRarityLabel(ItemStack stack) {
        if (stack.isEmpty()) return "§7Basic";
        if (!isDuplicable(stack)) return "§c§lNon-Duplicable (Protected)";
        Item item = stack.getItem();

        if (NETHERITE_ITEMS.contains(item)) return "§4§lMythic (Netherite)";
        if (END_GAME_ITEMS.contains(item)) return "§6§lEnd-Game (Legendary)";
        if (SHULKER_ITEMS.contains(item)) return "§d§lShulker Class";
        if (EPIC_ITEMS.contains(item)) return "§5§lEpic Artifact";
        if (RARE_ITEMS.contains(item)) return "§b§lRare / Gem";
        if (INTERMEDIATE_ITEMS.contains(item)) return "§eIntermediate";
        if (UNCOMMON_ITEMS.contains(item)) return "§aUncommon";
        if (COMMON_ITEMS.contains(item)) return "§7Common";

        Rarity rarity = stack.getRarity();
        return switch (rarity) {
            case EPIC -> "§5§lEpic";
            case RARE -> "§b§lRare";
            case UNCOMMON -> "§aUncommon";
            case COMMON -> "§7Common";
        };
    }

    public static String formatDuration(int ticks) {
        int totalSec = Math.max(0, ticks / 20);
        int hours = totalSec / 3600;
        int mins = (totalSec % 3600) / 60;
        int secs = totalSec % 60;

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, mins, secs);
        } else if (mins > 0) {
            return String.format("%dm %02ds", mins, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
