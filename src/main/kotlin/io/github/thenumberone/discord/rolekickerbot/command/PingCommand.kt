package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component

@Component
class PingCommand : DiscordCommand {
    override val name: String = "ping"

    override suspend fun exec(message: MessageCreateEvent, commandText: String) {
        val channel = message.message.channel.awaitSingle()
        channel.createMessage("pong: $commandText").awaitSingle()
    }
}