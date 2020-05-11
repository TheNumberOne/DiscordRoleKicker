package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService

interface RoleKickRuleRepository {
    fun addRule(rule: RoleKickRule)
    fun updateRule(rule: RoleKickRule)
    fun removeRule(server: Snowflake, role: Snowflake)
    fun removeServer(server: Snowflake)
    fun addOrUpdateRole(rule: RoleKickRule): RoleKickService.AddedOrUpdated
    fun getRules(guild: Snowflake): List<RoleKickRule>

}
