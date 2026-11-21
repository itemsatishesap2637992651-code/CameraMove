package com.example.cameramove;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CameraMoveClient implements ClientModInitializer {
    private static final double BASE_SPEED = 22.0;
    private static final int MIN_SPEED_PERCENT = 10;
    private static final int MAX_SPEED_PERCENT = 300;

    private static int speedPercent = 100;

    private KeyBinding rightKey;
    private KeyBinding leftKey;
    private KeyBinding upKey;
    private KeyBinding downKey;
    private KeyBinding diagRightKey;
    private KeyBinding diagLeftKey;
    private KeyBinding upLeftDiagKey;
    private KeyBinding upRightDiagKey;
    private KeyBinding speedUpKey;
    private KeyBinding speedDownKey;
    private KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("cameramove", "main"));

        rightKey = register("key.cameramove.right", GLFW.GLFW_KEY_KP_6, category);
        leftKey = register("key.cameramove.left", GLFW.GLFW_KEY_KP_4, category);
        upKey = register("key.cameramove.up", GLFW.GLFW_KEY_KP_8, category);
        downKey = register("key.cameramove.down", GLFW.GLFW_KEY_KP_2, category);
        diagRightKey = register("key.cameramove.diag_right", GLFW.GLFW_KEY_KP_3, category);
        diagLeftKey = register("key.cameramove.diag_left", GLFW.GLFW_KEY_KP_1, category);
        upLeftDiagKey = register("key.cameramove.up_left_diag", GLFW.GLFW_KEY_KP_7, category);
        upRightDiagKey = register("key.cameramove.up_right_diag", GLFW.GLFW_KEY_KP_9, category);
        speedUpKey = register("key.cameramove.speed_up", GLFW.GLFW_KEY_EQUAL, category);
        speedDownKey = register("key.cameramove.speed_down", GLFW.GLFW_KEY_MINUS, category);
        menuKey = register("key.cameramove.menu", GLFW.GLFW_KEY_RIGHT_BRACKET, category);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (speedUpKey.wasPressed()) {
                speedPercent = Math.min(MAX_SPEED_PERCENT, speedPercent + 25);
            }
            while (speedDownKey.wasPressed()) {
                speedPercent = Math.max(MIN_SPEED_PERCENT, speedPercent - 25);
            }
            while (menuKey.wasPressed()) {
                client.setScreen(new CameraMoveSettingsScreen(client.currentScreen));
            }

            ClientPlayerEntity player = client.player;
            if (player == null || client.currentScreen != null) {
                return;
            }

            double deltaX = 0.0;
            double deltaY = 0.0;

            // Yalnızca basılı tutulan tuşlar işlenir; bırakıldığında hareket durur.
            if (rightKey.isPressed()) {
                deltaX += 1.0;
            }
            if (leftKey.isPressed()) {
                deltaX -= 1.0;
            }
            if (upKey.isPressed()) {
                deltaY -= 1.0;
            }
            if (downKey.isPressed()) {
                deltaY += 1.0;
            }
            if (diagRightKey.isPressed()) {
                deltaX += 1.0;
                deltaY += 1.0;
            }
            if (diagLeftKey.isPressed()) {
                deltaX -= 1.0;
                deltaY += 1.0;
            }
            if (upLeftDiagKey.isPressed()) {
                deltaX -= 1.0;
                deltaY -= 1.0;
            }
            if (upRightDiagKey.isPressed()) {
                deltaX += 1.0;
                deltaY -= 1.0;
            }

            if (deltaX != 0.0 || deltaY != 0.0) {
                double multiplier = speedPercent / 100.0;
                player.changeLookDirection(deltaX * BASE_SPEED * multiplier, deltaY * BASE_SPEED * multiplier);
            }
        });
    }

    private KeyBinding register(String translationKey, int keyCode, KeyBinding.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                InputUtil.Type.KEYSYM,
                keyCode,
                category
        ));
    }

    private static class CameraMoveSettingsScreen extends Screen {
        private final Screen parent;

        protected CameraMoveSettingsScreen(Screen parent) {
            super(Text.literal("CameraMove Ayarları"));
            this.parent = parent;
        }

        @Override
        protected void init() {
