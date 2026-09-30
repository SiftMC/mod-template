package com.example.examplemod.mixin;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Greets the player after joining a world or server. Every loader brands the client, so this needs no loader API. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void examplemod$greet(CallbackInfo ci) {
        Minecraft.getInstance().player.sendSystemMessage(
            Component.literal("Hello from " + ExampleMod.MOD_ID + " on " + ClientBrandRetriever.getClientModName() + "!"));
    }
}
