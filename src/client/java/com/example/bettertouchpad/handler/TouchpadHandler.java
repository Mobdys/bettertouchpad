package com.example.bettertouchpad.handler;

import com.example.bettertouchpad.config.ScrollAction;
import com.example.bettertouchpad.config.TouchpadAction;
import com.example.bettertouchpad.config.TouchpadConfig;
import com.example.bettertouchpad.config.TouchpadGesture;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonInfo;

public class TouchpadHandler {
    private static long lastButton1PressTime = 0L;
    private static boolean lastClickWasDouble = false;
    private static boolean zoomActive = false;

    // State for 1-finger swipe look and gestures
    private static double smoothLookX = 0.0;
    private static double smoothLookY = 0.0;
    private static double swipeXAccumulator = 0.0;
    private static double swipeYAccumulator = 0.0;

    public static boolean handleButtonEvent(long handle, MouseButtonInfo rawButtonInfo, int action) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        boolean inScreen = (client.gui.screen() != null);
        boolean pressed = (action == 1);

        // In GUI screen, if tapToClick is enabled and user left-clicks/taps, allow normal menu clicking
        if (inScreen && config.isTapToClick() && rawButtonInfo.button() == 1) {
            return false;
        }

        TouchpadAction targetAction = TouchpadAction.NONE;

        if (rawButtonInfo.button() == 1) {
            long now = System.currentTimeMillis();
            if (pressed) {
                if (now - lastButton1PressTime < 280L) {
                    lastClickWasDouble = true;
                    targetAction = config.getDoubleClickAction();
                } else {
                    lastClickWasDouble = false;
                    targetAction = config.getSingleClickAction();
                }
                lastButton1PressTime = now;
            } else {
                targetAction = lastClickWasDouble ? config.getDoubleClickAction() : config.getSingleClickAction();
            }
        } else if (rawButtonInfo.button() == 3) {
            targetAction = config.getTwoFingerClickAction();
        } else if (rawButtonInfo.button() == 2) {
            targetAction = config.getThreeFingerClickAction();
        } else {
            return false;
        }

        if (targetAction == TouchpadAction.NONE) {
            return true; // Cancel event
        }

        if (!targetAction.isMouseButton()) {
            executeGameAction(client, targetAction, pressed);
            return true; // Cancel mouse click event
        }

        return false; // Let normal mouse processing continue with remapped button
    }

    public static MouseButtonInfo remapMouseButton(MouseButtonInfo original) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) {
            return original;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null && config.isTapToClick() && original.button() == 1) {
            return original;
        }

        TouchpadAction targetAction;
        if (original.button() == 1) {
            targetAction = lastClickWasDouble ? config.getDoubleClickAction() : config.getSingleClickAction();
        } else if (original.button() == 3) {
            targetAction = config.getTwoFingerClickAction();
        } else if (original.button() == 2) {
            targetAction = config.getThreeFingerClickAction();
        } else {
            return original;
        }

        if (targetAction.isMouseButton() && targetAction.getMouseButton() > 0 && targetAction.getMouseButton() != original.button()) {
            return new MouseButtonInfo(targetAction.getMouseButton(), original.modifiers());
        }

        return original;
    }

    public static void triggerGesture(TouchpadGesture gesture) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) return;

        TouchpadAction action = config.getActionForGesture(gesture);
        if (action == TouchpadAction.NONE) return;

        Minecraft client = Minecraft.getInstance();
        if (action.isMouseButton()) {
            int btn = action.getMouseButton();
            if (btn > 0 && client.mouseHandler != null && client.getWindow() != null) {
                long windowHandle = client.getWindow().handle();
                MouseButtonInfo info = new MouseButtonInfo(btn, 0);
                client.mouseHandler.onButton(windowHandle, info, 1);
                client.mouseHandler.onButton(windowHandle, info, 0);
            }
        } else {
            executeInstantGameAction(client, action);
        }
    }

    public static double modifyLookX(double xrel) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) return xrel;

        TouchpadAction action = config.getOneFingerSwipeAction();
        if (action == TouchpadAction.NONE) {
            return 0.0;
        }

        if (action == TouchpadAction.HOTBAR_CYCLE) {
            swipeXAccumulator += xrel;
            if (swipeXAccumulator >= 35.0) {
                swipeXAccumulator = 0.0;
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    int slot = (client.player.getInventory().getSelectedSlot() + 1) % 9;
                    client.player.getInventory().setSelectedSlot(slot);
                }
            } else if (swipeXAccumulator <= -35.0) {
                swipeXAccumulator = 0.0;
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    int slot = client.player.getInventory().getSelectedSlot() - 1;
                    if (slot < 0) slot = 8;
                    client.player.getInventory().setSelectedSlot(slot);
                }
            }
            return 0.0;
        }

        if (action != TouchpadAction.LOOK_NORMAL && action != TouchpadAction.LOOK_SMOOTH) {
            swipeXAccumulator += xrel;
            if (Math.abs(swipeXAccumulator) >= 60.0) {
                swipeXAccumulator = 0.0;
                Minecraft client = Minecraft.getInstance();
                executeInstantGameAction(client, action);
            }
            return 0.0;
        }

        double sens = config.getTouchpadSensitivity();
        double delta = xrel * sens;

        if (action == TouchpadAction.LOOK_SMOOTH || config.isSmoothLook()) {
            smoothLookX = smoothLookX * 0.65 + delta * 0.35;
            return smoothLookX;
        }

        return delta;
    }

    public static double modifyLookY(double yrel) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) return yrel;

        TouchpadAction action = config.getOneFingerSwipeAction();
        if (action == TouchpadAction.NONE || action == TouchpadAction.HOTBAR_CYCLE) {
            return 0.0;
        }

        if (action != TouchpadAction.LOOK_NORMAL && action != TouchpadAction.LOOK_SMOOTH) {
            swipeYAccumulator += yrel;
            if (Math.abs(swipeYAccumulator) >= 60.0) {
                swipeYAccumulator = 0.0;
                Minecraft client = Minecraft.getInstance();
                executeInstantGameAction(client, action);
            }
            return 0.0;
        }

        double sens = config.getTouchpadSensitivity();
        double delta = yrel * sens;

        if (action == TouchpadAction.LOOK_SMOOTH || config.isSmoothLook()) {
            smoothLookY = smoothLookY * 0.65 + delta * 0.35;
            return smoothLookY;
        }

        return delta;
    }

    public static double modifyLookDelta(double delta) {
        return modifyLookX(delta);
    }

    private static void executeInstantGameAction(Minecraft client, TouchpadAction action) {
        if (client.options == null) return;
        switch (action) {
            case SPRINT -> {
                boolean current = client.options.keySprint.isDown();
                client.options.keySprint.setDown(!current);
            }
            case SNEAK -> {
                boolean current = client.options.keyShift.isDown();
                client.options.keyShift.setDown(!current);
            }
            case JUMP -> {
                client.options.keyJump.setDown(true);
                client.execute(() -> client.options.keyJump.setDown(false));
            }
            case DROP_ITEM -> {
                client.options.keyDrop.setDown(true);
                KeyMapping.click(client.options.keyDrop.getDefaultKey());
                client.options.keyDrop.setDown(false);
            }
            case INVENTORY -> {
                if (client.player != null) {
                    if (client.gui.screen() == null) {
                        client.gui.setScreen(new InventoryScreen(client.player));
                    } else if (client.gui.screen() instanceof InventoryScreen) {
                        client.gui.setScreen(null);
                    }
                }
            }
            case PERSPECTIVE -> {
                CameraType next = client.options.getCameraType().cycle();
                client.options.setCameraType(next);
            }
            case HOTBAR_NEXT -> {
                if (client.player != null) {
                    int slot = (client.player.getInventory().getSelectedSlot() + 1) % 9;
                    client.player.getInventory().setSelectedSlot(slot);
                }
            }
            case HOTBAR_PREV -> {
                if (client.player != null) {
                    int slot = client.player.getInventory().getSelectedSlot() - 1;
                    if (slot < 0) slot = 8;
                    client.player.getInventory().setSelectedSlot(slot);
                }
            }
            case ZOOM -> {
                zoomActive = !zoomActive;
            }
            default -> {}
        }
    }

    private static void executeGameAction(Minecraft client, TouchpadAction action, boolean pressed) {
        if (client.options == null) return;

        switch (action) {
            case SPRINT -> client.options.keySprint.setDown(pressed);
            case SNEAK -> client.options.keyShift.setDown(pressed);
            case JUMP -> client.options.keyJump.setDown(pressed);
            case DROP_ITEM -> {
                client.options.keyDrop.setDown(pressed);
                if (pressed) {
                    KeyMapping.click(client.options.keyDrop.getDefaultKey());
                }
            }
            case INVENTORY -> {
                if (pressed && client.player != null) {
                    if (client.gui.screen() == null) {
                        client.gui.setScreen(new InventoryScreen(client.player));
                    } else if (client.gui.screen() instanceof InventoryScreen) {
                        client.gui.setScreen(null);
                    }
                }
            }
            case PERSPECTIVE -> {
                if (pressed) {
                    CameraType next = client.options.getCameraType().cycle();
                    client.options.setCameraType(next);
                }
            }
            case HOTBAR_NEXT -> {
                if (pressed && client.player != null) {
                    int slot = (client.player.getInventory().getSelectedSlot() + 1) % 9;
                    client.player.getInventory().setSelectedSlot(slot);
                }
            }
            case HOTBAR_PREV -> {
                if (pressed && client.player != null) {
                    int slot = client.player.getInventory().getSelectedSlot() - 1;
                    if (slot < 0) slot = 8;
                    client.player.getInventory().setSelectedSlot(slot);
                }
            }
            case ZOOM -> {
                if (pressed) {
                    zoomActive = !zoomActive;
                }
            }
            default -> {}
        }
    }

    public static double modifyScrollX(double xoffset) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) return xoffset;

        if (config.getTwoFingerScrollAction() == ScrollAction.NONE) {
            return 0.0;
        }

        double factor = config.getScrollSensitivity();
        if (config.isInvertScroll() || config.getTwoFingerScrollAction() == ScrollAction.INVERTED_HOTBAR) {
            factor = -factor;
        }
        return xoffset * factor;
    }

    public static double modifyScrollY(double yoffset) {
        TouchpadConfig config = TouchpadConfig.getInstance();
        if (!config.isTouchpadMode()) return yoffset;

        if (config.getTwoFingerScrollAction() == ScrollAction.NONE) {
            return 0.0;
        }

        double factor = config.getScrollSensitivity();
        if (config.isInvertScroll() || config.getTwoFingerScrollAction() == ScrollAction.INVERTED_HOTBAR) {
            factor = -factor;
        }
        return yoffset * factor;
    }

    public static boolean isZoomActive() {
        return zoomActive;
    }
}
