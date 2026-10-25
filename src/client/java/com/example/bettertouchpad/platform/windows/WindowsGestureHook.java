package com.example.bettertouchpad.platform.windows;

import com.example.bettertouchpad.config.TouchpadConfig;
import com.example.bettertouchpad.config.TouchpadGesture;
import com.example.bettertouchpad.handler.TouchpadHandler;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class WindowsGestureHook {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterTouchpad/WindowsHook");
    private static volatile boolean running = false;
    private static MemorySegment hHook = MemorySegment.NULL;
    private static int hookThreadId = 0;
    private static Thread hookThread = null;

    // Native method handles via Java 25 Foreign Function & Memory API
    private static MethodHandle setWindowsHookExHandle;
    private static MethodHandle unhookWindowsHookExHandle;
    private static MethodHandle callNextHookExHandle;
    private static MethodHandle getMessageHandle;
    private static MethodHandle translateMessageHandle;
    private static MethodHandle dispatchMessageHandle;
    private static MethodHandle postThreadMessageHandle;
    private static MethodHandle getModuleHandleHandle;
    private static MethodHandle getCurrentThreadIdHandle;

    // Modifier key tracking
    private static volatile boolean winDown = false;
    private static volatile boolean ctrlDown = false;
    private static volatile boolean altDown = false;

    public static long hookCallback(int nCode, long wParam, long lParam) {
        if (nCode >= 0 && lParam != 0) {
            try {
                TouchpadConfig config = TouchpadConfig.getInstance();
                Minecraft client = Minecraft.getInstance();

                boolean isWindowActive = (client != null && client.isWindowActive());
                boolean shouldBlock = (config.isTouchpadMode() && config.isBlockWindowsGestures());

                if (!isWindowActive) {
                    // Reset all modifier states when Minecraft is in background
                    winDown = false;
                    ctrlDown = false;
                    altDown = false;
                } else if (shouldBlock) {
                    MemorySegment pKbd = MemorySegment.ofAddress(lParam).reinterpret(24);
                    int vkCode = pKbd.get(ValueLayout.JAVA_INT, 0);

                    boolean isKeyDown = (wParam == 0x0100L || wParam == 0x0104L); // WM_KEYDOWN, WM_SYSKEYDOWN
                    boolean isKeyUp = (wParam == 0x0101L || wParam == 0x0105L);   // WM_KEYUP, WM_SYSKEYUP

                    // Track modifier keys
                    if (vkCode == 0x11 || vkCode == 0xA2 || vkCode == 0xA3) { // VK_CONTROL, VK_LCONTROL, VK_RCONTROL
                        ctrlDown = isKeyDown;
                    }
                    if (vkCode == 0x12 || vkCode == 0xA4 || vkCode == 0xA5) { // VK_MENU (Alt), VK_LMENU, VK_RMENU
                        altDown = isKeyDown;
                    }

                    // 1. Windows Key (VK_LWIN = 0x5B, VK_RWIN = 0x5C)
                    if (vkCode == 0x5B || vkCode == 0x5C) {
                        winDown = isKeyDown;
                        return 1L; // Block Windows key completely (prevents Start menu & shortcuts)
                    }

                    // 2. ANY key pressed or released while Windows key is down (BLOCK ALL WINDOWS SHORTCUTS)
                    if (winDown) {
                        // Check if this combination corresponds to a touchpad gesture:
                        if (vkCode == 0x53) { // Win + S -> 3-Finger Tap / Search
                            if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.THREE_FINGER_CLICK));
                        } else if (vkCode == 0x25 || vkCode == 0x27) { // Win + (Ctrl) + Left/Right -> 4-Finger Swipe / Virtual Desktops
                            if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_SWIPE));
                        } else if (vkCode == 0x41) { // Win + A -> 4-Finger Tap / Action Center
                            if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_TAP));
                        } else if (vkCode == 0x09 || vkCode == 0x44) { // Win + Tab / Win + D -> Task View / Minimize to Desktop
                            if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_SWIPE));
                        }
                        // Suppress ALL keys while Win is down: Win+E, Win+R, Win+G, Win+I, Win+V, Win+W, Win+X, Win+Z, Win+Shift+S, etc.
                        return 1L;
                    }

                    // 3. Ctrl + Esc (Windows Start Menu shortcut alternative)
                    if (ctrlDown && vkCode == 0x1B) { // VK_ESCAPE
                        return 1L; // Block Ctrl+Esc opening Start Menu
                    }

                    // 4. Alt + Tab (Windows Task Switcher / Touchpad 3-Finger Swipe)
                    if (altDown && vkCode == 0x09) { // VK_TAB
                        if (isKeyDown) {
                            client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.THREE_FINGER_SWIPE));
                        }
                        return 1L; // Block Windows Alt+Tab
                    }

                    // 5. Alt + Esc (Direct Window Switching)
                    if (altDown && vkCode == 0x1B) { // VK_ESCAPE
                        return 1L; // Block Alt+Esc
                    }

                    // 6. Application / Context Menu Key (VK_APPS = 0x5D)
                    if (vkCode == 0x5D) {
                        return 1L; // Block Windows Context Menu key
                    }

                    // 7. Precision Touchpad Direct Virtual Keys (F21 to F24)
                    if (vkCode == 0x81) { // VK_F22 (3-Finger gesture)
                        if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.THREE_FINGER_CLICK));
                        return 1L;
                    }
                    if (vkCode == 0x82) { // VK_F23 (4-Finger swipe)
                        if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_SWIPE));
                        return 1L;
                    }
                    if (vkCode == 0x83) { // VK_F24 (4-Finger tap)
                        if (isKeyDown) client.execute(() -> TouchpadHandler.triggerGesture(TouchpadGesture.FOUR_FINGER_TAP));
                        return 1L;
                    }
                    if (vkCode == 0x80) { // VK_F21
                        return 1L;
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("Error inside Windows gesture hook callback", t);
            }
        }

        // Pass event through to next hook
        try {
            if (callNextHookExHandle != null) {
                return (long) callNextHookExHandle.invokeExact(MemorySegment.NULL, nCode, wParam, lParam);
            }
        } catch (Throwable ignored) {}
        return 0L;
    }

    public static void start() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }
        if (running) return;
        running = true;

        hookThread = new Thread(() -> {
            try {
                Linker linker = Linker.nativeLinker();
                SymbolLookup user32 = SymbolLookup.libraryLookup("user32.dll", Arena.global());
                SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32.dll", Arena.global());

                setWindowsHookExHandle = linker.downcallHandle(
                    user32.find("SetWindowsHookExW").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
                );
                unhookWindowsHookExHandle = linker.downcallHandle(
                    user32.find("UnhookWindowsHookEx").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                );
                callNextHookExHandle = linker.downcallHandle(
                    user32.find("CallNextHookEx").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
                );
                getMessageHandle = linker.downcallHandle(
                    user32.find("GetMessageW").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
                );
                translateMessageHandle = linker.downcallHandle(
                    user32.find("TranslateMessage").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                );
                dispatchMessageHandle = linker.downcallHandle(
                    user32.find("DispatchMessageW").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
                );
                postThreadMessageHandle = linker.downcallHandle(
                    user32.find("PostThreadMessageW").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
                );
                getModuleHandleHandle = linker.downcallHandle(
                    kernel32.find("GetModuleHandleW").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
                );
                getCurrentThreadIdHandle = linker.downcallHandle(
                    kernel32.find("GetCurrentThreadId").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT)
                );

                hookThreadId = (int) getCurrentThreadIdHandle.invokeExact();
                MemorySegment hMod = (MemorySegment) getModuleHandleHandle.invokeExact(MemorySegment.NULL);

                MethodHandle target = MethodHandles.lookup().findStatic(
                    WindowsGestureHook.class, "hookCallback",
                    java.lang.invoke.MethodType.methodType(long.class, int.class, long.class, long.class)
                );
                FunctionDescriptor hookDesc = FunctionDescriptor.of(
                    ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG
                );
                MemorySegment upcallStub = linker.upcallStub(target, hookDesc, Arena.global());

                int WH_KEYBOARD_LL = 13;
                hHook = (MemorySegment) setWindowsHookExHandle.invokeExact(WH_KEYBOARD_LL, upcallStub, hMod, 0);

                if (hHook.equals(MemorySegment.NULL)) {
                    LOGGER.warn("Failed to install Windows Touchpad Hook via FFM");
                    return;
                }

                LOGGER.info("Windows Touchpad Gesture Hook successfully installed via Java FFM. All Windows shortcuts and gestures blocked while Minecraft is active.");

                Arena arena = Arena.ofConfined();
                MemorySegment msg = arena.allocate(48);
                while (running) {
                    int ret = (int) getMessageHandle.invokeExact(msg, MemorySegment.NULL, 0, 0);
                    if (ret <= 0) break;
                    translateMessageHandle.invokeExact(msg);
                    dispatchMessageHandle.invokeExact(msg);
                }
                arena.close();

                if (!hHook.equals(MemorySegment.NULL)) {
                    unhookWindowsHookExHandle.invokeExact(hHook);
                    hHook = MemorySegment.NULL;
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
        try {
            if (postThreadMessageHandle != null && hookThreadId != 0) {
                postThreadMessageHandle.invokeExact(hookThreadId, 0x0012, 0L, 0L);
            }
            if (unhookWindowsHookExHandle != null && !hHook.equals(MemorySegment.NULL)) {
                unhookWindowsHookExHandle.invokeExact(hHook);
                hHook = MemorySegment.NULL;
            }
        } catch (Throwable ignored) {}
    }
}
