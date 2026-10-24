package com.example.client.mixin;

import com.example.bettertouchpad.handler.TouchpadHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onTouchpadButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (TouchpadHandler.handleButtonEvent(handle, rawButtonInfo, action)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "onButton", at = @At("HEAD"), argsOnly = true)
    private MouseButtonInfo modifyTouchpadButton(MouseButtonInfo rawButtonInfo) {
        return TouchpadHandler.remapMouseButton(rawButtonInfo);
    }

    @ModifyVariable(method = "onScroll", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double modifyScrollX(double xoffset) {
        return TouchpadHandler.modifyScrollX(xoffset);
    }

    @ModifyVariable(method = "onScroll", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double modifyScrollY(double yoffset) {
        return TouchpadHandler.modifyScrollY(yoffset);
    }

    @ModifyVariable(method = "onMove", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private double modifyXRel(double xrel) {
        return TouchpadHandler.modifyLookDelta(xrel);
    }

    @ModifyVariable(method = "onMove", at = @At("HEAD"), ordinal = 3, argsOnly = true)
    private double modifyYRel(double yrel) {
        return TouchpadHandler.modifyLookDelta(yrel);
    }
}
