package com.edvardcode.runchallenge.handler;

import com.edvardcode.runchallenge.RunChallenge;
import com.edvardcode.runchallenge.tracker.RunTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RunChallenge.MOD_ID)
public class PlayerEventHandler {
    private static final Map<UUID, RunTracker> playerTrackers = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        RunTracker tracker = playerTrackers.get(player.getUUID());
        if (tracker != null && tracker.isActive()) {
            tracker.updatePosition(player.getX(), player.getZ());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        playerTrackers.remove(player.getUUID());
    }

    public static RunTracker getTracker(Player player) {
        return playerTrackers.get(player.getUUID());
    }

    public static void setTracker(Player player, RunTracker tracker) {
        playerTrackers.put(player.getUUID(), tracker);
    }
}