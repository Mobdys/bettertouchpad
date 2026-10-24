package com.example.bettertouchpad.config;

import net.minecraft.network.chat.Component;

public enum ScrollAction {
    HOTBAR("hotbar"),
    INVERTED_HOTBAR("inverted_hotbar"),
    ZOOM("zoom"),
    NONE("none");

    private final String id;

    ScrollAction(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return Component.translatable("bettertouchpad.scroll_action." + id);
    }

    public Component getDescription() {
        return Component.translatable("bettertouchpad.scroll_action." + id + ".desc");
    }
}
