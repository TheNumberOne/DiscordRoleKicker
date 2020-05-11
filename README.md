# Discord Role Kicker Bot

[![CircleCI](https://circleci.com/gh/TheNumberOne/DiscordRoleKicker.svg?style=svg)](https://circleci.com/gh/TheNumberOne/DiscordRoleKicker)

(Note that this bot hasn't been implemented yet and is currently in the development process.)

Current version: 0.0.1-SNAPSHOT

## To build/compile

```shell script
./gradlew build
```

## Needed Bot Permissions

3072
 * View Channels
 * Send Messages

## To run

Java must be version 11 or higher.

```shell script
./gradlew bootJar
java -jar build/libs/DiscordRoleKicker-<version>.jar --discord.bot.token=YourReallyLongDiscordBotTokenHere
```

## Basic Functionality
 - User is assigned a role that the bot listens to.
 - **not implemented** After `<X>` time, the user is warned they will be kicked if they still have the same role as before.
 - **not implemented** After `<Y>` more time, the user is kicked.
 
Admin Commands:
 - `.setrolekickerprefix <prefix>` - set the prefix that the bot uses for the server to what you desire.
 - `.(addrole|editrole) <role name> <X> <Y> [<warning message>]` - adds or updates a role for the bot to watch
 - **not implemented** `.listrole(s)` - list the roles that are currently being watched
 - **not implemented** `.removerole <role name>` - removes the role from the list of those that are watched
 - **not implemented** `.setwarningmessage <role name> <warning message>` - sets the warning messages sent to users
 
Additional requirements:
 - The bot will not ban users who can ban or with a role higher than it.

TODO:
 - Implement commands
 - Persist settings
 - Scan servers on startup
    - Clear servers that are no longer attached.
    - Remove users from being watched.
    - Start timers for new users with roles.
 - Handle servers that are being removed.
 