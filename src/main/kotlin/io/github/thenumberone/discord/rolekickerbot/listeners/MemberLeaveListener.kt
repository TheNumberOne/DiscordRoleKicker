package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.guild.MemberLeaveEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import org.springframework.stereotype.Component

@Component
class MemberLeaveListener(private val roleKickService: RoleKickService) : DiscordEventListener<MemberLeaveEvent> {
    override suspend fun on(event: MemberLeaveEvent) {
        val member = event.member.orElse(null) ?: return
        roleKickService.removeMember(event.guildId, member.id)
    }
}