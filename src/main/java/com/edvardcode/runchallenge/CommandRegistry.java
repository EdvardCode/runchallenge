package com.edvardcode.runchallenge;

import com.edvardcode.runchallenge.command.RunChallengeCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RunChallenge.MOD_ID)
public class CommandRegistry {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RunChallengeCommand.register(event.getDispatcher());
    }
}