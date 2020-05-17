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
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.github.thenumberone.discord.rolekickerbot.util.toAbbreviatedString
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

private const val title = "List Roles"

@Component
class ListRolesCommand(private val roleKickService: RoleKickService, private val embedHelper: EmbedHelper) :
    MultipleNamesCommand, AdminCommand {
    override val names: Set<String> = setOf("listroles", "listrole")

    override suspend fun execIfPrivileged(event: MessageCreateEvent, commandText: String) {
        val guild = event.guild.awaitFirstOrNull() ?: return
        val guildId = guild.id
        val rules = roleKickService.getRules(guildId)

        embedHelper.respondTo(event, title) {
            for (rule in rules) {
                addRule(rule)
            }
        }
        event.member.get().mention
    }
}

fun EmbedCreateSpec.addRule(rule: RoleKickRule) {
    addField("Role", mentionRole(rule.roleId), false)
    addField("Warning Time", rule.timeTilWarning.toAbbreviatedString(), true)
    addField("Kick Time", rule.timeTilKick.toAbbreviatedString(), true)
    addField("Warning Message", rule.warningMessage, true)
}

fun mentionRole(snowflake: Snowflake) = "<@&${snowflake.asString()}>"
fun mentionUser(snowflake: Snowflake) = "<@${snowflake.asString()}>"