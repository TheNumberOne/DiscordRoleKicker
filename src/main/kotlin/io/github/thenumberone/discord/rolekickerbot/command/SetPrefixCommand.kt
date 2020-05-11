package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.repository.PrefixRepository
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import org.springframework.stereotype.Component

@Component
class SetPrefixCommand(
    val prefixRepository: PrefixRepository,
    val embedHelper: EmbedHelper
) : SingleNameCommand, AdminCommand {
    override val name: String = "setrolekickerprefix"

    override suspend fun execIfPrivileged(message: MessageCreateEvent, commandText: String) {
        val guildId = message.guildId.orElse(null) ?: return
        val before = prefixRepository.get(guildId)
        prefixRepository.set(guildId, commandText)
        embedHelper.respondTo(message) {
            setTitle("Set Prefix")
            addField("Before", before, true)
            addField("After", commandText, true)
        }
    }
}