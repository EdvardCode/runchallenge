package com.edvardcode.runchallenge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.edvardcode.runchallenge.RunChallenge;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RunChallenge.MOD_ID, value = Dist.CLIENT)
public class KeyBindings {
    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
            "key.runchallenge.gui",
            GLFW.GLFW_KEY_R, // Клавиша R
            "key.categories.runchallenge"
    );

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (OPEN_GUI_KEY.consumeClick()) {
                net.minecraft.client.Minecraft.getInstance()
                        .setScreen(new ChallengeScreen());
            }
        }
    }

    @Mod.EventBusSubscriber(modid = RunChallenge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_GUI_KEY);
        }
    }
}