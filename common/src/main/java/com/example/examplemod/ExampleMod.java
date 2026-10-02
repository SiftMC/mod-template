package com.example.examplemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /** Every loader's entrypoint calls this once. */
    public static void init() {
        LOGGER.info("Hello from {}", MOD_ID);
    }

    public static String greeting(String brand, String minecraftVersion) {
        return "Hello from " + MOD_ID + " on " + brand + " " + minecraftVersion + "!";
    }
}
