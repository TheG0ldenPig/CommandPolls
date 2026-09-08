package com.commandpoll.client;

import com.commandpoll.network.CreatePollPacket;
import com.commandpoll.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CreatePollScreen extends Screen {

    private static final Set<String> BLOCKED_COMMANDS =
            new HashSet<>(Arrays.asList(
                    "give",
                    "gamerule"
            ));

    private EditBox titleBox;
    private EditBox commandBox;
    private EditBox timerBox;

    private Button typeButton;
    private Button doneButton;
    private Button cancelButton;

    private boolean timerEnabled = true;
    private boolean commandBlocked = false;

    public CreatePollScreen() {
        super(Component.translatable(
                "screen.commandpoll.create"
        ));
    }

    @Override
    protected void init() {
        super.init();

        int guiWidth = 156;
        int guiHeight = 200;

        int left =
                (this.width - guiWidth) / 2;

        int top =
                (this.height - guiHeight) / 2;

        titleBox = new EditBox(
                this.font,
                left + 10,
                top + 25,
                guiWidth - 20,
                20,
                Component.translatable(
                        "screen.commandpoll.title"
                )
        );

        titleBox.setMaxLength(40);

        this.addRenderableWidget(titleBox);

        commandBox = new EditBox(
                this.font,
                left + 10,
                top + 67,
                guiWidth - 20,
                20,
                Component.translatable(
                        "screen.commandpoll.command"
                )
        );

        commandBox.setMaxLength(100);

        commandBox.setResponder(value ->
                updateCommandValidation()
        );

        this.addRenderableWidget(commandBox);

        typeButton = Button.builder(
                getTypeText(),
                button -> {

                    timerEnabled =
                            !timerEnabled;

                    button.setMessage(
                            getTypeText()
                    );

                    if (timerBox != null) {

                        timerBox.active =
                                timerEnabled;

                        timerBox.setTextColor(
                                timerEnabled
                                        ? 0xFFFFFFFF
                                        : 0xFF777777
                        );
                    }
                }
        ).bounds(
                left + 10,
                top + 105,
                guiWidth - 20,
                20
        ).build();

        this.addRenderableWidget(typeButton);

        timerBox = new EditBox(
                this.font,
                left + 10,
                top + 130,
                guiWidth - 20,
                20,
                Component.translatable(
                        "screen.commandpoll.timer"
                )
        );

        timerBox.setMaxLength(5);
        timerBox.setValue("00:30");
        timerBox.active = timerEnabled;

        timerBox.setTextColor(
                timerEnabled
                        ? 0xFFFFFFFF
                        : 0xFF777777
        );

        this.addRenderableWidget(timerBox);

        doneButton = Button.builder(
                Component.translatable(
                        "screen.commandpoll.done"
                ),
                button -> createPoll()
        ).bounds(
                left + 10,
                top + 164,
                65,
                20
        ).build();

        this.addRenderableWidget(doneButton);

        cancelButton = Button.builder(
                Component.translatable(
                        "screen.commandpoll.cancel"
                ),
                button -> this.onClose()
        ).bounds(
                left + 81,
                top + 164,
                65,
                20
        ).build();

        cancelButton.setAlpha(0.65F);

        this.addRenderableWidget(cancelButton);

        updateCommandValidation();
    }

    private void createPoll() {

        updateCommandValidation();

        if (commandBlocked) {
            return;
        }

        String title =
                titleBox.getValue().trim();

        String command =
                commandBox.getValue().trim();

        if (title.isEmpty()) {
            return;
        }

        if (command.isEmpty()) {
            return;
        }

        int remainingSeconds = 0;

        if (timerEnabled) {

            String timerText =
                    timerBox.getValue().trim();

            try {

                String[] parts =
                        timerText.split(":");

                if (parts.length != 2) {
                    return;
                }

                int minutes =
                        Integer.parseInt(
                                parts[0]
                        );

                int seconds =
                        Integer.parseInt(
                                parts[1]
                        );

                if (minutes < 0 ||
                        seconds < 0 ||
                        seconds > 59) {
                    return;
                }

                remainingSeconds =
                        (minutes * 60) + seconds;

                if (remainingSeconds <= 0) {
                    return;
                }

            } catch (
                    NumberFormatException exception
            ) {
                return;
            }
        }

        ModNetwork.CHANNEL.sendToServer(
                new CreatePollPacket(
                        title,
                        command,
                        timerEnabled,
                        remainingSeconds
                )
        );

        this.onClose();
    }

    private void updateCommandValidation() {

        if (commandBox == null ||
                doneButton == null) {
            return;
        }

        String command =
                commandBox.getValue().trim();

        commandBlocked =
                isBlockedCommand(command);

        doneButton.active =
                !commandBlocked;
    }

    private boolean isBlockedCommand(
            String command
    ) {

        if (command.isEmpty()) {
            return false;
        }

        if (command.startsWith("/")) {

            command =
                    command.substring(1)
                            .trim();
        }

        if (command.isEmpty()) {
            return false;
        }

        String[] parts =
                command.toLowerCase()
                        .split("\\s+");

        if (parts.length == 0) {
            return false;
        }

        String mainCommand =
                parts[0].replaceAll(
                        "^[^a-z0-9]+|[^a-z0-9]+$",
                        ""
                );

        return BLOCKED_COMMANDS.contains(
                mainCommand
        ) || mainCommand.equals(
                "gamemode"
        );
    }

    private Component getTypeText() {

        return timerEnabled
                ? Component.translatable(
                "screen.commandpoll.type.timer"
        )
                : Component.translatable(
                "screen.commandpoll.type.majority"
        );
    }

    @Override
    public void resize(
            Minecraft minecraft,
            int width,
            int height
    ) {

        String savedTitle =
                titleBox != null
                        ? titleBox.getValue()
                        : "";

        String savedCommand =
                commandBox != null
                        ? commandBox.getValue()
                        : "";

        String savedTimer =
                timerBox != null
                        ? timerBox.getValue()
                        : "00:30";

        super.resize(
                minecraft,
                width,
                height
        );

        if (titleBox != null) {
            titleBox.setValue(
                    savedTitle
            );
        }

        if (commandBox != null) {
            commandBox.setValue(
                    savedCommand
            );
        }

        if (timerBox != null) {

            timerBox.setValue(
                    savedTimer
            );

            timerBox.active =
                    timerEnabled;

            timerBox.setTextColor(
                    timerEnabled
                            ? 0xFFFFFFFF
                            : 0xFF777777
            );
        }

        updateCommandValidation();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        guiGraphics.fill(
                0,
                0,
                this.width,
                this.height,
                0x80000000
        );

        int guiWidth = 156;
        int guiHeight = 200;

        int left =
                (this.width - guiWidth) / 2;

        int top =
                (this.height - guiHeight) / 2;

        guiGraphics.fill(
                left,
                top,
                left + guiWidth,
                top + guiHeight,
                0xFF0F0B07
        );

        int texturePixel = 3;

        int[] dirtColors = {
                0xFF0B0805,
                0xFF0F0B07,
                0xFF130D09,
                0xFF17110B,
                0xFF111111,
                0xFF0D0D0D
        };

        for (
                int x = left;
                x < left + guiWidth;
                x += texturePixel
        ) {

            for (
                    int y = top;
                    y < top + guiHeight;
                    y += texturePixel
            ) {

                int gridX =
                        (x - left) / texturePixel;

                int gridY =
                        (y - top) / texturePixel;

                int hash =
                        gridX * 374761393 +
                                gridY * 668265263;

                hash =
                        (hash ^ (hash >> 13))
                                * 1274126177;

                hash ^= (hash >> 16);

                int value =
                        Math.abs(hash % 100);

                int color;

                if (value < 43) {
                    color = dirtColors[0];
                } else if (value < 72) {
                    color = dirtColors[1];
                } else if (value < 88) {
                    color = dirtColors[2];
                } else if (value < 96) {
                    color = dirtColors[3];
                } else if (value < 98) {
                    color = dirtColors[4];
                } else {
                    color = dirtColors[5];
                }

                int pixelWidth =
                        texturePixel;

                int pixelHeight =
                        texturePixel;

                if (value % 17 == 0) {
                    pixelWidth += 2;
                }

                if (value % 23 == 0) {
                    pixelHeight += 2;
                }

                guiGraphics.fill(
                        x,
                        y,
                        Math.min(
                                x + pixelWidth,
                                left + guiWidth
                        ),
                        Math.min(
                                y + pixelHeight,
                                top + guiHeight
                        ),
                        color
                );
            }
        }

        guiGraphics.fill(
                left,
                top,
                left + guiWidth,
                top + 1,
                0xFFFFFFFF
        );

        guiGraphics.fill(
                left,
                top,
                left + 1,
                top + guiHeight,
                0xFFFFFFFF
        );

        guiGraphics.fill(
                left,
                top + guiHeight - 1,
                left + guiWidth,
                top + guiHeight,
                0xFF555555
        );

        guiGraphics.fill(
                left + guiWidth - 1,
                top,
                left + guiWidth,
                top + guiHeight,
                0xFF555555
        );

        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "screen.commandpoll.title"
                ),
                left + 10,
                top + 12,
                0xFFFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "screen.commandpoll.command"
                ),
                left + 10,
                top + 54,
                0xFFFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "screen.commandpoll.type"
                ),
                left + 10,
                top + 95,
                0xFFFFFFFF
        );

        if (!timerEnabled) {

            guiGraphics.fill(
                    left + 10,
                    top + 130,
                    left + guiWidth - 10,
                    top + 150,
                    0xFF303030
            );

            guiGraphics.drawString(
                    this.font,
                    timerBox.getValue(),
                    left + 14,
                    top + 136,
                    0xFF777777
            );
        }

        updateCommandValidation();

        if (commandBlocked) {

            Component warning =
                    Component.translatable(
                            "screen.commandpoll.invalid_command"
                    );

            int warningWidth =
                    this.font.width(
                            warning
                    );

            guiGraphics.drawString(
                    this.font,
                    warning,
                    left +
                            (guiWidth - warningWidth) / 2,
                    top + 154,
                    0xFFFF5555
            );
        }

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }
}