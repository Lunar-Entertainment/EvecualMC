package com.evecual.evecualmc.block;

import net.minecraft.util.StringIdentifiable;

public enum HeliChargerPart implements StringIdentifiable {
    CENTER("center"),
    NORTH("north"),
    SOUTH("south"),
    WEST("west"),
    EAST("east"),
    NORTH_WEST("north_west"),
    NORTH_EAST("north_east"),
    SOUTH_WEST("south_west"),
    SOUTH_EAST("south_east");

    private final String name;

    HeliChargerPart(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
