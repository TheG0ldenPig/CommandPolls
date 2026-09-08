# CommandPoll

CommandPoll is a Minecraft Forge mod for creating interactive polls that can execute server commands when the poll receives a YES result.

## Features

* Create polls directly in Minecraft
* Timer-based polls
* Majority-based polls
* YES/NO voting
* Change your vote within 10 seconds
* Compact poll HUD displayed in the top-right corner
* Live vote counts
* Command preview available from the info button
* Commands can be entered with or without `/`
* In-game feedback displayed above the hotbar
* Automatic command execution when the final result is YES
* Multiplayer support
* Multiple language translations
* Built-in command safety restrictions

## Commands

### `/createpoll`

Opens the poll creation screen.

### `/cancelpoll`

Cancels the active poll.

Only the player who created the poll can cancel the poll.

### `/commandpoll_vote yes`

Votes YES in the active poll.

### `/commandpoll_vote no`

Votes NO in the active poll.

## Poll Types

### Timer Poll

The poll ends automatically when the timer reaches zero.

If the final result is YES, the configured command is executed.

### Majority Poll

The poll has no timer.

Once all online players have voted, the poll waits for a short period before determining the final result.

If the final result is YES, the configured command is executed.

## Voting

Players can change their vote for up to 10 seconds after their first vote.

After that period, the vote is locked and can no longer be changed.

## Supported Languages

* English
* Italian
* German
* Spanish
* French
* Brazilian Portuguese
* Russian
* Simplified Chinese
* Japanese
* Korean

## Requirements

* Minecraft 1.20.1
* Minecraft Forge 47.4.10
* Java 17

## Installation

1. Install Minecraft 1.20.1.
2. Install Minecraft Forge 47.4.10.
3. Download the latest CommandPoll `.jar` file.
4. Place the `.jar` file in your Minecraft `mods` folder.
5. Start Minecraft using the Forge profile.

## Development

CommandPoll uses:

* Minecraft Forge
* ForgeGradle
* Java 17
* Official Mojang mappings

To build the mod:

```bash
./gradlew build
```

## Credits

**CommandPoll** was created and developed by **TheG0ldenPig_**.

**Logo and icon:** CryX_YZ

Special thanks to the Minecraft Forge team and the Minecraft modding community for providing the tools and documentation that make Minecraft mod development possible.

## License

CommandPoll is licensed under the MIT License.

See `LICENSE` for the full license text.
