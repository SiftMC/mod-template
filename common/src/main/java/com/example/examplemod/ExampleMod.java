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
}
