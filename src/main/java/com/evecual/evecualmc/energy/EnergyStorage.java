package com.evecual.evecualmc.energy;

public interface EnergyStorage {
    long getEnergy();
    long getMaxEnergy();
    long insertEnergy(long amount, boolean simulate);
    long extractEnergy(long amount, boolean simulate);
}
