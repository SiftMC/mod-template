package com.example.examplemod.mixin;

import com.example.examplemod.ExampleMod;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ClientPacketListenerMixin greets the player in chat after joining a world or server.
 * Every loader brands the client, so the greeting needs no loader API.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void greet(CallbackInfo ci) {
        //? if >=1.21.6 {
        String version = SharedConstants.getCurrentVersion().name();
        //?} else {
        /*String version = SharedConstants.getCurrentVersion().getName();
        *///?}
        String greeting = ExampleMod.greeting(ClientBrandRetriever.getClientModName(), version);
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(greeting));
    }
}
