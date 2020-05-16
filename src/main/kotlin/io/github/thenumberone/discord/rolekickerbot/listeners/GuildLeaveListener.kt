package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.guild.GuildDeleteEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import org.springframework.stereotype.Component

@Component
class GuildLeaveListener(private val roleKickService: RoleKickService) : DiscordEventListener<GuildDeleteEvent> {
    override suspend fun on(event: GuildDeleteEvent) {
        if (event.isUnavailable) return
        val id = event.guildId
        roleKickService.removeServer(id)
    }
}