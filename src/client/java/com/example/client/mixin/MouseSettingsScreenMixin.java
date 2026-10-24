package com.example.client.mixin;

import com.example.bettertouchpad.gui.TouchpadSettingsScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseSettingsScreen.class)
public abstract class MouseSettingsScreenMixin extends OptionsSubScreen {
    protected MouseSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void injectTouchpadSettingsButton(CallbackInfo ci) {
        if (this.list != null) {
            this.list.addBig(Button.builder(
                Component.translatable("bettertouchpad.touchpad_settings"),
                button -> this.minecraft.gui.setScreen(new TouchpadSettingsScreen(this, this.options))
            ).build());
        }
    }
}
