package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule

interface RoleKickService {
    suspend fun addRole(rule: RoleKickRule)
    suspend fun updateRole(rule: RoleKickRule)
    enum class AddedOrUpdated { Added, Updated }

    suspend fun addOrUpdateRule(rule: RoleKickRule): AddedOrUpdated

    /**
     * @return true if the role was removed. False if the role was not being tracked.
     */
    suspend fun removeRole(guild: Snowflake, role: Snowflake): Boolean
    suspend fun removeServer(guild: Snowflake)
    suspend fun getRules(guild: Snowflake): List<RoleKickRule>
    suspend fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>,
        memberIdsToRoleIds: Map<Snowflake, Set<Snowflake>>
    )

    suspend fun scanMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>)
    suspend fun removeMember(guildId: Snowflake, memberId: Snowflake)
    suspend fun updateMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>)
}