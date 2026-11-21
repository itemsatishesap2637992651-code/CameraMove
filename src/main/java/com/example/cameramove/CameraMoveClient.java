package com.example.cameramove;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class CameraMoveClient implements ClientModInitializer {
    private boolean rotating = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        if (client.options.jumpKey.wasPressed()) {
            rotating = !rotating;
        }

        if (rotating) {
            float currentYaw = player.getYaw();
            player.setYaw(currentYaw + 2.0f);
        }
    }
}
