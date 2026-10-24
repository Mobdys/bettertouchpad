package com.example.client;

import com.example.bettertouchpad.config.TouchpadConfig;
import com.example.bettertouchpad.platform.windows.WindowsGestureHook;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterTouchpadClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("BetterTouchpad/Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Loading BetterTouchpad client configuration...");
        TouchpadConfig.getInstance().load();

        LOGGER.info("Initializing Windows gesture hook for 3 & 4-finger gestures...");
        WindowsGestureHook.start();
    }
}
