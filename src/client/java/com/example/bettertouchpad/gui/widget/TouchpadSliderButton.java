package com.example.bettertouchpad.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class TouchpadSliderButton extends AbstractSliderButton {
    private final String translationKey;
    private final double min;
    private final double max;
    private final Consumer<Double> onValueChange;

    public TouchpadSliderButton(int x, int y, int width, int height, String translationKey, double currentVal, double min, double max, Consumer<Double> onValueChange) {
        super(x, y, width, height, Component.empty(), (Math.clamp(currentVal, min, max) - min) / (max - min));
        this.translationKey = translationKey;
        this.min = min;
        this.max = max;
        this.onValueChange = onValueChange;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        int percent = (int) Math.round(getValue() * 100.0);
        this.setMessage(Component.translatable(translationKey, percent + "%"));
    }

    @Override
    protected void applyValue() {
        if (onValueChange != null) {
            onValueChange.accept(getValue());
        }
    }

    public double getValue() {
        return Mth.lerp(this.value, this.min, this.max);
    }
}
