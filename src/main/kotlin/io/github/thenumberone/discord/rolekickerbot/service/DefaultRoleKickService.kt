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

package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.common.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.configuration.getCurrentGateway
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRuleRepository
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMember
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMembersRepository
import io.github.thenumberone.discord.rolekickerbot.scheduler.TrackedMemberScheduler
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService.AddedOrUpdated
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Instant

private val logger = LoggerFactory.getLogger(DefaultRoleKickService::class.java)

@Service
class DefaultRoleKickService(
    private val roleKickRuleRepository: RoleKickRuleRepository,
    private val transaction: TransactionalOperator,
    private val trackedMembersRepository: TrackedMembersRepository,
    private val scheduler: TrackedMemberScheduler
) : RoleKickService {
    override suspend fun addRole(rule: RoleKickRule) {
        addRuleWithoutScheduler(rule)
        scheduler.refresh()
    }

    private suspend fun addRuleWithoutScheduler(rule: RoleKickRule) {
        transaction.executeAndAwait {
            roleKickRuleRepository.save(rule).awaitSingle()

            val guildMembers = getCurrentGateway().requestMembers(rule.guildId).collectList().awaitSingle()
            val matchingMembers = guildMembers.filter { rule.roleId in it.roleIds }.map { it.id }.toSet()
            trackedMembersRepository.syncRole(rule.guildId, rule.roleId, matchingMembers, Instant.now())
        }
    }

    private suspend fun updateRuleWithoutScheduler(rule: RoleKickRule) {
        transaction.executeAndAwait {
            if (!roleKickRuleRepository.updateRuleTimes(rule.roleId, rule.timeTilWarning, rule.timeTilKick)) {
                require(false) { "Must update exist role!" }
            } else {
                logger.info("Updated rule")
            }
        }
    }

    override suspend fun updateRole(rule: RoleKickRule) {
        updateRuleWithoutScheduler(rule)
        scheduler.refresh()
    }

    override suspend fun removeRole(guild: Snowflake, roleId: Snowflake): Boolean {
        val removed = transaction.executeAndAwait {
            if (roleKickRuleRepository.deleteByRoleId(roleId)) {
                trackedMembersRepository.deleteAllByGuildIdAndRoleId(guild, roleId)
                true
            } else {
                false
            }
        } ?: false
        if (removed) {
            scheduler.refresh()
        }
        return removed
    }

    override suspend fun removeServer(guildId: Snowflake) {
        transaction.executeAndAwait {
            roleKickRuleRepository.deleteAllByGuildId(guildId)
            trackedMembersRepository.deleteAllByGuildId(guildId)
        }
        scheduler.refresh()
    }

    override suspend fun addOrUpdateRule(rule: RoleKickRule): AddedOrUpdated {
        val addedOrUpdated = transaction.executeAndAwait {
            val exists = roleKickRuleRepository.findByRoleId(rule.roleId) != null
            if (exists) {
                updateRuleWithoutScheduler(rule)
                AddedOrUpdated.Updated
            } else {
                addRuleWithoutScheduler(rule)
                AddedOrUpdated.Added
            }
        } ?: error("Problem updating table")
        scheduler.refresh()
        return addedOrUpdated
    }

    override suspend fun getRules(guildId: Snowflake): List<RoleKickRule> {
        return transaction.executeAndAwait {
            roleKickRuleRepository.findAllByGuildId(guildId).toList()
        } ?: error("Problem looking up guild rules")
    }

    override suspend fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>,
        memberIdsToRoleIds: Map<Snowflake, Set<Snowflake>>
    ) {
        transaction.executeAndAwait {
            roleKickRuleRepository.deleteAllByGuildIdAndRoleIdNotIn(guildId, roleIds)

            val rules = roleKickRuleRepository.findAllByGuildId(guildId).toList()
            val matchingRules = memberIdsToRoleIds.mapValues { (_, memberRoleIds) ->
                rules.filter { it.roleId in memberRoleIds }.map { it.roleId }
            }
            trackedMembersRepository.syncGuild(guildId, matchingRules, Instant.now())
        } ?: error("Problem syncing guild")
        scheduler.refresh()
    }

    override suspend fun scanMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>) {
        transaction.executeAndAwait {
            val rules = roleKickRuleRepository
                .findAllByGuildIdAndRoleIdIn(guildId, roleIds)
                .toList()
                .map { it.roleId }

            trackedMembersRepository.syncMember(guildId, memberId, rules, Instant.now())
        }
        scheduler.refresh()
    }

    override suspend fun removeMember(guildId: Snowflake, memberId: Snowflake) {
        transaction.executeAndAwait {
            trackedMembersRepository.deleteAllByGuildIdAndMemberId(guildId, memberId)
        }
        scheduler.refresh()
    }

    override suspend fun updateMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>) {
        transaction.executeAndAwait {
            scanMember(guildId, memberId, roleIds)
        }
        scheduler.refresh()
    }

    override suspend fun updateWarningMessage(id: Snowflake, warning: String) {
        transaction.executeAndAwait {
            roleKickRuleRepository.updateWarningMessage(id, warning)
        }
        scheduler.refresh()
    }

    override suspend fun getTrackedMembers(guildId: Snowflake): List<TrackedMember> {
        return transaction.executeAndAwait {
            trackedMembersRepository.findAllByGuildId(guildId).toList()
        } ?: error("Problem finding tracked members")
    }
}