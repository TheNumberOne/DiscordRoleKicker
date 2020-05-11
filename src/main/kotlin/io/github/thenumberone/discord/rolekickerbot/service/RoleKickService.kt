package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule

interface RoleKickService {
    suspend fun addRole(spec: RoleKickRule)
    suspend fun updateRole(spec: RoleKickRule)
    suspend fun removeRole(server: Snowflake, role: Snowflake)
    suspend fun removeServer(server: Snowflake)
    enum class AddedOrUpdated { Added, Updated }

    suspend fun addOrUpdateRole(roleRuleSpec: RoleKickRule): AddedOrUpdated
}