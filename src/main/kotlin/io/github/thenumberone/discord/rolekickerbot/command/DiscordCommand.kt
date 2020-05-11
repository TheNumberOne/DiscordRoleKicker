package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent

interface DiscordCommand {
    suspend fun matches(name: String): Boolean
    suspend fun exec(message: MessageCreateEvent, commandText: String)
}
