package com.example.examplemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private ExampleMod() {
    }

    /** Called once by every loader's entrypoint. */
    public static void init() {
        LOGGER.info("Hello from {}", MOD_ID);
    }

    /** Minecraft moved this more than once. Stonecutter keeps the branch that fits the version being built. */
    public static String minecraftVersion() {
        //? if >=1.21.6 {
        return net.minecraft.SharedConstants.getCurrentVersion().name();
        //?} elif >=1.14 {
        /*return net.minecraft.SharedConstants.getCurrentVersion().getName();
        *///?} else {
        /*return net.minecraft.realms.RealmsSharedConstants.VERSION_STRING;
        *///?}
    }
}
