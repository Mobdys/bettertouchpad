package com.example.bettertouchpad.config;

import net.minecraft.network.chat.Component;

public enum TouchpadGesture {
    SINGLE_CLICK("single_click"),
    ONE_FINGER_SWIPE("one_finger_swipe"),
    TWO_FINGER_CLICK("two_finger_click"),
    THREE_FINGER_CLICK("three_finger_click"),
    THREE_FINGER_SWIPE("three_finger_swipe"),
    FOUR_FINGER_TAP("four_finger_tap"),
    FOUR_FINGER_SWIPE("four_finger_swipe"),
    DOUBLE_CLICK("double_click");

    private final String id;

    TouchpadGesture(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return Component.translatable("bettertouchpad.gesture." + id);
    }

    public Component getDescription() {
        return Component.translatable("bettertouchpad.gesture." + id + ".desc");
    }
}
