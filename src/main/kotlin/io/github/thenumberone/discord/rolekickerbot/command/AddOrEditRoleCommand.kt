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

private const val embedTitle = "Add/Edit Role"
private val logger = LoggerFactory.getLogger(AddOrEditRoleCommand::class.java)

@Component
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