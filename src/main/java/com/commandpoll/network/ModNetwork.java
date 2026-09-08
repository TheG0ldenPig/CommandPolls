package com.commandpoll.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(
                            "commandpoll",
                            "main"
                    ),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private static int packetId = 0;

    public static void register() {

        CHANNEL.registerMessage(
                packetId++,
                CreatePollPacket.class,
                CreatePollPacket::encode,
                CreatePollPacket::decode,
                CreatePollPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                PollUpdatePacket.class,
                PollUpdatePacket::encode,
                PollUpdatePacket::decode,
                PollUpdatePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                VotePacket.class,
                VotePacket::encode,
                VotePacket::decode,
                VotePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                PollFeedbackPacket.class,
                PollFeedbackPacket::encode,
                PollFeedbackPacket::decode,
                PollFeedbackPacket::handle
        );
    }
}