package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.core.GatewayDiscordClient
import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.repository.RoleKickRuleRepository
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMembersRepository
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService.AddedOrUpdated
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DefaultRoleKickService(
    private val roleKickRuleRepository: RoleKickRuleRepository,
    private val trackedMembersRepository: TrackedMembersRepository,
    private val discordClient: GatewayDiscordClient
) : RoleKickService {
    override suspend fun addRole(rule: RoleKickRule) {
        roleKickRuleRepository.addRule(rule)
        onRoleAdded(rule)
    }

    override suspend fun updateRole(rule: RoleKickRule) {
        roleKickRuleRepository.updateRule(rule)
    }

    override suspend fun removeRole(guild: Snowflake, role: Snowflake): Boolean {
        return roleKickRuleRepository.removeRule(guild, role)
    }

    override suspend fun removeServer(guild: Snowflake) {
        roleKickRuleRepository.removeServer(guild)
    }

    override suspend fun addOrUpdateRule(rule: RoleKickRule): AddedOrUpdated {
        val ret = roleKickRuleRepository.addOrUpdateRole(rule)
        if (ret == AddedOrUpdated.Added) onRoleAdded(rule)
        return ret
    }

    private suspend fun onRoleAdded(rule: RoleKickRule) {
        val guildMembers = discordClient.getGuildMembers(rule.guildId).collectList().awaitSingle()
        val matchingMembers = guildMembers.filter { rule.roleId in it.roleIds }.map { it.id }.toSet()
        trackedMembersRepository.syncRole(rule.guildId, rule.roleId, matchingMembers, Instant.now())
    }

    override suspend fun getRules(guild: Snowflake): List<RoleKickRule> {
        return roleKickRuleRepository.getRules(guild)
    }

    override suspend fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>,
        memberIdsToRoleIds: Map<Snowflake, Set<Snowflake>>
    ) {
        roleKickRuleRepository.syncGuild(guildId, roleIds)
        val rules = roleKickRuleRepository.getRules(guildId)
        val matchingRules = memberIdsToRoleIds.mapValues { (_, memberRoleIds) ->
            rules.filter { it.roleId in memberRoleIds }
        }
        trackedMembersRepository.syncGuild(guildId, roleIds, matchingRules, Instant.now())
    }

    override suspend fun scanMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>) {
        val rules = roleKickRuleRepository.getRules(guildId)
        val matchingRules = rules.filter { it.roleId in roleIds }
        trackedMembersRepository.syncMember(guildId, memberId, matchingRules, Instant.now())
    }

    override suspend fun removeMember(guildId: Snowflake, memberId: Snowflake) {
        trackedMembersRepository.syncMember(guildId, memberId, emptyList(), Instant.now())
    }

    override suspend fun updateMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>) {
        scanMember(guildId, memberId, roleIds)
    }
}