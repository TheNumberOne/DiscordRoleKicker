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
import io.github.thenumberone.discord.rolekickerbot.data.TrackedMember
import io.github.thenumberone.discord.rolekickerbot.data.TrackedMemberId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant

private val logger = LoggerFactory.getLogger(TrackedMembersRepositoryImpl::class.java)

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
            val newTracked = times + nowTracked
            if (newTracked.isEmpty()) {
                members.remove(currentMember.id)
            } else {
                val newMember = currentMember.copy(trackedRoleIds = times + nowTracked)
                members[newMember.id] = newMember
            }
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

    override fun findByGuild(guildId: Snowflake): List<TrackedMember> {
        return synchronized(this) {
            members.filterKeys { it.guildId == guildId }
        }.values.toList()
    }

    override fun removeRole(guildId: Snowflake, roleId: Snowflake) {
        synchronized(this) {
            val affectedMembers = members.values.filter { it.id.guildId == guildId && roleId in it.trackedRoleIds }

            val (changedMembers, deletedMembers) = affectedMembers.map {
                it.copy(trackedRoleIds = it.trackedRoleIds - roleId)
            }.partition {
                it.trackedRoleIds.isNotEmpty()
            }
            members.putAll(changedMembers.map { it.id to it })
            deletedMembers.forEach { members.remove(it.id) }
        }
    }
}