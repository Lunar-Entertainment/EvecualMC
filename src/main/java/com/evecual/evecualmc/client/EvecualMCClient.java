package com.evecual.evecualmc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EvecualMCClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("evecualmc-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("EvecualMC client initialized.");
    }
}
