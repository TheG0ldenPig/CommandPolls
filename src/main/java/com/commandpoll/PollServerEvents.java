package com.commandpoll;

import com.commandpoll.network.CreatePollPacket;
import com.commandpoll.network.ModNetwork;
import com.commandpoll.network.PollFeedbackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(
        modid = CommandPoll.MODID
)
public class PollServerEvents {

    private static int tickCounter = 0;

    private static final long MAJORITY_WAIT_TIME =
            10_000L;

    private static final Set<String> BLOCKED_COMMANDS =
            new HashSet<>(Arrays.asList(
                    "give",
                    "gamerule"
            ));

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {
        if (event.phase !=
                TickEvent.Phase.END) {
            return;
        }

        if (!PollManager.isPollActive()) {
            tickCounter = 0;
            return;
        }

        tickCounter++;

        if (tickCounter < 20) {
            return;
        }

        tickCounter = 0;

        Poll poll =
                PollManager.getActivePoll();

        if (poll == null) {
            return;
        }

        MinecraftServer server =
                event.getServer();

        if (poll.isTimerEnabled()) {

            handleTimerPoll(
                    server,
                    poll
            );

            return;
        }

        handleMajorityPoll(
                server,
                poll
        );
    }

    private static void handleTimerPoll(
            MinecraftServer server,
            Poll poll
    ) {
        int remaining =
                poll.getRemainingSeconds();

        if (remaining <= 1) {

            finishPoll(
                    server,
                    poll
            );

            return;
        }

        poll.setRemainingSeconds(
                remaining - 1
        );

        sendUpdate(server);
    }

    private static void handleMajorityPoll(
            MinecraftServer server,
            Poll poll
    ) {
        int totalPlayers =
                server.getPlayerList()
                        .getPlayerCount();

        int totalVotes =
                poll.getYesVotes()
                        + poll.getNoVotes();

        if (totalPlayers <= 0 ||
                totalVotes < totalPlayers) {
            return;
        }

        if (poll.getLastVoteTime() <= 0L) {
            return;
        }

        long timeSinceLastVote =
                System.currentTimeMillis()
                        - poll.getLastVoteTime();

        if (timeSinceLastVote <
                MAJORITY_WAIT_TIME) {
            return;
        }

        finishPoll(
                server,
                poll
        );
    }

    private static void finishPoll(
            MinecraftServer server,
            Poll poll
    ) {
        boolean tie =
                poll.getYesVotes() ==
                        poll.getNoVotes();

        boolean resultYes;

        if (poll.getYesVotes() >
                poll.getNoVotes()) {

            resultYes = true;

        } else if (poll.getNoVotes() >
                poll.getYesVotes()) {

            resultYes = false;

        } else {

            resultYes =
                    ThreadLocalRandom.current()
                            .nextBoolean();
        }

        String resultKey;

        if (tie) {

            resultKey =
                    resultYes
                            ? "message.commandpoll.result.tie_yes"
                            : "message.commandpoll.result.tie_no";

        } else {

            resultKey =
                    resultYes
                            ? "message.commandpoll.result.majority_yes"
                            : "message.commandpoll.result.majority_no";
        }

        poll.finish();

        ModNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new PollFeedbackPacket(
                        resultKey,
                        resultYes,
                        true
                )
        );

        if (resultYes) {

            executePollCommand(
                    server,
                    poll
            );
        }

        sendFinalUpdate(server);
    }

    private static void executePollCommand(
            MinecraftServer server,
            Poll poll
    ) {
        String command =
                poll.getCommand().trim();

        if (command.isEmpty()) {
            return;
        }

        if (command.startsWith("/")) {

            command =
                    command.substring(1)
                            .trim();
        }

        if (command.isEmpty()) {
            return;
        }

        if (isCommandBlocked(command)) {
            return;
        }

        try {

            server.getCommands()
                    .performPrefixedCommand(
                            server.createCommandSourceStack()
                                    .withSuppressedOutput(),
                            command
                    );

        } catch (Exception ignored) {
        }
    }

    private static boolean isCommandBlocked(
            String command
    ) {
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
        ) || mainCommand.equals("gamemode");
    }

    private static void sendUpdate(
            MinecraftServer server
    ) {
        Poll poll =
                PollManager.getActivePoll();

        if (poll == null) {
            return;
        }

        for (ServerPlayer player :
                server.getPlayerList()
                        .getPlayers()) {

            CreatePollPacket.sendUpdate(
                    player
            );
        }
    }

    private static void sendFinalUpdate(
            MinecraftServer server
    ) {
        Poll poll =
                PollManager.getActivePoll();

        if (poll == null) {
            return;
        }

        for (ServerPlayer player :
                server.getPlayerList()
                        .getPlayers()) {

            CreatePollPacket.sendUpdate(
                    player
            );
        }

        PollManager.removePoll();
    }
}