package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.role.RoleDeleteEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import org.springframework.stereotype.Component

@Component
class RoleDeleteListener(private val roleKickService: RoleKickService) : DiscordEventListener<RoleDeleteEvent> {
    override suspend fun on(event: RoleDeleteEvent) {
        roleKickService.removeRole(event.guildId, event.roleId)
    }
}