package com.commandpoll.network;

import com.commandpoll.client.PollHud;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PollMessagePacket {

    private final String messageKey;
    private final boolean yes;
    private final boolean highlightVoteWord;

    public PollMessagePacket(
            String messageKey,
            boolean yes,
            boolean highlightVoteWord
    ) {
        this.messageKey = messageKey;
        this.yes = yes;
        this.highlightVoteWord = highlightVoteWord;
    }

    public void encode(
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(messageKey);
        buffer.writeBoolean(yes);
        buffer.writeBoolean(highlightVoteWord);
    }

    public static PollMessagePacket decode(
            FriendlyByteBuf buffer
    ) {
        return new PollMessagePacket(
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public static void handle(
            PollMessagePacket packet,
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(() -> {

            Minecraft minecraft =
                    Minecraft.getInstance();

            if (minecraft.player == null) {
                return;
            }

            PollHud.showMessage(
                    packet.messageKey,
                    packet.yes,
                    packet.highlightVoteWord
            );
        });

        context.setPacketHandled(true);
    }
}