package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface AdminCommand : DiscordCommand {
    suspend fun execIfPrivileged(message: MessageCreateEvent, commandText: String)

    override suspend fun exec(message: MessageCreateEvent, commandText: String) {
        val member = message.member.orElse(null) ?: return
        val permissions = member.basePermissions.awaitFirstOrNull() ?: return
        if (!permissions.contains(Permission.ADMINISTRATOR)) return
        return execIfPrivileged(message, commandText)
    }
}