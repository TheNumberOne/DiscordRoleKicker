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
        removeRule(rule.server, rule.role)
        addRule(rule)
    }

    override fun removeRule(server: Snowflake, role: Snowflake) {
        rules.removeIf { it.server == server && it.role == role }
    }

    override fun removeServer(server: Snowflake) {
        rules.removeIf { it.server == server }
    }

    override fun addOrUpdateRole(rule: RoleKickRule): RoleKickService.AddedOrUpdated {
        return if (rules.removeIf { it.server == rule.server && it.role == it.role }) {
            addRule(rule)
            RoleKickService.AddedOrUpdated.Updated
        } else {
            addRule(rule)
            RoleKickService.AddedOrUpdated.Added
        }
    }
}