package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleFinderService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService.AddedOrUpdated.Added
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService.AddedOrUpdated.Updated
import io.github.thenumberone.discord.rolekickerbot.util.displayDurationHelp
import io.github.thenumberone.discord.rolekickerbot.util.parseArguments
import io.github.thenumberone.discord.rolekickerbot.util.parseDuration
import io.github.thenumberone.discord.rolekickerbot.util.toAbbreviatedString
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

    override suspend fun execIfPrivileged(message: MessageCreateEvent, commandText: String) {
        val parts = parseArguments(commandText)
        if (parts.size < 3) {
            embedHelper.respondTo(message, embedTitle) {
                setDescription("Too few parameters")
                addField("Syntax", "`(addrole|editrole) <warning time> <kick time> [<warning message>]`", false)
            }
            return
        }
        val (roleName, warningTimeString, kickTimeString) = parts
        val warningMessage =
            (if (parts.size >= 4) parts[3].trim() else "").ifEmpty { "Warned for having role $roleName too long." }

        val warningTime = parseAndValidateTime(warningTimeString, "warning time", message) ?: return
        val kickTime = parseAndValidateTime(kickTimeString, "kick time", message) ?: return

        val roleId = findAndValidateRoleId(message, roleName) ?: return
        val guildId = message.guildId.orElse(null) ?: return
        val roleRuleSpec = RoleKickRule(guildId, roleId, warningTime, kickTime, warningMessage)
        val addOrUpdated = roleKickService.addOrUpdateRole(roleRuleSpec)

        logger.info("Added or updated role")

        embedHelper.respondTo(message, embedTitle) {
            when (addOrUpdated) {
                Added -> setDescription("Added role kicker rule")
                Updated -> setDescription("Updated role kicker rule")
            }
            addField("role", roleName, true)
            addField("warning time", warningTime.toAbbreviatedString(), true)
            addField("kick time", kickTime.toAbbreviatedString(), true)
            addField("warning message", warningMessage, false)
        }
    }

    private suspend fun findAndValidateRoleId(message: MessageCreateEvent, roleName: String): Snowflake? {
        val guild = message.guild.awaitFirstOrNull() ?: return null
        val roleIds = roleFinderService.findRoles(guild, roleName).map { it.id }
        if (roleIds.isEmpty()) {
            embedHelper.respondTo(message, embedTitle) {
                setDescription("No roles with name $roleName found.")
            }
            return null
        } else if (roleIds.size > 1) {
            embedHelper.respondTo(message, embedTitle) {
                setDescription("Multiple roles with $roleName found.")
            }
            return null
        }
        return roleIds.single()
    }

    private suspend fun parseAndValidateTime(
        timeString: String,
        varName: String,
        message: MessageCreateEvent
    ): Duration? {
        val duration = parseDuration(timeString)
        if (duration == null) {
            embedHelper.respondTo(message, embedTitle) {
                setDescription("Invalid $varName specification")
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