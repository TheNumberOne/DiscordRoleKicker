/*
 * MIT License
 *
 * Copyright (c) 2020 Rosetta Roberts
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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

    override fun removeRule(server: Snowflake, role: Snowflake): Boolean {
        return rules.removeIf { it.guildId == server && it.roleId == role }
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

    override fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>
    ) {
        rules.removeIf { it.guildId == guildId && it.roleId in roleIds }
    }

    override fun removeRules(rules: List<RoleKickRule>) {
        this.rules.removeAll(rules)
    }
}