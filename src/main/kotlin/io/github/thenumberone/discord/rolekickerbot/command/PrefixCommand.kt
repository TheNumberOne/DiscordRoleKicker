package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import io.github.thenumberone.discord.rolekickerbot.repository.PrefixRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class PrefixCommand(val prefixRepository: PrefixRepository) : DiscordCommand {
    override val name: String = "setrolekickerprefix"

    override suspend fun exec(message: MessageCreateEvent, commandText: String) {
        val guildId = message.guildId.orElse(null) ?: return
        val member = message.member.orElse(null) ?: return
        val permissions = member.basePermissions.awaitFirstOrNull() ?: return
        if (!permissions.contains(Permission.ADMINISTRATOR)) return
        prefixRepository.set(guildId, commandText)
        message.message.channel.awaitFirstOrNull()?.createEmbed { embed ->
            embed.apply {
                setDescription("Set prefix to $commandText")

            }
        }?.awaitFirstOrNull()
    }
}