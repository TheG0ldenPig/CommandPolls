package com.commandpoll;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Poll {

    private static final long VOTE_CHANGE_TIME =
            10_000L;

    private final String title;
    private final String command;
    private final UUID creator;
    private final String creatorName;
    private final boolean timerEnabled;

    private int remainingSeconds;
    private int yesVotes;
    private int noVotes;
    private boolean finished;

    private long lastVoteTime;

    private final Map<UUID, Boolean> playerVotes;
    private final Map<UUID, Long> voteTimes;

    public Poll(
            String title,
            String command,
            UUID creator,
            String creatorName,
            boolean timerEnabled,
            int remainingSeconds
    ) {
        this.title = title;
        this.command = command;
        this.creator = creator;
        this.creatorName = creatorName;
        this.timerEnabled = timerEnabled;
        this.remainingSeconds = remainingSeconds;

        this.yesVotes = 0;
        this.noVotes = 0;
        this.finished = false;

        this.lastVoteTime = 0L;

        this.playerVotes = new HashMap<>();
        this.voteTimes = new HashMap<>();
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getYesVotes() {
        return yesVotes;
    }

    public int getNoVotes() {
        return noVotes;
    }

    public boolean isFinished() {
        return finished;
    }

    public long getLastVoteTime() {
        return lastVoteTime;
    }

    public void setRemainingSeconds(
            int remainingSeconds
    ) {
        this.remainingSeconds =
                Math.max(
                        remainingSeconds,
                        0
                );

        if (this.remainingSeconds <= 0 &&
                timerEnabled) {

            finished = true;
        }
    }

    public void finish() {
        finished = true;
        remainingSeconds = 0;
    }

    public boolean hasVoted(
            UUID player
    ) {
        return playerVotes.containsKey(player);
    }

    public Boolean getPlayerVote(
            UUID player
    ) {
        return playerVotes.get(player);
    }

    public boolean canChangeVote(
            UUID player
    ) {
        if (finished) {
            return false;
        }

        if (timerEnabled &&
                remainingSeconds <= 1) {
            return false;
        }

        Long firstVoteTime =
                voteTimes.get(player);

        if (firstVoteTime == null) {
            return true;
        }

        return System.currentTimeMillis() -
                firstVoteTime <=
                VOTE_CHANGE_TIME;
    }

    public boolean vote(
            UUID player,
            boolean yes
    ) {
        if (finished) {
            return false;
        }

        if (timerEnabled &&
                remainingSeconds <= 0) {
            return false;
        }

        Boolean previousVote =
                playerVotes.get(player);

        if (previousVote == null) {

            playerVotes.put(
                    player,
                    yes
            );

            long now =
                    System.currentTimeMillis();

            voteTimes.put(
                    player,
                    now
            );

            lastVoteTime = now;

            if (yes) {
                yesVotes++;
            } else {
                noVotes++;
            }

            return true;
        }

        if (!canChangeVote(player)) {
            return false;
        }

        if (previousVote == yes) {
            return false;
        }

        if (previousVote) {
            yesVotes--;
        } else {
            noVotes--;
        }

        playerVotes.put(
                player,
                yes
        );

        lastVoteTime =
                System.currentTimeMillis();

        if (yes) {
            yesVotes++;
        } else {
            noVotes++;
        }

        return true;
    }
}