package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.guild.GuildCreateEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class GuildSyncEventListener(private val roleKickService: RoleKickService) : DiscordEventListener<GuildCreateEvent> {
    override suspend fun on(event: GuildCreateEvent) {
        val roles = event.guild.roleIds
        val members: List<Member> = event.guild.members.collectList().awaitFirstOrNull() ?: return
        val membersToRoles = members.map { member ->
            member.id to member.roleIds
        }.toMap()
        roleKickService.syncGuild(event.guild.id, roles, membersToRoles)
    }
}