package com.example.cameramove;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class CameraMoveClient implements ClientModInitializer {
    private boolean active = false;
    private boolean jumpWasDown = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) {
                return;
            }

            boolean jumpDown = client.options.jumpKey.isPressed();
            if (jumpDown && !jumpWasDown) {
                active = !active;
            }
            jumpWasDown = jumpDown;

            if (active) {
                player.changeLookDirection(24.0, 0.0);
            }
        });
    }
}
