package com.commandpoll;

import com.commandpoll.network.ModNetwork;
import com.commandpoll.network.PollFeedbackPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(
        modid = CommandPoll.MODID
)
public class PollCommand {

    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {
        event.getDispatcher().register(
                Commands.literal("cancelpoll")
                        .executes(context ->
                                cancelPoll(
                                        context.getSource()
                                )
                        )
        );
    }

    private static int cancelPoll(
            CommandSourceStack source
    ) {
        ServerPlayer player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        if (!PollManager.isPollActive()) {

            ModNetwork.CHANNEL.sendTo(
                    new PollFeedbackPacket(
                            "message.commandpoll.no_active_poll",
                            false,
                            false
                    ),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );

            return 0;
        }

        Poll poll =
                PollManager.getActivePoll();

        if (poll == null) {
            return 0;
        }

        if (!poll.getCreator()
                .equals(player.getUUID())) {

            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.commandpoll.not_creator"
                    )
            );

            return 0;
        }

        PollManager.cancelPoll(
                player
        );

        for (ServerPlayer onlinePlayer :
                player.server
                        .getPlayerList()
                        .getPlayers()) {

            ModNetwork.CHANNEL.sendTo(
                    new com.commandpoll.network.PollUpdatePacket(
                            poll.getTitle(),
                            poll.getCommand(),
                            poll.getCreatorName(),
                            poll.isTimerEnabled(),
                            0,
                            poll.getYesVotes(),
                            poll.getNoVotes(),
                            player.server
                                    .getPlayerList()
                                    .getPlayerCount(),
                            true
                    ),
                    onlinePlayer.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }

        ModNetwork.CHANNEL.sendTo(
                new PollFeedbackPacket(
                        "message.commandpoll.cancelled",
                        false,
                        false
                ),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );

        PollManager.removePoll();

        return 1;
    }
}