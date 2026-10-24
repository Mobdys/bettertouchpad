package com.example.bettertouchpad.gui;

import com.example.bettertouchpad.config.TouchpadAction;
import com.example.bettertouchpad.config.TouchpadConfig;
import com.example.bettertouchpad.config.TouchpadGesture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ActionPickerScreen extends OptionsSubScreen {
    private final TouchpadGesture gesture;

    public ActionPickerScreen(Screen lastScreen, Options options, TouchpadGesture gesture) {
        super(lastScreen, options, Component.translatable("bettertouchpad.action_picker.title", gesture.getDisplayName()));
        this.gesture = gesture;
    }

    @Override
    protected void addOptions() {
        if (this.list == null) return;
        TouchpadConfig config = TouchpadConfig.getInstance();
        TouchpadAction currentAction = config.getActionForGesture(this.gesture);

        for (TouchpadAction action : TouchpadAction.values()) {
            boolean isSelected = (action == currentAction);
            MutableComponent label;
            if (isSelected) {
                label = Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                        .append(action.getDisplayName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))
                        .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                        .append(action.getDescription().copy().withStyle(ChatFormatting.WHITE));
            } else {
                label = action.getDisplayName().copy().withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                        .append(action.getDescription().copy().withStyle(ChatFormatting.DARK_GRAY));
            }

            Button btn = Button.builder(label, button -> {
                config.setActionForGesture(this.gesture, action);
                config.save();
                this.minecraft.gui.setScreen(this.lastScreen);
            }).width(310).build();

            this.list.addBig(btn);
        }
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(200).build());
    }
}
