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
class ListRoles(private val roleKickService: RoleKickService, private val embedHelper: EmbedHelper) :
    MultipleNamesCommand, AdminCommand {
    override val names: Set<String> = setOf("listroles", "listrole")

    override suspend fun execIfPrivileged(event: MessageCreateEvent, commandText: String) {
        val guild = event.guild.awaitFirstOrNull() ?: return
        val guildId = guild.id
        val rules = roleKickService.getRules(guildId)

        embedHelper.respondTo(event, "List Roles") {
            for (rule in rules) {
                addRule(rule)
            }
        }
    }
}

fun EmbedCreateSpec.addRule(rule: RoleKickRule) {
    addField("Role", mention(rule.roleId), false)
    addField("Warning Time", rule.timeTilWarning.toAbbreviatedString(), true)
    addField("Kick Time", rule.timeTilKick.toAbbreviatedString(), true)
    addField("Warning Message", rule.warningMessage, true)
}

fun mention(snowflake: Snowflake) = "<@&${snowflake.asString()}>"