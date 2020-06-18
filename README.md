# Discord Role Kicker Bot

[![CircleCI](https://circleci.com/gh/TheNumberOne/DiscordRoleKicker.svg?style=svg)](https://circleci.com/gh/TheNumberOne/DiscordRoleKicker)

Current version: `1.1.3`

[Add to your server!](https://discord.com/api/oauth2/authorize?client_id=697518455204741210&permissions=11330&scope=bot)

## Basic Functionality
 - User is assigned a role that the bot listens to.
 - After `<X>` time, the user is warned they will be kicked if they still have the same role as before.
 - After `<Y>` more time, the user is kicked.
 
Admin Commands:
 - `.setrolekickerprefix <prefix>` - set the prefix that the bot uses for the server to what you desire.
 - `.(addrole|editrole) <role name> <X> <Y> [<warning message>]` - adds or updates a role for the bot to watch
 - `.listrole(s)` - list the roles that are currently being watched
 - `.removerole <role name>` - removes the role from the list of those that are watched
 - `.setwarningmessage <role name> <warning message>` - sets the warning messages sent to users
 - `.listmember(s)` - list the members that are currently being tracked
 
Additional requirements:
 - The bot will not ban users who can ban or with a role higher than it.

## To build/compile

```shell script
./gradlew build
```

## Needed Bot Permissions

11330
 * Kick Members
 * Read Messages
 * Send Messages
 * Manage Messages (For paging on the .listmembers command)
 * Add Reactions

## To run

Java must be version 11 or higher.

```shell script
./gradlew bootJar
java -jar build/libs/DiscordRoleKicker-<version>.jar --discord.bot.token=YourReallyLongDiscordBotTokenHere
```

## To run with docker

This command will start up the bot with some general default settings and so that it will restart as needed.
```shell script
docker run --restart=always --env discord.bot.token=<your-bot-token> --memory=250m --name=<container-name> thenumeralone/role-kicker-bot:<version>    
```

To upgrade the bot running within an existing container, run
```shell script
docker stop <old-container-name>
./upgrade.sh <old-container-name> thenumeralone/role-kicker-bot:<version> <new-container-name>
docker start <new-container-name>
```


 