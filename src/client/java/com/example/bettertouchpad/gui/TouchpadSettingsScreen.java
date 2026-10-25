package com.example.bettertouchpad.gui;

import com.example.bettertouchpad.config.ScrollAction;
import com.example.bettertouchpad.config.TouchpadAction;
import com.example.bettertouchpad.config.TouchpadConfig;
import com.example.bettertouchpad.config.TouchpadGesture;
import com.example.bettertouchpad.gui.widget.TouchpadSliderButton;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class TouchpadSettingsScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("bettertouchpad.title");

    public TouchpadSettingsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void addOptions() {
        if (this.list == null) return;
        TouchpadConfig config = TouchpadConfig.getInstance();

        // 1. Dokunmatik Yüzey Hareketleri (Gestures)
        this.list.addHeader(Component.translatable("bettertouchpad.category.gestures"));

        // Single Click row
        CycleButton<TouchpadAction> singleClickButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getSingleClickAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.single_click.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.single_click"), (button, value) -> {
                config.setSingleClickAction(value);
                config.save();
            });
        Button singleClickPicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.SINGLE_CLICK))
        ).build();
        this.list.addSmall(singleClickButton, singleClickPicker);

        // One-Finger Swipe row (1 Parmakla Kaydırma)
        CycleButton<TouchpadAction> oneFingerSwipeButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getOneFingerSwipeAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.one_finger_swipe.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.one_finger_swipe"), (button, value) -> {
                config.setOneFingerSwipeAction(value);
                config.save();
            });
        Button oneFingerSwipePicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.ONE_FINGER_SWIPE))
        ).build();
        this.list.addSmall(oneFingerSwipeButton, oneFingerSwipePicker);

        // Two-Finger Click row
        CycleButton<TouchpadAction> twoFingerButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getTwoFingerClickAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.two_finger_click.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.two_finger_click"), (button, value) -> {
                config.setTwoFingerClickAction(value);
                config.save();
            });
        Button twoFingerPicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.TWO_FINGER_CLICK))
        ).build();
        this.list.addSmall(twoFingerButton, twoFingerPicker);

        // Three-Finger Click row
        CycleButton<TouchpadAction> threeFingerButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getThreeFingerClickAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.three_finger_click.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.three_finger_click"), (button, value) -> {
                config.setThreeFingerClickAction(value);
                config.save();
            });
        Button threeFingerPicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.THREE_FINGER_CLICK))
        ).build();
        this.list.addSmall(threeFingerButton, threeFingerPicker);

        // Three-Finger Swipe row
        CycleButton<TouchpadAction> threeFingerSwipeButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getThreeFingerSwipeAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.three_finger_swipe.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.three_finger_swipe"), (button, value) -> {
                config.setThreeFingerSwipeAction(value);
                config.save();
            });
        Button threeFingerSwipePicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.THREE_FINGER_SWIPE))
        ).build();
        this.list.addSmall(threeFingerSwipeButton, threeFingerSwipePicker);

        // Four-Finger Tap row
        CycleButton<TouchpadAction> fourFingerTapButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getFourFingerTapAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.four_finger_tap.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.four_finger_tap"), (button, value) -> {
                config.setFourFingerTapAction(value);
                config.save();
            });
        Button fourFingerTapPicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.FOUR_FINGER_TAP))
        ).build();
        this.list.addSmall(fourFingerTapButton, fourFingerTapPicker);

        // Four-Finger Swipe row (Windows masaüstü değiştirme yerine)
        CycleButton<TouchpadAction> fourFingerSwipeButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getFourFingerSwipeAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.four_finger_swipe.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.four_finger_swipe"), (button, value) -> {
                config.setFourFingerSwipeAction(value);
                config.save();
            });
        Button fourFingerSwipePicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.FOUR_FINGER_SWIPE))
        ).build();
        this.list.addSmall(fourFingerSwipeButton, fourFingerSwipePicker);

        // Double Click row
        CycleButton<TouchpadAction> doubleClickButton = CycleButton.builder(TouchpadAction::getDisplayName, config.getDoubleClickAction())
            .withValues(TouchpadAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.double_click.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.double_click"), (button, value) -> {
                config.setDoubleClickAction(value);
                config.save();
            });
        Button doubleClickPicker = Button.builder(
            Component.translatable("bettertouchpad.button.action_picker"),
            button -> this.minecraft.gui.setScreen(new ActionPickerScreen(this, this.options, TouchpadGesture.DOUBLE_CLICK))
        ).build();
        this.list.addSmall(doubleClickButton, doubleClickPicker);

        // Scroll and Invert Scroll row
        CycleButton<ScrollAction> scrollButton = CycleButton.builder(ScrollAction::getDisplayName, config.getTwoFingerScrollAction())
            .withValues(ScrollAction.values())
            .withTooltip(action -> Tooltip.create(Component.translatable("bettertouchpad.gesture.two_finger_scroll.tooltip", action.getDisplayName(), action.getDescription())))
            .create(Component.translatable("bettertouchpad.gesture.two_finger_scroll"), (button, value) -> {
                config.setTwoFingerScrollAction(value);
                config.save();
            });
        CycleButton<Boolean> invertScrollButton = CycleButton.onOffBuilder(config.isInvertScroll())
            .withTooltip(val -> Tooltip.create(Component.translatable("bettertouchpad.option.invert_scroll.tooltip")))
            .create(Component.translatable("bettertouchpad.option.invert_scroll"), (button, value) -> {
                config.setInvertScroll(value);
                config.save();
            });
        this.list.addSmall(scrollButton, invertScrollButton);

        // 2. Genel Ayarlar (General Settings)
        this.list.addHeader(Component.translatable("bettertouchpad.category.general"));

        // Windows Hareketlerini Engelle (3 ve 4 Parmak)
        CycleButton<Boolean> blockGesturesBtn = CycleButton.onOffBuilder(config.isBlockWindowsGestures())
            .withTooltip(val -> Tooltip.create(Component.translatable("bettertouchpad.option.block_windows_gestures.tooltip")))
            .create(Component.translatable("bettertouchpad.option.block_windows_gestures"), (button, value) -> {
                config.setBlockWindowsGestures(value);
                config.save();
            });
        CycleButton<Boolean> touchpadModeBtn = CycleButton.onOffBuilder(config.isTouchpadMode())
            .withTooltip(val -> Tooltip.create(Component.translatable("bettertouchpad.option.touchpad_mode.tooltip")))
            .create(Component.translatable("bettertouchpad.option.touchpad_mode"), (button, value) -> {
                config.setTouchpadMode(value);
                config.save();
            });
        this.list.addSmall(blockGesturesBtn, touchpadModeBtn);

        CycleButton<Boolean> tapToClickBtn = CycleButton.onOffBuilder(config.isTapToClick())
            .withTooltip(val -> Tooltip.create(Component.translatable("bettertouchpad.option.tap_to_click.tooltip")))
            .create(Component.translatable("bettertouchpad.option.tap_to_click"), (button, value) -> {
                config.setTapToClick(value);
                config.save();
            });
        CycleButton<Boolean> smoothLookBtn = CycleButton.onOffBuilder(config.isSmoothLook())
            .withTooltip(val -> Tooltip.create(Component.translatable("bettertouchpad.option.smooth_look.tooltip")))
            .create(Component.translatable("bettertouchpad.option.smooth_look"), (button, value) -> {
                config.setSmoothLook(value);
                config.save();
            });
        this.list.addSmall(tapToClickBtn, smoothLookBtn);

        // 3. Hassasiyet Ayarları (Sensitivity)
        this.list.addHeader(Component.translatable("bettertouchpad.category.sensitivity"));

        TouchpadSliderButton touchpadSensSlider = new TouchpadSliderButton(
            0, 0, 150, 20,
            "bettertouchpad.option.touchpad_sensitivity",
            config.getTouchpadSensitivity(), 0.2, 2.0,
            val -> {
                config.setTouchpadSensitivity(val);
                config.save();
            }
        );
        TouchpadSliderButton scrollSensSlider = new TouchpadSliderButton(
            0, 0, 150, 20,
            "bettertouchpad.option.scroll_sensitivity",
            config.getScrollSensitivity(), 0.2, 2.0,
            val -> {
                config.setScrollSensitivity(val);
                config.save();
            }
        );
        this.list.addSmall(touchpadSensSlider, scrollSensSlider);
    }

    @Override
    protected void addFooter() {
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("bettertouchpad.button.reset_defaults"), btn -> {
            TouchpadConfig.getInstance().resetToDefaults();
            this.minecraft.gui.setScreen(new TouchpadSettingsScreen(this.lastScreen, this.options));
        }).width(150).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose()).width(150).build());
    }

    @Override
    public void removed() {
        TouchpadConfig.getInstance().save();
    }
}
