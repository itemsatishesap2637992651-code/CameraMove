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
    private static final double BASE_SPEED = 42.0;
    private static final int MIN_SPEED = 10;
    private static final int MAX_SPEED = 300;
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("cameramove", "main"));

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
        rightKey = register("key.cameramove.right", GLFW.GLFW_KEY_KP_6);
        leftKey = register("key.cameramove.left", GLFW.GLFW_KEY_KP_4);
        upKey = register("key.cameramove.up", GLFW.GLFW_KEY_KP_8);
        downKey = register("key.cameramove.down", GLFW.GLFW_KEY_KP_2);
        diagRightKey = register("key.cameramove.diag_right", GLFW.GLFW_KEY_KP_3);
        diagLeftKey = register("key.cameramove.diag_left", GLFW.GLFW_KEY_KP_1);
        upLeftDiagKey = register("key.cameramove.up_left_diag", GLFW.GLFW_KEY_KP_7);
        upRightDiagKey = register("key.cameramove.up_right_diag", GLFW.GLFW_KEY_KP_9);
        speedUpKey = register("key.cameramove.speed_up", GLFW.GLFW_KEY_EQUAL);
        speedDownKey = register("key.cameramove.speed_down", GLFW.GLFW_KEY_MINUS);
        menuKey = register("key.cameramove.menu", GLFW.GLFW_KEY_RIGHT_BRACKET);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (speedUpKey.wasPressed()) {
                speedPercent = Math.min(MAX_SPEED, speedPercent + 25);
            }
            while (speedDownKey.wasPressed()) {
                speedPercent = Math.max(MIN_SPEED, speedPercent - 25);
            }
            while (menuKey.wasPressed()) {
                client.setScreen(new CameraMoveScreen(client.currentScreen));
            }

            ClientPlayerEntity player = client.player;
            if (player == null || client.currentScreen != null) {
                return;
            }

            double dx = 0.0;
            double dy = 0.0;

            if (rightKey.isPressed()) {
                dx += 1.0;
            }
            if (leftKey.isPressed()) {
                dx -= 1.0;
            }
            if (upKey.isPressed()) {
                dy -= 1.0;
            }
            if (downKey.isPressed()) {
                dy += 1.0;
            }
            if (diagRightKey.isPressed()) {
                dx += 1.0;
                dy += 1.0;
            }
            if (diagLeftKey.isPressed()) {
                dx -= 1.0;
                dy += 1.0;
            }
            if (upLeftDiagKey.isPressed()) {
                dx -= 1.0;
                dy -= 1.0;
            }
            if (upRightDiagKey.isPressed()) {
                dx += 1.0;
                dy -= 1.0;
            }

            if (dx != 0.0 || dy != 0.0) {
                double scale = BASE_SPEED * (speedPercent / 100.0);
                player.changeLookDirection(dx * scale, dy * scale);
            }
        });
    }

    private KeyBinding register(String translationKey, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                InputUtil.Type.KEYSYM,
                keyCode,
                CATEGORY
        ));
    }

    private static String formatSpeedLabel(int value) {
        return "Hız: %" + value + (value >= 250 ? " (Çok Hızlı)" : "");
    }

    private static final class CameraMoveScreen extends Screen {
        private final Screen parent;

        private CameraMoveScreen(Screen parent) {
            super(Text.literal("CameraMove Ayarları"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int cy = this.height / 2;
            double progress = (double) (speedPercent - MIN_SPEED) / (double) (MAX_SPEED - MIN_SPEED);

            SliderWidget slider = new SliderWidget(
                    cx - 110,
                    cy - 20,
                    220,
                    20,
                    Text.literal(formatSpeedLabel(speedPercent)),
                    progress
            ) {
                @Override
                protected void updateMessage() {
                    int val = MIN_SPEED + (int) Math.round(this.value * (MAX_SPEED - MIN_SPEED));
                    this.setMessage(Text.literal(formatSpeedLabel(val)));
                }

                @Override
                protected void applyValue() {
                    speedPercent = MIN_SPEED + (int) Math.round(this.value * (MAX_SPEED - MIN_SPEED));
                    this.updateMessage();
                }
            };
            this.addDrawableChild(slider);

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Hızı %300 Yap"), btn -> {
                speedPercent = MAX_SPEED;
                MinecraftClient.getInstance().setScreen(new CameraMoveScreen(this.parent));
            }).dimensions(cx - 110, cy + 8, 105, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Hızı %100 Yap"), btn -> {
                speedPercent = 100;
                MinecraftClient.getInstance().setScreen(new CameraMoveScreen(this.parent));
            }).dimensions(cx + 5, cy + 8, 105, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Tamam"), btn -> this.close())
                    .dimensions(cx - 110, cy + 36, 220, 20).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 45, 0xFFFFFF);
        }

        @Override
        public void close() {
            MinecraftClient.getInstance().setScreen(this.parent);
        }
    }
}
