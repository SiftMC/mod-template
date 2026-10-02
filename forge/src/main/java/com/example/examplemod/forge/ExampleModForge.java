package com.example.examplemod.forge;

import com.example.examplemod.ExampleMod;

//? if >=1.14.4 {
@net.minecraftforge.fml.common.Mod(ExampleMod.MOD_ID)
//?} else {
/*@cpw.mods.fml.common.Mod(modid = ExampleMod.MOD_ID)
*///?}
public final class ExampleModForge {
    public ExampleModForge() {
        ExampleMod.init();
        //? if <1.14.4 {
        /*cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);
        *///?}
    }

    // Forge 1.7.10 has no Mixin, so there the server greets the player instead of the mixin in common.
    //? if <1.14.4 {
    /*@cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void greet(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        String brand = cpw.mods.fml.common.FMLCommonHandler.instance().getModName();
        String greeting = ExampleMod.greeting(brand, cpw.mods.fml.common.Loader.MC_VERSION);
        event.player.addChatMessage(new net.minecraft.util.ChatComponentText(greeting));
    }
    *///?}
}
