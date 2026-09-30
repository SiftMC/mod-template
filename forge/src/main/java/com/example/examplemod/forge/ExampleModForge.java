package com.example.examplemod.forge;

import com.example.examplemod.ExampleMod;

//? if >=1.14 {
@net.minecraftforge.fml.common.Mod(ExampleMod.MOD_ID)
//?} else {
/*@cpw.mods.fml.common.Mod(modid = ExampleMod.MOD_ID)
*///?}
public final class ExampleModForge {
    public ExampleModForge() {
        ExampleMod.init();
    }
}
