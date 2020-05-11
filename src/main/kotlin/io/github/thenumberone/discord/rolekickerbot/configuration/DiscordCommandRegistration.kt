package io.github.thenumberone.discord.rolekickerbot.configuration

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.command.DiscordCommand
import io.github.thenumberone.discord.rolekickerbot.repository.PrefixRepository
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DiscordCommandRegistration(
    client: GatewayDiscordClient,
    commands: List<DiscordCommand>,
    prefixRepository: PrefixRepository
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DiscordCommandRegistration::class.java)
    }

    init {
        client.eventDispatcher.on(MessageCreateEvent::class.java).flatMap { event ->
            mono {
                if (event.member.orElse(null)?.isBot == true) {
                    return@mono null
                }
                val content = event.message.content
                val server = event.guildId.orElse(null) ?: return@mono null
                val prefix = prefixRepository.get(server)
                if (!content.startsWith(prefix)) {
                    return@mono null
                }
                val commandText = content.substring(prefix.length)
                val parts = commandText.split(' ', limit = 2)
                val commandName = if (parts.isNotEmpty()) parts[0] else ""
                val commandArguments = if (parts.size >= 2) parts[1] else ""

                val command = commands.firstOrNull { it.matches(commandName) } ?: return@mono null
                try {
                    command.exec(event, commandArguments)
                } catch (e: Exception) {
                    logger.error("Error while executing command: $content", e)
                }
            }
        }.subscribe()
    }

}