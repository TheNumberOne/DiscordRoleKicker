package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import org.springframework.stereotype.Repository

@Repository
class RoleKickRuleRepositoryImpl : RoleKickRuleRepository {
    private val rules = mutableListOf<RoleKickRule>()

    override fun addRule(rule: RoleKickRule) {
        rules.add(rule)
    }

    override fun updateRule(rule: RoleKickRule) {
        removeRule(rule.guildId, rule.roleId)
        addRule(rule)
    }

    override fun removeRule(server: Snowflake, role: Snowflake) {
        rules.removeIf { it.guildId == server && it.roleId == role }
    }

    override fun removeServer(server: Snowflake) {
        rules.removeIf { it.guildId == server }
    }

    override fun addOrUpdateRole(rule: RoleKickRule): RoleKickService.AddedOrUpdated {
        return if (rules.removeIf { it.guildId == rule.guildId && it.roleId == rule.roleId }) {
            addRule(rule)
            RoleKickService.AddedOrUpdated.Updated
        } else {
            addRule(rule)
            RoleKickService.AddedOrUpdated.Added
        }
    }

    override fun getRules(guild: Snowflake): List<RoleKickRule> {
        return rules.filter { it.guildId == guild }
    }
}