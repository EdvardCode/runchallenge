package com.edvardcode.runchallenge.effects;

import com.edvardcode.runchallenge.RunChallenge;
import com.edvardcode.runchallenge.handler.PlayerEventHandler;
import com.edvardcode.runchallenge.tracker.RunTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RunChallenge.MOD_ID)
public class FireworkManager {
    private static final int FIREWORK_COUNT = 5;
    private static final int SALVOS = 5;
    private static final Map<UUID, Boolean> fireworksSpawned = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            RunTracker tracker = PlayerEventHandler.getTracker(player);
            UUID playerUUID = player.getUUID();

            if (tracker != null && tracker.isCompleted() && !fireworksSpawned.getOrDefault(playerUUID, false)) {
                spawnFireworks(player, tracker);
                fireworksSpawned.put(playerUUID, true);
                tracker.stopChallenge();
            }

            if (tracker == null || !tracker.isCompleted()) {
                fireworksSpawned.remove(playerUUID);
            }
        }
    }

    private static void spawnFireworks(ServerPlayer player, RunTracker tracker) {
        player.sendSystemMessage(Component.translatable(
                "runchallenge.chat.completed",
                String.format("%.0f", tracker.getRequiredDistance())
        ));

        for (int salvo = 0; salvo < SALVOS; salvo++) {
            player.getServer().tell(new net.minecraft.server.TickTask(
                    salvo * 15,
                    () -> {
                        for (int i = 0; i < FIREWORK_COUNT; i++) {
                            ItemStack fireworkStack = createRandomFirework();
                            FireworkRocketEntity firework = new FireworkRocketEntity(
                                    player.level(),
                                    player.getX() + (player.getRandom().nextDouble() - 0.5) * 8,
                                    player.getY() + 2 + player.getRandom().nextDouble() * 4,
                                    player.getZ() + (player.getRandom().nextDouble() - 0.5) * 8,
                                    fireworkStack
                            );
                            player.level().addFreshEntity(firework);
                        }
                    }
            ));
        }
    }

    private static ItemStack createRandomFirework() {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag tag = firework.getOrCreateTagElement("Fireworks");
        tag.putByte("Flight", (byte) (1 + (int)(Math.random() * 2)));

        ListTag explosions = new ListTag();
        CompoundTag explosion = new CompoundTag();

        int[] colors = new int[4 + (int)(Math.random() * 4)];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = (int)(Math.random() * 0xFFFFFF);
        }
        explosion.putIntArray("Colors", colors);

        int[] fadeColors = new int[1 + (int)(Math.random() * 3)];
        for (int i = 0; i < fadeColors.length; i++) {
            fadeColors[i] = (int)(Math.random() * 0xFFFFFF);
        }
        explosion.putIntArray("FadeColors", fadeColors);

        explosion.putByte("Type", (byte) (Math.random() * 5));
        explosion.putBoolean("Trail", Math.random() > 0.3);
        explosion.putBoolean("Flicker", Math.random() > 0.3);
        explosions.add(explosion);
        tag.put("Explosions", explosions);

        return firework;
    }
}