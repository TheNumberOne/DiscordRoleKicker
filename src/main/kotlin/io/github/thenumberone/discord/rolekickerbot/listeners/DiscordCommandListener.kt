package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.command.DiscordCommand
import io.github.thenumberone.discord.rolekickerbot.repository.PrefixRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DiscordCommandListener(
    val commands: List<DiscordCommand>,
    val prefixRepository: PrefixRepository
) : DiscordEventListener<MessageCreateEvent> {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DiscordCommandListener::class.java)
    }

    override suspend fun on(event: MessageCreateEvent) {
        if (event.member.orElse(null)?.isBot == true) {
            return
        }
        val content = event.message.content
        val server = event.guildId.orElse(null) ?: return
        val prefix = prefixRepository.get(server)
        if (!content.startsWith(prefix)) {
            return
        }
        val commandText = content.substring(prefix.length)
        val parts = commandText.split(' ', limit = 2)
        val commandName = if (parts.isNotEmpty()) parts[0] else ""
        val commandArguments = if (parts.size >= 2) parts[1] else ""

        val command = commands.firstOrNull { it.matches(commandName) } ?: return
        try {
            command.exec(event, commandArguments)
        } catch (e: Exception) {
            logger.error("Error while executing command: $content", e)
        }
    }
}