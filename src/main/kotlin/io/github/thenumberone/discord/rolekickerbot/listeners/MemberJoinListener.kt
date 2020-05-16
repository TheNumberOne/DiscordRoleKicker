package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.guild.MemberJoinEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import org.springframework.stereotype.Component

@Component
class MemberJoinListener(private val roleKickService: RoleKickService) : DiscordEventListener<MemberJoinEvent> {
    override suspend fun on(event: MemberJoinEvent) {
        val member = event.member
        roleKickService.scanMember(member.guildId, member.id, member.roleIds)
    }
}