package com.example.examplemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ExampleMod() {
    }

    /** Called once by every loader's entrypoint. */
    public static void init() {
        LOGGER.info("Hello from {}", MOD_ID);
    }
}
