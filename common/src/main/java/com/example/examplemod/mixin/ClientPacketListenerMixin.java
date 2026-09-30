package com.example.examplemod.mixin;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.19 {
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
//?} else {
/*import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.util.ChatComponentText;
*///?}

/** Greets the player after joining a world or server. Every loader brands the client, so this needs no loader API. */
//? if >=1.19 {
@Mixin(ClientPacketListener.class)
//?} else {
/*@Mixin(NetHandlerPlayClient.class)
*///?}
public abstract class ClientPacketListenerMixin {
    //? if >=1.19 {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    //?} else {
    /*@Inject(method = "handleJoinGame", at = @At("TAIL"))
    *///?}
    private void examplemod$greet(CallbackInfo ci) {
        //? if >=1.21.6 {
        String version = SharedConstants.getCurrentVersion().name();
        //?} elif >=1.19 {
        /*String version = SharedConstants.getCurrentVersion().getName();
        *///?} else {
        /*String version = RealmsSharedConstants.VERSION_STRING;
        *///?}
        String greeting = "Hello from " + ExampleMod.MOD_ID + " on " + ClientBrandRetriever.getClientModName() + " " + version + "!";
        //? if >=1.19 {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(greeting));
        //?} else {
        /*Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(greeting));
        *///?}
    }
}
