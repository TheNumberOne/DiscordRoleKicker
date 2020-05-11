package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.github.thenumberone.discord.rolekickerbot.util.toAbbreviatedString
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

private const val title = "List Roles"

@Component
class ListRoles(private val roleKickService: RoleKickService, private val embedHelper: EmbedHelper) :
    MultipleNamesCommand, AdminCommand {
    override val names: Set<String> = setOf("listroles", "listrole")

    override suspend fun execIfPrivileged(message: MessageCreateEvent, commandText: String) {
        val guild = message.guild.awaitFirstOrNull() ?: return
        val guildId = guild.id
        val rules = roleKickService.getRules(guildId)
        val roleNames = try {
            rules.map { guild.getRoleById(it.roleId).awaitFirst().name }
        } catch (e: NoSuchElementException) {
            embedHelper.respondTo(message, title) {
                setDescription("Problem finding the names of roles")
            }
            return
        }
        embedHelper.respondTo(message, "List Roles") {
            for ((i, rule) in rules.withIndex()) {
                addField("Role", roleNames[i], false)
                addField("Warning Time", rule.timeTilWarning.toAbbreviatedString(), true)
                addField("Kick Time", rule.timeTilKick.toAbbreviatedString(), true)
                addField("Warning Message", rule.warningMessage, true)
            }
        }
    }
}