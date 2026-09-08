package com.commandpoll.network;

import com.commandpoll.Poll;
import com.commandpoll.PollManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class CreatePollPacket {

    private final String title;
    private final String command;
    private final boolean timerEnabled;
    private final int remainingSeconds;

    public CreatePollPacket(
            String title,
            String command,
            boolean timerEnabled,
            int remainingSeconds
    ) {
        this.title = title;
        this.command = command;
        this.timerEnabled = timerEnabled;
        this.remainingSeconds = remainingSeconds;
    }

    public void encode(
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(title);
        buffer.writeUtf(command);
        buffer.writeBoolean(timerEnabled);
        buffer.writeInt(remainingSeconds);
    }

    public static CreatePollPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new CreatePollPacket(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readInt()
        );
    }

    public static void handle(
            CreatePollPacket packet,
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(() -> {

            ServerPlayer player =
                    context.getSender();

            if (player == null) {
                return;
            }

            if (PollManager.isPollActive()) {
                return;
            }

            String title =
                    packet.title.trim();

            String command =
                    packet.command.trim();

            if (title.isEmpty() ||
                    command.isEmpty()) {
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

            if (packet.timerEnabled &&
                    packet.remainingSeconds <= 0) {
                return;
            }

            boolean created =
                    PollManager.createPoll(
                            player,
                            title,
                            command,
                            packet.timerEnabled,
                            packet.remainingSeconds
                    );

            if (!created) {
                return;
            }

            sendUpdateToAll(
                    player
            );

            ModNetwork.CHANNEL.sendTo(
                    new PollFeedbackPacket(
                            "message.commandpoll.created",
                            false,
                            false
                    ),
                    player.connection.connection,
                    net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
            );
        });

        context.setPacketHandled(true);
    }

    private static void sendUpdateToAll(
            ServerPlayer player
    ) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                createUpdatePacket(player)
        );
    }

    private static PollUpdatePacket createUpdatePacket(
            ServerPlayer player
    ) {
        Poll poll =
                PollManager.getActivePoll();

        if (poll == null) {
            return new PollUpdatePacket(
                    "",
                    "",
                    "",
                    false,
                    0,
                    0,
                    0,
                    0,
                    true
            );
        }

        int totalPlayers =
                player.server
                        .getPlayerList()
                        .getPlayerCount();

        return new PollUpdatePacket(
                poll.getTitle(),
                poll.getCommand(),
                poll.getCreatorName(),
                poll.isTimerEnabled(),
                poll.getRemainingSeconds(),
                poll.getYesVotes(),
                poll.getNoVotes(),
                totalPlayers,
                poll.isFinished()
        );
    }

    public static void sendUpdate(
            ServerPlayer player
    ) {
        Poll poll =
                PollManager.getActivePoll();

        if (poll == null) {
            return;
        }

        int totalPlayers =
                player.server
                        .getPlayerList()
                        .getPlayerCount();

        ModNetwork.CHANNEL.sendTo(
                new PollUpdatePacket(
                        poll.getTitle(),
                        poll.getCommand(),
                        poll.getCreatorName(),
                        poll.isTimerEnabled(),
                        poll.getRemainingSeconds(),
                        poll.getYesVotes(),
                        poll.getNoVotes(),
                        totalPlayers,
                        poll.isFinished()
                ),
                player.connection.connection,
                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
        );
    }
}