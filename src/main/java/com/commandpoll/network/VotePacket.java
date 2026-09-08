package com.commandpoll.network;

import com.commandpoll.Poll;
import com.commandpoll.PollManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VotePacket {

    private final boolean yes;

    public VotePacket(
            boolean yes
    ) {
        this.yes = yes;
    }

    public void encode(
            FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(yes);
    }

    public static VotePacket decode(
            FriendlyByteBuf buffer
    ) {
        return new VotePacket(
                buffer.readBoolean()
        );
    }

    public static void handle(
            VotePacket packet,
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

            if (!PollManager.isPollActive()) {

                player.sendSystemMessage(
                        Component.translatable(
                                "message.commandpoll.no_active_poll"
                        )
                );

                return;
            }

            Poll poll =
                    PollManager.getActivePoll();

            if (poll == null ||
                    poll.isFinished()) {

                player.sendSystemMessage(
                        Component.translatable(
                                "message.commandpoll.poll_ended"
                        )
                );

                return;
            }

            Boolean previousVote =
                    poll.getPlayerVote(
                            player.getUUID()
                    );

            if (previousVote != null &&
                    !poll.canChangeVote(
                            player.getUUID()
                    )) {

                ModNetwork.CHANNEL.sendTo(
                        new PollFeedbackPacket(
                                "message.commandpoll.vote_locked",
                                false
                        ),
                        player.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );

                return;
            }

            if (previousVote != null &&
                    previousVote == packet.yes) {

                return;
            }

            boolean success =
                    poll.vote(
                            player.getUUID(),
                            packet.yes
                    );

            if (!success) {
                return;
            }

            String feedbackKey;

            if (previousVote == null) {

                feedbackKey =
                        packet.yes
                                ? "message.commandpoll.voted_yes"
                                : "message.commandpoll.voted_no";

            } else {

                feedbackKey =
                        packet.yes
                                ? "message.commandpoll.changed_yes"
                                : "message.commandpoll.changed_no";
            }

            ModNetwork.CHANNEL.sendTo(
                    new PollFeedbackPacket(
                            feedbackKey,
                            packet.yes
                    ),
                    player.connection.connection,
                    net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
            );

            sendUpdate(player);
        });

        context.setPacketHandled(true);
    }

    private static void sendUpdate(
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

        ModNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
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
                )
        );
    }
}