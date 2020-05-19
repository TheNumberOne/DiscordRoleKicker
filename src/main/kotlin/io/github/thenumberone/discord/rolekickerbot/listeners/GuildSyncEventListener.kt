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

package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.guild.GuildCreateEvent
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class GuildSyncEventListener(private val roleKickService: RoleKickService) : DiscordEventListener<GuildCreateEvent> {
    override suspend fun on(event: GuildCreateEvent) {
        val roles = event.guild.roleIds
        val members: List<Member> = event.guild.requestMembers().collectList().awaitFirstOrNull() ?: return
        val membersToRoles = members.map { member ->
            member.id to member.roleIds
        }.toMap()
        roleKickService.syncGuild(event.guild.id, roles, membersToRoles)
    }
}