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
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleFinderService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService.AddedOrUpdated.Added
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService.AddedOrUpdated.Updated
import io.github.thenumberone.discord.rolekickerbot.util.displayDurationHelp
import io.github.thenumberone.discord.rolekickerbot.util.parseArguments
import io.github.thenumberone.discord.rolekickerbot.util.parseDuration
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.Priority

private const val embedTitle = "Add/Edit Role"
private val logger = LoggerFactory.getLogger(AddOrEditRoleCommand::class.java)

@Component
@Priority(0)
class AddOrEditRoleCommand(
    val embedHelper: EmbedHelper,
    val roleKickService: RoleKickService,
    val roleFinderService: RoleFinderService
) : MultipleNamesCommand,
    AdminCommand {
    override val names: Set<String> = setOf("addrole", "editrole")

    override suspend fun execIfPrivileged(event: MessageCreateEvent, commandText: String) {
        val guild = event.guild.awaitFirstOrNull() ?: return
        val channel = event.message.channel.awaitFirstOrNull() ?: return

        val parts = parseArguments(commandText)
        if (parts.size < 3) {
            embedHelper.respondTo(event, embedTitle) {
                setDescription("Too few parameters")
                addField("Syntax", "`(addrole|editrole) <role> <warning time> <kick time> [<warning message>]`", false)
            }
            return
        }
        val (roleName, warningTimeString, kickTimeString) = parts

        val role = roleFinderService.findAndValidateRole(guild, roleName, channel, embedTitle) ?: return
        val warningTime = parseAndValidateTime(warningTimeString, "warning time", event) ?: return
        val kickTime = parseAndValidateTime(kickTimeString, "kick time", event) ?: return
        val warningMessage = (if (parts.size >= 4) parts[3].trim() else "").ifEmpty {
            "Warned for having role ${role.mention} in ${guild.name} too long."
        }

        val rule = RoleKickRule(guild.id, role.id, warningTime, kickTime, warningMessage)
        val addOrUpdated = roleKickService.addOrUpdateRule(rule)

        logger.info("Added or updated role")

        embedHelper.respondTo(event, embedTitle) {
            when (addOrUpdated) {
                Added -> setDescription("Added role kicker rule")
                Updated -> setDescription("Updated role kicker rule")
            }
            addRule(rule)
        }
    }

    private suspend fun parseAndValidateTime(
        timeString: String,
        varName: String,
        message: MessageCreateEvent
    ): Duration? {
        val duration = parseDuration(timeString)
        if (duration == null) {
            embedHelper.respondTo(message, embedTitle) {
                setDescription("Invalid ${varName.decapitalize()} specification")
                displayDurationHelp()
            }
            return null
        }
        if (duration <= Duration.ZERO) {
            embedHelper.respondTo(message, embedTitle) {
                setDescription("${varName.capitalize()} must be positive.")
            }
            return null
        }
        return duration
    }
}