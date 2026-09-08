package com.commandpoll;

import net.minecraft.server.level.ServerPlayer;

public class PollManager {

    private static Poll activePoll = null;

    public static Poll getActivePoll() {
        return activePoll;
    }

    public static boolean isPollActive() {
        return activePoll != null &&
                !activePoll.isFinished();
    }

    public static boolean createPoll(
            ServerPlayer creator,
            String title,
            String command,
            boolean timerEnabled,
            int remainingSeconds
    ) {
        if (isPollActive()) {
            return false;
        }

        activePoll = new Poll(
                title,
                command,
                creator.getUUID(),
                creator.getGameProfile().getName(),
                timerEnabled,
                remainingSeconds
        );

        return true;
    }

    public static boolean cancelPoll(
            ServerPlayer player
    ) {
        if (activePoll == null) {
            return false;
        }

        if (!activePoll.getCreator()
                .equals(player.getUUID())) {

            return false;
        }

        activePoll.finish();

        return true;
    }

    public static void finishPoll() {
        if (activePoll != null) {
            activePoll.finish();
        }
    }

    public static void removePoll() {
        activePoll = null;
    }
}