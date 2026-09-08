package com.commandpoll.client;

import com.commandpoll.network.ModNetwork;
import com.commandpoll.network.VotePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(
        modid = "commandpoll",
        value = Dist.CLIENT
)
public class PollHud {

    private static final Minecraft MINECRAFT =
            Minecraft.getInstance();

    private static boolean visible = false;

    private static String title = "";
    private static String command = "";

    private static boolean timerEnabled;
    private static int remainingSeconds;

    private static int yesVotes;
    private static int noVotes;
    private static int totalPlayers;

    private static Boolean personalVote = null;
    private static long personalVoteTime = 0L;
    private static boolean personalVoteLocked = false;

    private static boolean messageVisible = false;
    private static String messageKey = "";
    private static boolean messageYes = false;
    private static boolean messageHighlightVoteWord = false;
    private static long messageUntil = 0L;

    private static final int WIDTH = 235;
    private static final int HEIGHT = 65;
    private static final int MARGIN = 8;

    private static final int VOTE_WIDTH = 30;
    private static final int VOTE_HEIGHT = 12;

    private static final int BAR_WIDTH = 110;
    private static final int BAR_HEIGHT = 7;

    private static final long MESSAGE_TIME = 2000L;

    public static void showPoll(
            String newTitle,
            String newCommand,
            String newCreatorName,
            boolean newTimerEnabled,
            int newRemainingSeconds,
            int newYesVotes,
            int newNoVotes,
            int newTotalPlayers
    ) {
        boolean wasVisible =
                visible;

        title = newTitle;
        command = newCommand;
        timerEnabled = newTimerEnabled;
        remainingSeconds = newRemainingSeconds;
        yesVotes = newYesVotes;
        noVotes = newNoVotes;
        totalPlayers = newTotalPlayers;

        visible = true;

        if (!wasVisible) {
            personalVote = null;
            personalVoteTime = 0L;
            personalVoteLocked = false;
        }
    }

    public static void endPoll() {
        visible = false;

        personalVote = null;
        personalVoteTime = 0L;
        personalVoteLocked = false;
    }

    public static void showMessage(
            String newMessageKey,
            boolean yes,
            boolean highlightVoteWord
    ) {
        messageKey = newMessageKey;
        messageYes = yes;
        messageHighlightVoteWord =
                highlightVoteWord;

        messageVisible = true;

        messageUntil =
                System.currentTimeMillis()
                        + MESSAGE_TIME;
    }

    @SubscribeEvent
    public static void render(
            RenderGuiOverlayEvent.Post event
    ) {
        if (MINECRAFT.player == null) {
            return;
        }

        GuiGraphics graphics =
                event.getGuiGraphics();

        if (visible) {
            renderPoll(graphics);
        }

        if (messageVisible) {

            if (System.currentTimeMillis()
                    >= messageUntil) {

                messageVisible = false;

            } else {

                renderMessage(graphics);
            }
        }
    }

    private static void renderPoll(
            GuiGraphics graphics
    ) {
        updateVoteLock();

        int screenWidth =
                graphics.guiWidth();

        int x =
                screenWidth
                        - WIDTH
                        - MARGIN;

        int y =
                MARGIN;

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                0xD9101010
        );

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + 1,
                0xFFFFFFFF
        );

        graphics.fill(
                x,
                y + HEIGHT - 1,
                x + WIDTH,
                y + HEIGHT,
                0xFF555555
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + HEIGHT,
                0xFFFFFFFF
        );

        graphics.fill(
                x + WIDTH - 1,
                y,
                x + WIDTH,
                y + HEIGHT,
                0xFF555555
        );

        String displayTitle =
                title;

        if (displayTitle.length() > 27) {
            displayTitle =
                    displayTitle.substring(
                            0,
                            26
                    ) + "...";
        }

        graphics.drawString(
                MINECRAFT.font,
                Component.literal(
                        displayTitle
                ),
                x + 8,
                y + 6,
                0xFFFFFFFF
        );

        int infoSize = 14;

        int infoX =
                x + WIDTH
                        - infoSize
                        - 7;

        int infoY =
                y + 5;

        boolean infoHovered =
                isInside(
                        getMouseX(graphics),
                        getMouseY(graphics),
                        infoX,
                        infoY,
                        infoSize,
                        infoSize
                );

        drawInfoButton(
                graphics,
                infoX,
                infoY,
                infoSize,
                infoHovered
        );

        int yesPercentage =
                getPercentage(yesVotes);

        int yesY =
                y + 24;

        drawVoteButton(
                graphics,
                x + 8,
                yesY,
                Component.translatable(
                        "hud.commandpoll.yes"
                ).getString(),
                0xFF55FF55,
                personalVote != null &&
                        personalVote
        );

        drawBar(
                graphics,
                x + 43,
                yesY + 3,
                BAR_WIDTH,
                BAR_HEIGHT,
                yesPercentage,
                0xFF55FF55
        );

        graphics.drawString(
                MINECRAFT.font,
                Component.literal(
                        yesVotes +
                                "/" +
                                totalPlayers
                ),
                x + 160,
                yesY,
                0xFFFFFFFF
        );

        int noPercentage =
                getPercentage(noVotes);

        int noY =
                y + 39;

        drawVoteButton(
                graphics,
                x + 8,
                noY,
                Component.translatable(
                        "hud.commandpoll.no"
                ).getString(),
                0xFFFF5555,
                personalVote != null &&
                        !personalVote
        );

        drawBar(
                graphics,
                x + 43,
                noY + 3,
                BAR_WIDTH,
                BAR_HEIGHT,
                noPercentage,
                0xFFFF5555
        );

        graphics.drawString(
                MINECRAFT.font,
                Component.literal(
                        noVotes +
                                "/" +
                                totalPlayers
                ),
                x + 160,
                noY,
                0xFFFFFFFF
        );

        if (timerEnabled) {

            int minutes =
                    remainingSeconds / 60;

            int seconds =
                    remainingSeconds % 60;

            String time =
                    String.format(
                            "%02d:%02d",
                            minutes,
                            seconds
                    );

            graphics.drawString(
                    MINECRAFT.font,
                    Component.translatable(
                            "hud.commandpoll.time",
                            time
                    ),
                    x + 8,
                    y + 53,
                    0xFFFFFF55
            );

        } else {

            graphics.drawString(
                    MINECRAFT.font,
                    Component.translatable(
                            "hud.commandpoll.majority"
                    ),
                    x + 8,
                    y + 53,
                    0xFFAAAAAA
            );
        }

        if (infoHovered) {

            graphics.renderTooltip(
                    MINECRAFT.font,
                    Component.translatable(
                            "hud.commandpoll.command",
                            command
                    ),
                    getMouseX(graphics),
                    getMouseY(graphics)
            );
        }
    }

    private static void renderMessage(
            GuiGraphics graphics
    ) {
        Component message =
                Component.translatable(
                        messageKey
                );

        String fullText =
                message.getString();

        boolean shouldHighlight =
                messageHighlightVoteWord &&
                        !messageKey.equals(
                                "message.commandpoll.vote_locked"
                        );

        String keyword = "";

        if (shouldHighlight) {
            keyword =
                    Component.translatable(
                            messageYes
                                    ? "hud.commandpoll.yes"
                                    : "hud.commandpoll.no"
                    ).getString();
        }

        int keywordIndex =
                shouldHighlight
                        ? findKeyword(
                        fullText,
                        keyword
                )
                        : -1;

        Component beforeComponent;
        Component keywordComponent;
        Component afterComponent;

        if (keywordIndex == -1) {

            beforeComponent =
                    Component.literal(
                            fullText
                    );

            keywordComponent =
                    Component.empty();

            afterComponent =
                    Component.empty();

        } else {

            String before =
                    fullText.substring(
                            0,
                            keywordIndex
                    );

            String highlightedText =
                    fullText.substring(
                            keywordIndex,
                            keywordIndex +
                                    keyword.length()
                    );

            String after =
                    fullText.substring(
                            keywordIndex +
                                    keyword.length()
                    );

            beforeComponent =
                    Component.literal(
                            before
                    );

            Style keywordStyle =
                    Style.EMPTY
                            .withBold(true)
                            .withColor(
                                    messageYes
                                            ? ChatFormatting.GREEN
                                            : ChatFormatting.RED
                            );

            keywordComponent =
                    Component.literal(
                            highlightedText
                    ).setStyle(
                            keywordStyle
                    );

            afterComponent =
                    Component.literal(
                            after
                    );
        }

        int beforeWidth =
                MINECRAFT.font.width(
                        beforeComponent
                );

        int keywordWidth =
                MINECRAFT.font.width(
                        keywordComponent
                );

        int afterWidth =
                MINECRAFT.font.width(
                        afterComponent
                );

        int totalWidth =
                beforeWidth +
                        keywordWidth +
                        afterWidth;

        int boxWidth =
                totalWidth + 16;

        int boxHeight = 18;

        int screenWidth =
                graphics.guiWidth();

        int screenHeight =
                graphics.guiHeight();

        int maxWidth =
                screenWidth - 20;

        if (boxWidth > maxWidth) {
            boxWidth = maxWidth;
        }

        int x =
                (screenWidth - boxWidth) / 2;

        int y =
                screenHeight - 61;

        drawRoundedBox(
                graphics,
                x,
                y,
                boxWidth,
                boxHeight
        );

        if (keywordIndex == -1 ||
                boxWidth < totalWidth + 16) {

            drawCenteredMessage(
                    graphics,
                    fullText,
                    x,
                    y,
                    boxWidth
            );

            return;
        }

        int startX =
                x +
                        (boxWidth - totalWidth)
                                / 2;

        int textY =
                y + 5;

        graphics.drawString(
                MINECRAFT.font,
                beforeComponent,
                startX,
                textY,
                0xFFFFFFFF
        );

        graphics.drawString(
                MINECRAFT.font,
                keywordComponent,
                startX +
                        beforeWidth,
                textY,
                messageYes
                        ? 0xFF55FF55
                        : 0xFFFF5555
        );

        graphics.drawString(
                MINECRAFT.font,
                afterComponent,
                startX +
                        beforeWidth +
                        keywordWidth,
                textY,
                0xFFFFFFFF
        );
    }

    private static void drawRoundedBox(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        /*
         * Trasparenza reale:
         * 0.45 = 45% di nero.
         *
         * Il colore del fill rimane nero puro;
         * l'alpha viene applicato tramite shader.
         */
        RenderSystem.setShaderColor(
                0.0F,
                0.0F,
                0.0F,
                0.45F
        );

        graphics.fill(
                x + 3,
                y,
                x + width - 3,
                y + height,
                0xFF000000
        );

        graphics.fill(
                x,
                y + 3,
                x + width,
                y + height - 3,
                0xFF000000
        );

        graphics.fill(
                x + 2,
                y + 1,
                x + width - 2,
                y + height - 1,
                0xFF000000
        );

        graphics.fill(
                x + 1,
                y + 2,
                x + width - 1,
                y + height - 2,
                0xFF000000
        );

        /*
         * Ripristina il colore dello shader,
         * così il resto dell'HUD non diventa trasparente.
         */
        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        RenderSystem.disableBlend();
    }

    private static void drawCenteredMessage(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int width
    ) {
        int textWidth =
                MINECRAFT.font.width(text);

        graphics.drawString(
                MINECRAFT.font,
                Component.literal(text),
                x +
                        (width - textWidth) / 2,
                y + 5,
                0xFFFFFFFF
        );
    }

    private static int findKeyword(
            String text,
            String keyword
    ) {
        if (text == null ||
                keyword == null ||
                keyword.isEmpty()) {
            return -1;
        }

        String lowerText =
                text.toLowerCase(
                        Locale.ROOT
                );

        String lowerKeyword =
                keyword.toLowerCase(
                        Locale.ROOT
                );

        int searchFrom = 0;

        while (searchFrom <
                lowerText.length()) {

            int index =
                    lowerText.indexOf(
                            lowerKeyword,
                            searchFrom
                    );

            if (index == -1) {
                return -1;
            }

            if (isWholeWordMatch(
                    lowerText,
                    index,
                    lowerKeyword.length()
            )) {
                return index;
            }

            searchFrom =
                    index +
                            lowerKeyword.length();
        }

        return -1;
    }

    private static boolean isWholeWordMatch(
            String text,
            int index,
            int length
    ) {
        String word =
                text.substring(
                        index,
                        index + length
                );

        boolean hasAsciiLetterOrDigit =
                false;

        for (int i = 0;
             i < word.length();
             i++) {

            char c =
                    word.charAt(i);

            if ((c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9')) {

                hasAsciiLetterOrDigit = true;
                break;
            }
        }

        if (!hasAsciiLetterOrDigit) {
            return true;
        }

        boolean validBefore =
                index == 0 ||
                        !isAsciiLetterOrDigit(
                                text.charAt(
                                        index - 1
                                )
                        );

        int end =
                index + length;

        boolean validAfter =
                end >= text.length() ||
                        !isAsciiLetterOrDigit(
                                text.charAt(end)
                        );

        return validBefore &&
                validAfter;
    }

    private static boolean isAsciiLetterOrDigit(
            char c
    ) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9');
    }

    private static void updateVoteLock() {
        if (personalVote == null ||
                personalVoteLocked) {
            return;
        }

        if (timerEnabled &&
                remainingSeconds <= 1) {

            personalVoteLocked = true;
            return;
        }

        if (System.currentTimeMillis()
                - personalVoteTime >=
                10_000L) {

            personalVoteLocked = true;
        }
    }

    private static void drawInfoButton(
            GuiGraphics graphics,
            int x,
            int y,
            int size,
            boolean hovered
    ) {
        int background =
                hovered
                        ? 0xFFD0D0D0
                        : 0xFF202020;

        int border =
                0xFFB0B0B0;

        int textColor =
                hovered
                        ? 0xFF222222
                        : 0xFFD0D0D0;

        graphics.fill(
                x,
                y,
                x + size,
                y + size,
                background
        );

        graphics.fill(
                x,
                y,
                x + size,
                y + 1,
                border
        );

        graphics.fill(
                x,
                y + size - 1,
                x + size,
                y + size,
                border
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + size,
                border
        );

        graphics.fill(
                x + size - 1,
                y,
                x + size,
                y + size,
                border
        );

        Component info =
                Component.translatable(
                        "hud.commandpoll.info"
                );

        int textWidth =
                MINECRAFT.font.width(info);

        graphics.drawString(
                MINECRAFT.font,
                info,
                x +
                        (size - textWidth) / 2,
                y + 3,
                textColor
        );
    }

    private static void drawVoteButton(
            GuiGraphics graphics,
            int x,
            int y,
            String text,
            int color,
            boolean selected
    ) {
        int background =
                selected
                        ? color
                        : 0xFF222222;

        int border =
                selected
                        ? color
                        : 0xFFB0B0B0;

        graphics.fill(
                x,
                y,
                x + VOTE_WIDTH,
                y + VOTE_HEIGHT,
                background
        );

        graphics.fill(
                x,
                y,
                x + VOTE_WIDTH,
                y + 1,
                border
        );

        graphics.fill(
                x,
                y + VOTE_HEIGHT - 1,
                x + VOTE_WIDTH,
                y + VOTE_HEIGHT,
                border
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + VOTE_HEIGHT,
                border
        );

        graphics.fill(
                x + VOTE_WIDTH - 1,
                y,
                x + VOTE_WIDTH,
                y + VOTE_HEIGHT,
                border
        );

        int textWidth =
                MINECRAFT.font.width(text);

        graphics.drawString(
                MINECRAFT.font,
                Component.literal(text),
                x +
                        (VOTE_WIDTH - textWidth) / 2,
                y + 2,
                0xFFFFFFFF
        );
    }

    private static void drawBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int percentage,
            int color
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0xFF333333
        );

        int filled =
                Math.round(
                        width *
                                (percentage / 100.0f)
                );

        if (filled > 0) {

            graphics.fill(
                    x,
                    y,
                    x + filled,
                    y + height,
                    color
            );
        }
    }

    private static int getPercentage(
            int votes
    ) {
        if (totalPlayers <= 0) {
            return 0;
        }

        return Math.round(
                votes * 100.0f /
                        totalPlayers
        );
    }

    private static boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + height;
    }

    private static int getMouseX(
            GuiGraphics graphics
    ) {
        return (int) (
                MINECRAFT.mouseHandler.xpos()
                        * graphics.guiWidth()
                        / MINECRAFT.getWindow()
                        .getScreenWidth()
        );
    }

    private static int getMouseY(
            GuiGraphics graphics
    ) {
        return (int) (
                MINECRAFT.mouseHandler.ypos()
                        * graphics.guiHeight()
                        / MINECRAFT.getWindow()
                        .getScreenHeight()
        );
    }

    public static void handleClick(
            double mouseX,
            double mouseY
    ) {
        if (!visible) {
            return;
        }

        updateVoteLock();

        int screenWidth =
                MINECRAFT.getWindow()
                        .getGuiScaledWidth();

        int x =
                screenWidth
                        - WIDTH
                        - MARGIN;

        int y =
                MARGIN;

        boolean clickedYes =
                isInside(
                        mouseX,
                        mouseY,
                        x + 8,
                        y + 24,
                        VOTE_WIDTH,
                        VOTE_HEIGHT
                );

        boolean clickedNo =
                isInside(
                        mouseX,
                        mouseY,
                        x + 8,
                        y + 39,
                        VOTE_WIDTH,
                        VOTE_HEIGHT
                );

        if (!clickedYes &&
                !clickedNo) {
            return;
        }

        boolean newVote =
                clickedYes;

        if (personalVote == null) {

            personalVoteTime =
                    System.currentTimeMillis();

            personalVote =
                    newVote;

        } else if (personalVoteLocked) {

            ModNetwork.CHANNEL.sendToServer(
                    new VotePacket(
                            newVote
                    )
            );

            return;

        } else if (personalVote == newVote) {

            ModNetwork.CHANNEL.sendToServer(
                    new VotePacket(
                            newVote
                    )
            );

            return;

        } else {

            personalVote =
                    newVote;
        }

        ModNetwork.CHANNEL.sendToServer(
                new VotePacket(
                        newVote
                )
        );
    }
}