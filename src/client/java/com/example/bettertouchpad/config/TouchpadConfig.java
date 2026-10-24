package com.example.bettertouchpad.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class TouchpadConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterTouchpad/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final TouchpadConfig INSTANCE = new TouchpadConfig();

    private boolean touchpadMode = true;
    private boolean tapToClick = true;
    private boolean invertScroll = false;
    private boolean smoothLook = false;
    private boolean blockWindowsGestures = true;
    private double touchpadSensitivity = 1.0;
    private double scrollSensitivity = 1.0;

    private TouchpadAction singleClickAction = TouchpadAction.LEFT_CLICK;
    private TouchpadAction twoFingerClickAction = TouchpadAction.RIGHT_CLICK;
    private TouchpadAction threeFingerClickAction = TouchpadAction.MIDDLE_CLICK;
    private TouchpadAction threeFingerSwipeAction = TouchpadAction.HOTBAR_NEXT;
    private TouchpadAction fourFingerTapAction = TouchpadAction.PERSPECTIVE;
    private TouchpadAction fourFingerSwipeAction = TouchpadAction.INVENTORY;
    private TouchpadAction doubleClickAction = TouchpadAction.LEFT_CLICK;
    private ScrollAction twoFingerScrollAction = ScrollAction.HOTBAR;

    public static TouchpadConfig getInstance() {
        return INSTANCE;
    }

    private File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("bettertouchpad.json").toFile();
    }

    public void load() {
        File file = getConfigFile();
        if (!file.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            TouchpadConfig loaded = GSON.fromJson(reader, TouchpadConfig.class);
            if (loaded != null) {
                this.touchpadMode = loaded.touchpadMode;
                this.tapToClick = loaded.tapToClick;
                this.invertScroll = loaded.invertScroll;
                this.smoothLook = loaded.smoothLook;
                this.blockWindowsGestures = loaded.blockWindowsGestures;
                this.touchpadSensitivity = Math.clamp(loaded.touchpadSensitivity, 0.2, 2.0);
                this.scrollSensitivity = Math.clamp(loaded.scrollSensitivity, 0.2, 2.0);
                if (loaded.singleClickAction != null) this.singleClickAction = loaded.singleClickAction;
                if (loaded.twoFingerClickAction != null) this.twoFingerClickAction = loaded.twoFingerClickAction;
                if (loaded.threeFingerClickAction != null) this.threeFingerClickAction = loaded.threeFingerClickAction;
                if (loaded.threeFingerSwipeAction != null) this.threeFingerSwipeAction = loaded.threeFingerSwipeAction;
                if (loaded.fourFingerTapAction != null) this.fourFingerTapAction = loaded.fourFingerTapAction;
                if (loaded.fourFingerSwipeAction != null) this.fourFingerSwipeAction = loaded.fourFingerSwipeAction;
                if (loaded.doubleClickAction != null) this.doubleClickAction = loaded.doubleClickAction;
                if (loaded.twoFingerScrollAction != null) this.twoFingerScrollAction = loaded.twoFingerScrollAction;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load BetterTouchpad config", e);
        }
    }

    public void save() {
        File file = getConfigFile();
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save BetterTouchpad config", e);
        }
    }

    public void resetToDefaults() {
        this.touchpadMode = true;
        this.tapToClick = true;
        this.invertScroll = false;
        this.smoothLook = false;
        this.blockWindowsGestures = true;
        this.touchpadSensitivity = 1.0;
        this.scrollSensitivity = 1.0;
        this.singleClickAction = TouchpadAction.LEFT_CLICK;
        this.twoFingerClickAction = TouchpadAction.RIGHT_CLICK;
        this.threeFingerClickAction = TouchpadAction.MIDDLE_CLICK;
        this.threeFingerSwipeAction = TouchpadAction.HOTBAR_NEXT;
        this.fourFingerTapAction = TouchpadAction.PERSPECTIVE;
        this.fourFingerSwipeAction = TouchpadAction.INVENTORY;
        this.doubleClickAction = TouchpadAction.LEFT_CLICK;
        this.twoFingerScrollAction = ScrollAction.HOTBAR;
        save();
    }

    public TouchpadAction getActionForGesture(TouchpadGesture gesture) {
        return switch (gesture) {
            case SINGLE_CLICK -> singleClickAction;
            case TWO_FINGER_CLICK -> twoFingerClickAction;
            case THREE_FINGER_CLICK -> threeFingerClickAction;
            case THREE_FINGER_SWIPE -> threeFingerSwipeAction;
            case FOUR_FINGER_TAP -> fourFingerTapAction;
            case FOUR_FINGER_SWIPE -> fourFingerSwipeAction;
            case DOUBLE_CLICK -> doubleClickAction;
        };
    }

    public void setActionForGesture(TouchpadGesture gesture, TouchpadAction action) {
        switch (gesture) {
            case SINGLE_CLICK -> singleClickAction = action;
            case TWO_FINGER_CLICK -> twoFingerClickAction = action;
            case THREE_FINGER_CLICK -> threeFingerClickAction = action;
            case THREE_FINGER_SWIPE -> threeFingerSwipeAction = action;
            case FOUR_FINGER_TAP -> fourFingerTapAction = action;
            case FOUR_FINGER_SWIPE -> fourFingerSwipeAction = action;
            case DOUBLE_CLICK -> doubleClickAction = action;
        }
    }

    public TouchpadAction getActionForRawButton(int button) {
        return switch (button) {
            case 1 -> singleClickAction;      // SDL Left click (single tap)
            case 3 -> twoFingerClickAction;   // SDL Right click (two-finger tap)
            case 2 -> threeFingerClickAction; // SDL Middle click (three-finger tap)
            default -> TouchpadAction.NONE;
        };
    }

    public int getMappedMouseButton(int rawButton) {
        TouchpadAction action = getActionForRawButton(rawButton);
        if (action.isMouseButton()) {
            return action.getMouseButton();
        }
        return rawButton;
    }

    // Getters and Setters
    public boolean isTouchpadMode() { return touchpadMode; }
    public void setTouchpadMode(boolean touchpadMode) { this.touchpadMode = touchpadMode; }

    public boolean isTapToClick() { return tapToClick; }
    public void setTapToClick(boolean tapToClick) { this.tapToClick = tapToClick; }

    public boolean isInvertScroll() { return invertScroll; }
    public void setInvertScroll(boolean invertScroll) { this.invertScroll = invertScroll; }

    public boolean isSmoothLook() { return smoothLook; }
    public void setSmoothLook(boolean smoothLook) { this.smoothLook = smoothLook; }

    public boolean isBlockWindowsGestures() { return blockWindowsGestures; }
    public void setBlockWindowsGestures(boolean blockWindowsGestures) { this.blockWindowsGestures = blockWindowsGestures; }

    public double getTouchpadSensitivity() { return touchpadSensitivity; }
    public void setTouchpadSensitivity(double touchpadSensitivity) { this.touchpadSensitivity = touchpadSensitivity; }

    public double getScrollSensitivity() { return scrollSensitivity; }
    public void setScrollSensitivity(double scrollSensitivity) { this.scrollSensitivity = scrollSensitivity; }

    public TouchpadAction getSingleClickAction() { return singleClickAction; }
    public void setSingleClickAction(TouchpadAction singleClickAction) { this.singleClickAction = singleClickAction; }

    public TouchpadAction getTwoFingerClickAction() { return twoFingerClickAction; }
    public void setTwoFingerClickAction(TouchpadAction twoFingerClickAction) { this.twoFingerClickAction = twoFingerClickAction; }

    public TouchpadAction getThreeFingerClickAction() { return threeFingerClickAction; }
    public void setThreeFingerClickAction(TouchpadAction threeFingerClickAction) { this.threeFingerClickAction = threeFingerClickAction; }

    public TouchpadAction getThreeFingerSwipeAction() { return threeFingerSwipeAction; }
    public void setThreeFingerSwipeAction(TouchpadAction threeFingerSwipeAction) { this.threeFingerSwipeAction = threeFingerSwipeAction; }

    public TouchpadAction getFourFingerTapAction() { return fourFingerTapAction; }
    public void setFourFingerTapAction(TouchpadAction fourFingerTapAction) { this.fourFingerTapAction = fourFingerTapAction; }

    public TouchpadAction getFourFingerSwipeAction() { return fourFingerSwipeAction; }
    public void setFourFingerSwipeAction(TouchpadAction fourFingerSwipeAction) { this.fourFingerSwipeAction = fourFingerSwipeAction; }

    public TouchpadAction getDoubleClickAction() { return doubleClickAction; }
    public void setDoubleClickAction(TouchpadAction doubleClickAction) { this.doubleClickAction = doubleClickAction; }

    public ScrollAction getTwoFingerScrollAction() { return twoFingerScrollAction; }
    public void setTwoFingerScrollAction(ScrollAction twoFingerScrollAction) { this.twoFingerScrollAction = twoFingerScrollAction; }
}
