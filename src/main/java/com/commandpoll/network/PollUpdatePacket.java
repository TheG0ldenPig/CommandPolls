package com.commandpoll.network;

import com.commandpoll.client.PollHud;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PollUpdatePacket {

    private final String title;
    private final String command;
    private final String creatorName;
    private final boolean timerEnabled;
    private final int remainingSeconds;
    private final int yesVotes;
    private final int noVotes;
    private final int totalPlayers;
    private final boolean finished;

    public PollUpdatePacket(
            String title,
            String command,
            String creatorName,
            boolean timerEnabled,
            int remainingSeconds,
            int yesVotes,
            int noVotes,
            int totalPlayers,
            boolean finished
    ) {
        this.title = title;
        this.command = command;
        this.creatorName = creatorName;
        this.timerEnabled = timerEnabled;
        this.remainingSeconds = remainingSeconds;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.totalPlayers = totalPlayers;
        this.finished = finished;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(title, 40);
        buffer.writeUtf(command, 100);
        buffer.writeUtf(creatorName, 16);
        buffer.writeBoolean(timerEnabled);
        buffer.writeInt(remainingSeconds);
        buffer.writeInt(yesVotes);
        buffer.writeInt(noVotes);
        buffer.writeInt(totalPlayers);
        buffer.writeBoolean(finished);
    }

    public static PollUpdatePacket decode(
            FriendlyByteBuf buffer
    ) {
        String title = buffer.readUtf(40);
        String command = buffer.readUtf(100);
        String creatorName = buffer.readUtf(16);
        boolean timerEnabled = buffer.readBoolean();
        int remainingSeconds = buffer.readInt();
        int yesVotes = buffer.readInt();
        int noVotes = buffer.readInt();
        int totalPlayers = buffer.readInt();
        boolean finished = buffer.readBoolean();

        return new PollUpdatePacket(
                title,
                command,
                creatorName,
                timerEnabled,
                remainingSeconds,
                yesVotes,
                noVotes,
                totalPlayers,
                finished
        );
    }

    public static void handle(
            PollUpdatePacket packet,
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {

            Minecraft minecraft =
                    Minecraft.getInstance();

            if (minecraft.player == null) {
                return;
            }

            if (packet.finished) {
                PollHud.endPoll();
                return;
            }

            PollHud.showPoll(
                    packet.title,
                    packet.command,
                    packet.creatorName,
                    packet.timerEnabled,
                    packet.remainingSeconds,
                    packet.yesVotes,
                    packet.noVotes,
                    packet.totalPlayers
            );
        });

        context.setPacketHandled(true);
    }
}