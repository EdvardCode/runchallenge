package com.edvardcode.runchallenge;

import com.edvardcode.runchallenge.network.ChallengePacket;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RunChallenge.MOD_ID)
public class RunChallenge {
    public static final String MOD_ID = "runchallenge";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RunChallenge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("RunChallenge Mod Initialized");
        ChallengePacket.register();
    }
}