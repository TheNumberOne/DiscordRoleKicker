package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import org.springframework.stereotype.Component

@Component
class PingCommand(val embedHelper: EmbedHelper) : SingleNameCommand {
    override val name: String = "ping"

    override suspend fun exec(event: MessageCreateEvent, commandText: String) {
        embedHelper.respondTo(event) {
            setTitle("Pong")
            setDescription(commandText)
        }
    }
}