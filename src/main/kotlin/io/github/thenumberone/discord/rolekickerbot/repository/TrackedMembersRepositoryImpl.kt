package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.data.TrackedMember
import io.github.thenumberone.discord.rolekickerbot.data.TrackedMemberId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class TrackedMembersRepositoryImpl : TrackedMembersRepository {
    private val members = mutableMapOf<TrackedMemberId, TrackedMember>()

    override fun syncMember(
        guildId: Snowflake,
        memberId: Snowflake,
        matchingRules: Collection<RoleKickRule>,
        now: Instant
    ) {
        val id = TrackedMemberId(guildId, memberId)
        val times = matchingRules.map { it.roleId to now }.toMap()

        synchronized(this) {
            val currentMember = members.getOrElse(id) {
                TrackedMember(id, times)
            }
            val nowTracked = currentMember.trackedRoleIds.filterKeys { it in times }

            val newMember = currentMember.copy(trackedRoleIds = times + nowTracked)
            members[newMember.id] = newMember
        }
    }

    override fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>,
        memberIdsToRoleIds: Map<Snowflake, List<RoleKickRule>>,
        now: Instant
    ) {
        synchronized(this) {
            members.keys.removeIf { it.guildId == guildId && it.memberId !in memberIdsToRoleIds }
            for ((member, roles) in memberIdsToRoleIds) {
                syncMember(guildId, member, roles, now)
            }
        }
    }

    override fun syncRole(guildId: Snowflake, roleId: Snowflake, matchingMembers: Set<Snowflake>, now: Instant) {
        synchronized(this) {
            val affectedMembers = members.values.filter {
                it.id.guildId == guildId && it.id.memberId in matchingMembers
            }
            val newMembers = (matchingMembers - affectedMembers.map { it.id.memberId }).map { memberId ->
                TrackedMember(guildId, memberId, mapOf(roleId to now))
            }

            val (changedMembers, deletedMembers) = affectedMembers.map { member ->
                member.copy(trackedRoleIds = member.trackedRoleIds - roleId)
            }.partition { it.trackedRoleIds.isNotEmpty() }

            members.putAll((newMembers + changedMembers).map { it.id to it })
            deletedMembers.forEach { members.remove(it.id) }
        }
    }
}