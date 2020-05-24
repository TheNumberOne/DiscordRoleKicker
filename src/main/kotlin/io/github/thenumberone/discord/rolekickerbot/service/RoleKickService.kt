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
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMember

interface RoleKickService {
    suspend fun addRole(rule: RoleKickRule)
    suspend fun updateRole(rule: RoleKickRule)
    enum class AddedOrUpdated { Added, Updated }

    suspend fun addOrUpdateRule(rule: RoleKickRule): AddedOrUpdated

    /**
     * @return true if the role was removed. False if the role was not being tracked.
     */
    suspend fun removeRole(guild: Snowflake, roleId: Snowflake): Boolean
    suspend fun removeServer(guildId: Snowflake)
    suspend fun getRules(guildId: Snowflake): List<RoleKickRule>
    suspend fun syncGuild(
        guildId: Snowflake,
        roleIds: Set<Snowflake>,
        memberIdsToRoleIds: Map<Snowflake, Set<Snowflake>>
    )

    suspend fun scanMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>)
    suspend fun removeMember(guildId: Snowflake, memberId: Snowflake)
    suspend fun updateMember(guildId: Snowflake, memberId: Snowflake, roleIds: Set<Snowflake>)
    suspend fun getTrackedMembers(guildId: Snowflake): List<TrackedMember>
    suspend fun updateWarningMessage(id: Snowflake, warning: String)
}