package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent

interface DiscordCommand {
    val name: String
    suspend fun exec(message: MessageCreateEvent, commandText: String)
}
