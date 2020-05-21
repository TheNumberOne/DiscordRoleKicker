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

package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.github.thenumberone.discord.rolekickerbot.util.toAbbreviatedString
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import javax.annotation.Priority

@Component
@Priority(0)
class ListMembersCommand(private val roleKickService: RoleKickService, private val embedHelper: EmbedHelper) :
    MultipleNamesCommand, AdminCommand {
    override val names: Set<String> = setOf("listmembers", "listmember")

    override suspend fun execIfPrivileged(event: MessageCreateEvent, commandText: String) {
        val guildId = event.guildId.orElse(null) ?: return
        val members = roleKickService.getTrackedMembers(guildId)
        val rules = roleKickService.getRules(guildId).map { it.roleId to it }.toMap()

        val roleIdsToMembers = members.groupBy { it.roleId }

        val now = Instant.now()
        embedHelper.respondTo(event, "List Tracked Members") {
            for ((roleId, roleMembers) in roleIdsToMembers) {
                val memberMentions = mutableListOf<String>()
                val timeTilWarnLines = mutableListOf<String>()
                val timeTilKickLines = mutableListOf<String>()

                val rule = rules.getValue(roleId)

                for (member in roleMembers) {
                    val timeStarted = member.startedTracking

                    memberMentions.add(mentionUser(member.memberId))
                    val timeTilWarning = rule.timeTilWarning - Duration.between(timeStarted, now)
                    val timeTilKick = timeTilWarning + rule.timeTilKick
                    timeTilWarnLines.add(
                        if (member.triedWarn) "Warned"
                        else timeTilWarning.toAbbreviatedString()
                    )
                    timeTilKickLines.add(
                        if (member.triedKick) "Failed to Kick"
                        else timeTilKick.toAbbreviatedString()
                    )
                }

                addField("Role", mentionRole(roleId), false)
                addField("User", memberMentions.joinToString("\n"), true)
                addField("Time Til Warning", timeTilWarnLines.joinToString("\n"), true)
                addField("Time Til Kick", timeTilKickLines.joinToString("\n"), true)
            }
        }
    }

}