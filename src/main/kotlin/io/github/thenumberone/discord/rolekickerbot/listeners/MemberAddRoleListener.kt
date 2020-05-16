package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.guild.MemberUpdateEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import org.springframework.stereotype.Component

@Component
class MemberAddRoleListener(private val roleKickService: RoleKickService) : DiscordEventListener<MemberUpdateEvent> {
    override suspend fun on(event: MemberUpdateEvent) {
        roleKickService.updateMember(event.guildId, event.memberId, event.currentRoles)
    }
}