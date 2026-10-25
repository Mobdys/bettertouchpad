package com.example.bettertouchpad.config;

import net.minecraft.network.chat.Component;

public enum TouchpadAction {
    LEFT_CLICK("left_click", 1, true),
    RIGHT_CLICK("right_click", 3, true),
    MIDDLE_CLICK("middle_click", 2, true),
    MOUSE_4("mouse_4", 4, true),
    MOUSE_5("mouse_5", 5, true),
    SPRINT("sprint", -1, false),
    SNEAK("sneak", -2, false),
    JUMP("jump", -3, false),
    DROP_ITEM("drop_item", -4, false),
    INVENTORY("inventory", -5, false),
    PERSPECTIVE("perspective", -6, false),
    HOTBAR_NEXT("hotbar_next", -7, false),
    HOTBAR_PREV("hotbar_prev", -8, false),
    ZOOM("zoom", -9, false),
    LOOK_NORMAL("look_normal", 0, false),
    LOOK_SMOOTH("look_smooth", 0, false),
    HOTBAR_CYCLE("hotbar_cycle", 0, false),
    NONE("none", 0, false);

    private final String id;
    private final int mouseButton; // SDL button ID (1=Left, 2=Middle, 3=Right, 4=X1, 5=X2)
    private final boolean isMouseButton;

    TouchpadAction(String id, int mouseButton, boolean isMouseButton) {
        this.id = id;
        this.mouseButton = mouseButton;
        this.isMouseButton = isMouseButton;
    }

    public String getId() {
        return id;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public boolean isMouseButton() {
        return isMouseButton;
    }

    public Component getDisplayName() {
        return Component.translatable("bettertouchpad.action." + id);
    }

    public Component getDescription() {
        return Component.translatable("bettertouchpad.action." + id + ".desc");
    }
}
