package com.edvardcode.runchallenge.network;

import com.edvardcode.runchallenge.RunChallenge;
import com.edvardcode.runchallenge.handler.PlayerEventHandler;
import com.edvardcode.runchallenge.tracker.RunTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ChallengePacket {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new net.minecraft.resources.ResourceLocation(RunChallenge.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private final String action;
    private final int distance;
    private final String playerName;

    public ChallengePacket(String action, int distance, String playerName) {
        this.action = action;
        this.distance = distance;
        this.playerName = playerName;
    }

    public static void register() {
        CHANNEL.registerMessage(0, ChallengePacket.class,
                (msg, buf) -> {
                    buf.writeUtf(msg.action);
                    buf.writeInt(msg.distance);
                    buf.writeUtf(msg.playerName);
                },
                buf -> new ChallengePacket(buf.readUtf(), buf.readInt(), buf.readUtf()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer sender = ctx.get().getSender();
                        if (sender != null) {
                            handleServer(msg, sender);
                        }
                    });
                    ctx.get().setPacketHandled(true);
                }
        );
    }

    private static void handleServer(ChallengePacket msg, ServerPlayer sender) {
        ServerPlayer targetPlayer = sender.getServer().getPlayerList()
                .getPlayerByName(msg.playerName);

        if (targetPlayer == null) {
            targetPlayer = sender;
        }

        RunTracker tracker = PlayerEventHandler.getTracker(targetPlayer);

        switch (msg.action) {
            case "start":
                RunTracker newTracker = new RunTracker();
                newTracker.startChallenge(msg.distance);
                newTracker.setStartPoint(targetPlayer.getX(), targetPlayer.getZ());
                PlayerEventHandler.setTracker(targetPlayer, newTracker);
                break;

            case "stop":
                if (tracker != null) {
                    tracker.stopChallenge();
                }
                break;

            case "tpstart":
                if (tracker != null && tracker.isActive()) {
                    double startX = tracker.getStartX();
                    double startZ = tracker.getStartZ();
                    BlockPos pos = new BlockPos((int)startX, 0, (int)startZ);
                    int height = targetPlayer.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
                    targetPlayer.teleportTo(startX, height, startZ);
                }
                break;

            case "pushback":
                if (tracker != null && tracker.isActive()) {
                    Vec3 currentPos = targetPlayer.position();
                    double startX = tracker.getStartX();
                    double startZ = tracker.getStartZ();

                    double dx = currentPos.x - startX;
                    double dz = currentPos.z - startZ;
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance > 0) {
                        double pushX = -(dx / distance) * 10;
                        double pushZ = -(dz / distance) * 10;

                        targetPlayer.teleportTo(
                                currentPos.x + pushX,
                                currentPos.y,
                                currentPos.z + pushZ
                        );

                        targetPlayer.sendSystemMessage(
                                net.minecraft.network.chat.Component.translatable("runchallenge.chat.pushback")
                        );
                    }
                }
                break;
        }
    }

    public static void sendToServer(String action, int distance, String playerName) {
        CHANNEL.sendToServer(new ChallengePacket(action, distance, playerName));
    }
}