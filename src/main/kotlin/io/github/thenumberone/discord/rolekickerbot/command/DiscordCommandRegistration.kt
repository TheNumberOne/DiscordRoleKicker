package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class DiscordCommandRegistration(
    client: DiscordClient,
    commands: List<DiscordCommand>,
    prefixContainer: PrefixContainer
) {
    companion object {
        val logger = LoggerFactory.getLogger(DiscordCommandRegistration::class.java)
    }

    init {
        client.eventDispatcher.on(MessageCreateEvent::class.java).flatMap { event ->
            mono {
                if (event.member.orElse(null)?.isBot == true) {
                    return@mono null
                }
                val content = event.message.content.orElse(null) ?: return@mono null
                val server = event.guildId.orElse(null) ?: return@mono null
                val prefix = prefixContainer.get(server)
                if (!content.startsWith(prefix)) {
                    return@mono null
                }
                val commandText = content.substring(prefix.length)
                val parts = commandText.split(' ', limit = 2)
                val commandName = if (parts.isNotEmpty()) parts[0] else ""
                val commandArguments = if (parts.size >= 2) parts[1] else ""

                val command = commands.firstOrNull { it.name == commandName }// ?: return@mono null
                try {
                    command?.exec(event, commandArguments)
                } catch (e: Exception) {
                    logger.error("Error while executing command: $content", e)
                }
            }
        }.subscribe()

    }

}