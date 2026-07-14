package com.edvardcode.runchallenge.bossbar;

import com.edvardcode.runchallenge.RunChallenge;
import com.edvardcode.runchallenge.handler.PlayerEventHandler;
import com.edvardcode.runchallenge.tracker.RunTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RunChallenge.MOD_ID)
public class BossBarManager {

    private static final Map<UUID, ServerBossEvent> bossBars = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            RunTracker tracker = PlayerEventHandler.getTracker(player);

            if (tracker != null && tracker.isActive()) {
                ServerBossEvent bossBar = bossBars.computeIfAbsent(
                        player.getUUID(),
                        k -> {
                            ServerBossEvent bar = new ServerBossEvent(
                                    Component.translatable("runchallenge.bossbar.title", 0, 0),
                                    BossEvent.BossBarColor.BLUE,
                                    BossEvent.BossBarOverlay.PROGRESS
                            );
                            bar.addPlayer(player);
                            bar.setVisible(true);
                            return bar;
                        }
                );

                double progress = tracker.getProgress();
                bossBar.setProgress((float) progress);

                if (tracker.isCompleted()) {
                    bossBar.setName(Component.translatable("runchallenge.bossbar.completed"));
                    bossBar.setColor(BossEvent.BossBarColor.GREEN);
                } else {
                    bossBar.setName(Component.translatable(
                            "runchallenge.bossbar.title",
                            String.format("%.1f", tracker.getTotalDistance()),
                            String.format("%.0f", tracker.getRequiredDistance())
                    ));
                    bossBar.setColor(BossEvent.BossBarColor.BLUE);
                }
            } else {
                removeBossBar(player);
            }
        }
    }

    public static void removeBossBar(ServerPlayer player) {
        ServerBossEvent bossBar = bossBars.remove(player.getUUID());
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            bossBar.setVisible(false);
        }
    }
}