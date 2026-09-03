package com.evecual.evecualmc.block;

import net.minecraft.util.StringIdentifiable;

public enum ParkingLinesPart implements StringIdentifiable {
    FRONT_LEFT("front_left"),
    FRONT_RIGHT("front_right"),
    MID_LEFT("mid_left"),
    MID_RIGHT("mid_right"),
    BACK_LEFT("back_left"),
    BACK_RIGHT("back_right");

    private final String name;

    ParkingLinesPart(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
