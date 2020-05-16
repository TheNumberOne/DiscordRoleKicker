package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import java.time.Instant

interface TrackedMembersRepository {
    fun syncMember(
        guildId: Snowflake,
        memberId: Snowflake,
        matchingRules: Collection<RoleKickRule>,
        now: Instant
    )

    fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>,
        memberIdsToRoleIds: Map<Snowflake, List<RoleKickRule>>,
        now: Instant
    )

    fun syncRole(guildId: Snowflake, roleId: Snowflake, matchingMembers: Set<Snowflake>, now: Instant)

}