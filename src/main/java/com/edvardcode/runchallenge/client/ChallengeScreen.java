package com.edvardcode.runchallenge.client;

import com.edvardcode.runchallenge.RunChallenge;
import com.edvardcode.runchallenge.network.ChallengePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ChallengeScreen extends Screen {
    private EditBox distanceInput;
    private CycleButton<String> playerSelector;
    private List<String> onlinePlayers = new ArrayList<>();

    private static String lastSelectedPlayer = "";

    public ChallengeScreen() {
        super(Component.translatable("runchallenge.gui.title"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        updatePlayerList();

        String initialPlayer = "";
        if (!lastSelectedPlayer.isEmpty() && onlinePlayers.contains(lastSelectedPlayer)) {
            initialPlayer = lastSelectedPlayer;
        } else if (!onlinePlayers.isEmpty()) {
            initialPlayer = onlinePlayers.get(0);
        }

        int selectorWidth = 150;
        int selectorX = centerX - selectorWidth / 2;

        this.playerSelector = CycleButton.<String>builder(name -> Component.literal(name))
                .withValues(onlinePlayers)
                .withInitialValue(initialPlayer)
                .create(
                        selectorX, centerY - 55,
                        selectorWidth, 20,
                        Component.empty(),
                        (button, value) -> {
                            lastSelectedPlayer = value;
                        }
                );
        this.addRenderableWidget(this.playerSelector);

        int inputWidth = 150;
        int inputX = centerX - inputWidth / 2;

        this.distanceInput = new EditBox(
                this.font,
                inputX, centerY - 15,
                inputWidth, 20,
                Component.literal("10000")
        );
        this.distanceInput.setValue("10000");
        this.distanceInput.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.distanceInput);

        int buttonWidth = 70;
        int buttonHeight = 20;
        int buttonSpacing = 5;
        int totalButtonsWidth = buttonWidth * 2 + buttonSpacing;
        int buttonsStartX = centerX - totalButtonsWidth / 2;

        Button startButton = Button.builder(
                        Component.translatable("runchallenge.gui.start"),
                        button -> {
                            String distanceText = distanceInput.getValue();
                            String selectedPlayer = playerSelector.getValue();
                            if (!distanceText.isEmpty() && selectedPlayer != null && !selectedPlayer.isEmpty()) {
                                int distance = Integer.parseInt(distanceText);
                                if (distance > 0) {
                                    ChallengePacket.sendToServer("start", distance, selectedPlayer);
                                    showSuccessMessage("§aЧеллендж запущен!");
                                }
                            }
                        }
                ).pos(buttonsStartX, centerY + 10)
                .size(buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(startButton);

        Button stopButton = Button.builder(
                        Component.translatable("runchallenge.gui.stop"),
                        button -> {
                            String selectedPlayer = playerSelector.getValue();
                            if (selectedPlayer != null && !selectedPlayer.isEmpty()) {
                                ChallengePacket.sendToServer("stop", 0, selectedPlayer);
                                showSuccessMessage("§cЧеллендж остановлен!");
                            }
                        }
                ).pos(buttonsStartX + buttonWidth + buttonSpacing, centerY + 10)
                .size(buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(stopButton);

        int totalThreeButtonsWidth = buttonWidth * 3 + buttonSpacing * 2;
        int threeButtonsStartX = centerX - totalThreeButtonsWidth / 2;

        Button tpStartButton = Button.builder(
                        Component.translatable("runchallenge.gui.tpstart"),
                        button -> {
                            String selectedPlayer = playerSelector.getValue();
                            if (selectedPlayer != null && !selectedPlayer.isEmpty()) {
                                ChallengePacket.sendToServer("tpstart", 0, selectedPlayer);
                                showSuccessMessage("§bТелепорт в начало!");
                            }
                        }
                ).pos(threeButtonsStartX, centerY + 35)
                .size(buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(tpStartButton);

        Button pushBackButton = Button.builder(
                        Component.translatable("runchallenge.gui.pushback"),
                        button -> {
                            String selectedPlayer = playerSelector.getValue();
                            if (selectedPlayer != null && !selectedPlayer.isEmpty()) {
                                ChallengePacket.sendToServer("pushback", 0, selectedPlayer);
                                showSuccessMessage("§dОткинут назад!");
                            }
                        }
                ).pos(threeButtonsStartX + buttonWidth + buttonSpacing, centerY + 35)
                .size(buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(pushBackButton);

        Button statusButton = Button.builder(
                        Component.translatable("runchallenge.gui.status"),
                        button -> {
                            String selectedPlayer = playerSelector.getValue();
                            if (minecraft != null && minecraft.player != null && selectedPlayer != null) {
                                minecraft.player.connection.sendCommand("runchallenge status " + selectedPlayer);
                            }
                        }
                ).pos(threeButtonsStartX + (buttonWidth + buttonSpacing) * 2, centerY + 35)
                .size(buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(statusButton);

        int refreshButtonWidth = 100;

        Button refreshButton = Button.builder(
                        Component.translatable("runchallenge.gui.refresh"),
                        button -> {
                            updatePlayerList();
                            if (!onlinePlayers.isEmpty()) {
                                removeWidget(playerSelector);

                                String currentValue = lastSelectedPlayer;
                                if (!onlinePlayers.contains(currentValue)) {
                                    currentValue = onlinePlayers.get(0);
                                }

                                playerSelector = CycleButton.<String>builder(name -> Component.literal(name))
                                        .withValues(onlinePlayers)
                                        .withInitialValue(currentValue)
                                        .create(
                                                selectorX, centerY - 55,
                                                selectorWidth, 20,
                                                Component.empty(),
                                                (btn, value) -> lastSelectedPlayer = value
                                        );
                                addRenderableWidget(playerSelector);
                            }
                            showSuccessMessage("§eСписок обновлён!");
                        }
                ).pos(centerX - refreshButtonWidth - 5, centerY + 65)
                .size(refreshButtonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(refreshButton);

        Button closeButton = Button.builder(
                        Component.translatable("runchallenge.gui.close"),
                        button -> this.onClose()
                ).pos(centerX + 5, centerY + 65)
                .size(refreshButtonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(closeButton);
    }

    private void updatePlayerList() {
        onlinePlayers.clear();
        if (minecraft != null && minecraft.player != null) {
            for (var playerInfo : minecraft.player.connection.getOnlinePlayers()) {
                onlinePlayers.add(playerInfo.getProfile().getName());
            }

            if (onlinePlayers.isEmpty()) {
                onlinePlayers.add(minecraft.player.getName().getString());
            }
        }
    }

    private void showSuccessMessage(String message) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.drawCenteredString(this.font,
                Component.translatable("runchallenge.gui.title"),
                centerX, centerY - 85, 0xFFD700);

        guiGraphics.drawCenteredString(this.font,
                Component.translatable("runchallenge.gui.playername"),
                centerX, centerY - 70, 0xAAAAAA);

        guiGraphics.drawCenteredString(this.font,
                Component.translatable("runchallenge.gui.distance"),
                centerX, centerY - 30, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.distanceInput != null) {
            this.distanceInput.tick();
        }
    }
}