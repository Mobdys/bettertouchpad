package com.example.bettertouchpad.platform.windows;

import com.example.bettertouchpad.config.TouchpadConfig;
import com.example.bettertouchpad.config.TouchpadGesture;
import com.example.bettertouchpad.handler.TouchpadHandler;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsGestureHook {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterTouchpad/WindowsHook");
    private static volatile boolean running = false;
    private static HHOOK hHook = null;
    private static Thread hookThread = null;

    // Track state of synthetic injected modifier keys
    private static boolean injectedWinDown = false;
    private static boolean injectedCtrlDown = false;
    private static boolean injectedAltDown = false;

    public static void start() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }
        if (running) return;
        running = true;

        hookThread = new Thread(() -> {
            try {
                HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
                LowLevelKeyboardProc keyboardProc = (nCode, wParam, info) -> {
                    if (nCode >= 0 && info != null) {
                        int eventType = wParam.intValue();
                        boolean isKeyDown = (eventType == WinUser.WM_KEYDOWN || eventType == WinUser.WM_SYSKEYDOWN);
                        boolean isKeyUp = (eventType == WinUser.WM_KEYUP || eventType == WinUser.WM_SYSKEYUP);

                        // Check if key is injected by Windows Touchpad / Shell gesture engine
                        boolean isInjected = (info.flags & 0x12) != 0; // LLKHF_INJECTED (0x10) | LLKHF_LOWER_IL_INJECTED (0x02)

                        if (isInjected && TouchpadConfig.getInstance().isTouchpadMode() && TouchpadConfig.getInstance().isBlockWindowsGestures()) {
                            Minecraft client = Minecraft.getInstance();
                            if (client != null && client.isWindowActive()) {
                                int vk = info.vkCode;

                                // Update injected modifier state
                                if (vk == 0x5B || vk == 0x5C) { // VK_LWIN, VK_RWIN
                                    injectedWinDown = isKeyDown;
                                    return new LRESULT(1); // Suppress synthetic Windows key
                                }
                                if (vk == 0x11 || vk == 0xA2 || vk == 0xA3) { // VK_CONTROL, VK_LCONTROL, VK_RCONTROL
                                    injectedCtrlDown = isKeyDown;
                                    if (injectedWinDown) return new LRESULT(1);
                                }
                                if (vk == 0x12 || vk == 0xA4 || vk == 0xA5) { // VK_MENU, VK_LMENU, VK_RMENU (Alt)
                                    injectedAltDown = isKeyDown;
                                    return new LRESULT(1);
                                }

                                // 4-Finger Swipe Left / Right (Virtual Desktop switch: Win + Ctrl + Left / Right)
                                if (injectedWinDown && (vk == 0x25 || vk == 0x27)) { // VK_LEFT, VK_RIGHT
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_SWIPE));
                                    }
                                    return new LRESULT(1); // Block Windows virtual desktop switch!
                                }

                                // 4-Finger Tap (Action Center: Win + A)
                                if (injectedWinDown && vk == 0x41) { // 'A'
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_TAP));
                                    }
                                    return new LRESULT(1); // Block Windows Action Center!
                                }

                                // Precision Touchpad direct F21-F24 keys
                                if (vk == 0x83) { // VK_F24 (4-finger tap)
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_TAP));
                                    }
                                    return new LRESULT(1);
                                }
                                if (vk == 0x82) { // VK_F23 (4-finger swipe)
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_SWIPE));
                                    }
                                    return new LRESULT(1);
                                }
                                if (vk == 0x81) { // VK_F22 (3-finger swipe)
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.THREE_FINGER_SWIPE));
                                    }
                                    return new LRESULT(1);
                                }

                                // 3/4-Finger Swipe Up / Down (Task View: Win+Tab or Show Desktop: Win+D)
                                if (injectedWinDown && (vk == 0x09 || vk == 0x44)) { // VK_TAB, VK_D
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_SWIPE));
                                    }
                                    return new LRESULT(1); // Block Task View & Minimizing to Desktop!
                                }

                                // 3-Finger Swipe (Alt + Tab: Task switch)
                                if (injectedAltDown && vk == 0x09) { // VK_TAB
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.THREE_FINGER_SWIPE));
                                    }
                                    return new LRESULT(1); // Block Alt+Tab!
                                }

                                // 3-Finger Tap (Win + S: Search / Cortana)
                                if (injectedWinDown && vk == 0x53) { // 'S'
                                    if (isKeyDown) {
                                        client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.THREE_FINGER_CLICK));
                                    }
                                    return new LRESULT(1); // Block Windows Search!
                                }
                            }
                        }
                    }
                    return User32.INSTANCE.CallNextHookEx(hHook, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
                };

                hHook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardProc, hMod, 0);
                if (hHook == null) {
                    LOGGER.warn("Failed to install Windows Touchpad Hook: {}", Kernel32.INSTANCE.GetLastError());
                    return;
                }

                LOGGER.info("Windows Touchpad Gesture Hook successfully active. 3-finger and 4-finger gestures will be intercepted for Minecraft.");
                MSG msg = new MSG();
                while (running && User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                }

                if (hHook != null) {
                    User32.INSTANCE.UnhookWindowsHookEx(hHook);
                    hHook = null;
                }
            } catch (Throwable t) {
                LOGGER.error("Error in Windows Touchpad Gesture Hook thread", t);
            }
        }, "BetterTouchpad-WindowsGestureHook");

        hookThread.setDaemon(true);
        hookThread.start();
    }

    public static void stop() {
        running = false;
        if (hHook != null) {
            User32.INSTANCE.UnhookWindowsHookEx(hHook);
            hHook = null;
        }
    }
}
