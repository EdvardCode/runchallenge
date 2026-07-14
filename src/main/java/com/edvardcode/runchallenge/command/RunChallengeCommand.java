package com.edvardcode.runchallenge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.edvardcode.runchallenge.RunChallenge;
import com.edvardcode.runchallenge.handler.PlayerEventHandler;
import com.edvardcode.runchallenge.tracker.RunTracker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class RunChallengeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("runchallenge")
                        .then(Commands.literal("start")
                                .executes(context -> startChallenge(context.getSource(), 10000, null))
                                .then(Commands.argument("distance", DoubleArgumentType.doubleArg(1))
                                        .executes(context -> startChallenge(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "distance"),
                                                null
                                        ))
                                        .then(Commands.argument("player", StringArgumentType.string())
                                                .executes(context -> startChallenge(
                                                        context.getSource(),
                                                        DoubleArgumentType.getDouble(context, "distance"),
                                                        StringArgumentType.getString(context, "player")
                                                ))
                                        )
                                )
                        )
                        .then(Commands.literal("stop")
                                .executes(context -> stopChallenge(context.getSource(), null))
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .executes(context -> stopChallenge(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player")
                                        ))
                                )
                        )
                        .then(Commands.literal("status")
                                .executes(context -> showStatus(context.getSource(), null))
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .executes(context -> showStatus(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player")
                                        ))
                                )
                        )
                        .then(Commands.literal("tpstart")
                                .executes(context -> tpToStart(context.getSource(), null))
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .executes(context -> tpToStart(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player")
                                        ))
                                )
                        )
                        .then(Commands.literal("pushback")
                                .executes(context -> pushBack(context.getSource(), null))
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .executes(context -> pushBack(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player")
                                        ))
                                )
                        )
                        .then(Commands.literal("players")
                                .executes(context -> listPlayers(context.getSource()))
                        )
        );
    }

    /**
     * Получает целевого игрока по имени
     */
    private static ServerPlayer getTargetPlayer(CommandSourceStack source, String playerName) {
        if (playerName != null && !playerName.isEmpty()) {
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target != null) {
                return target;
            }
            source.sendFailure(Component.literal("§cИгрок не найден: " + playerName));
            return null;
        }
        if (source.getEntity() instanceof ServerPlayer) {
            return (ServerPlayer) source.getEntity();
        }
        source.sendFailure(Component.translatable("runchallenge.command.player_only"));
        return null;
    }

    /**
     * Запуск челленджа
     */
    private static int startChallenge(CommandSourceStack source, double distance, String playerName) {
        ServerPlayer player = getTargetPlayer(source, playerName);
        if (player == null) return 0;

        RunTracker tracker = new RunTracker();
        tracker.startChallenge(distance);
        tracker.setStartPoint(player.getX(), player.getZ());
        PlayerEventHandler.setTracker(player, tracker);

        source.sendSuccess(
                () -> Component.translatable("runchallenge.command.start", player.getName().getString(), distance),
                false
        );

        if (source.getEntity() != player) {
            player.sendSystemMessage(Component.translatable("runchallenge.command.start_self", distance));
        }

        return 1;
    }

    /**
     * Остановка челленджа
     */
    private static int stopChallenge(CommandSourceStack source, String playerName) {
        ServerPlayer player = getTargetPlayer(source, playerName);
        if (player == null) return 0;

        RunTracker tracker = PlayerEventHandler.getTracker(player);
        if (tracker != null) {
            tracker.stopChallenge();
            source.sendSuccess(
                    () -> Component.translatable("runchallenge.command.stop", player.getName().getString()),
                    false
            );
        } else {
            source.sendSuccess(
                    () -> Component.translatable("runchallenge.command.no_challenge_player", player.getName().getString()),
                    false
            );
        }
        return 1;
    }

    /**
     * Показ статуса челленджа
     */
    private static int showStatus(CommandSourceStack source, String playerName) {
        ServerPlayer player = getTargetPlayer(source, playerName);
        if (player == null) return 0;

        RunTracker tracker = PlayerEventHandler.getTracker(player);
        if (tracker != null && tracker.isActive()) {
            source.sendSuccess(
                    () -> Component.translatable(
                            "runchallenge.command.status",
                            player.getName().getString(),
                            tracker.getTotalDistance(),
                            tracker.getRequiredDistance(),
                            tracker.getProgress() * 100
                    ),
                    false
            );
        } else {
            source.sendSuccess(
                    () -> Component.translatable("runchallenge.command.no_challenge_player", player.getName().getString()),
                    false
            );
        }
        return 1;
    }

    /**
     * Телепорт игрока в начало забега
     */
    private static int tpToStart(CommandSourceStack source, String playerName) {
        ServerPlayer player = getTargetPlayer(source, playerName);
        if (player == null) return 0;

        RunTracker tracker = PlayerEventHandler.getTracker(player);
        if (tracker != null && tracker.isActive()) {
            double startX = tracker.getStartX();
            double startZ = tracker.getStartZ();
            BlockPos pos = new BlockPos((int)startX, 0, (int)startZ);
            int height = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            player.teleportTo(startX, height, startZ);

            source.sendSuccess(
                    () -> Component.translatable("runchallenge.command.tpstart", player.getName().getString()),
                    false
            );

            if (source.getEntity() != player) {
                player.sendSystemMessage(Component.translatable("runchallenge.command.tpstart_self"));
            }
        } else {
            source.sendFailure(Component.translatable("runchallenge.command.no_challenge_player", player.getName().getString()));
        }
        return 1;
    }

    /**
     * Откинуть игрока назад на 10 блоков
     */
    private static int pushBack(CommandSourceStack source, String playerName) {
        ServerPlayer player = getTargetPlayer(source, playerName);
        if (player == null) return 0;

        RunTracker tracker = PlayerEventHandler.getTracker(player);
        if (tracker != null && tracker.isActive()) {
            Vec3 currentPos = player.position();
            double startX = tracker.getStartX();
            double startZ = tracker.getStartZ();

            double dx = currentPos.x - startX;
            double dz = currentPos.z - startZ;
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                double pushX = -(dx / distance) * 10;
                double pushZ = -(dz / distance) * 10;

                player.teleportTo(
                        currentPos.x + pushX,
                        currentPos.y,
                        currentPos.z + pushZ
                );

                player.sendSystemMessage(Component.translatable("runchallenge.command.pushback_self"));

                source.sendSuccess(
                        () -> Component.translatable("runchallenge.command.pushback_target", player.getName().getString()),
                        false
                );
            } else {
                source.sendFailure(Component.literal("§cИгрок находится в точке старта, некуда откидывать!"));
            }
        } else {
            source.sendFailure(Component.translatable("runchallenge.command.no_challenge_player", player.getName().getString()));
        }
        return 1;
    }

    /**
     * Список игроков онлайн
     */
    private static int listPlayers(CommandSourceStack source) {
        Collection<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();

        if (players.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal("§7Нет игроков онлайн"),
                    false
            );
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("§eИгроки онлайн: §f" + players.size()),
                false
        );

        for (ServerPlayer player : players) {
            RunTracker tracker = PlayerEventHandler.getTracker(player);
            String status = (tracker != null && tracker.isActive())
                    ? " §a[В забеге: " + String.format("%.0f", tracker.getTotalDistance()) + "/" + String.format("%.0f", tracker.getRequiredDistance()) + "]"
                    : " §7[Нет забега]";

            source.sendSuccess(
                    () -> Component.literal("  §6" + player.getName().getString() + status),
                    false
            );
        }

        return 1;
    }
}