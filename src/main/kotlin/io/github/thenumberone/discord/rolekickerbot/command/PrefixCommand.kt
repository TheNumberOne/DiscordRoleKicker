package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component

@Component
class PrefixCommand : DiscordCommand {
    override val name: String = "setrolekickerprefix"

    override suspend fun exec(message: MessageCreateEvent, commandText: String) {
        val member = message.member.orElse(null) ?: return
        val permissions = member.basePermissions.awaitSingle()
//        if (permissions.contains(Permission.))
        TODO("Not yet implemented")
    }
}