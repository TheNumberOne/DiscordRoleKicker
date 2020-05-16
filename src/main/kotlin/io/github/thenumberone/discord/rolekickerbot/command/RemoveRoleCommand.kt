package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleFinderService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

private const val title = "Remove Role"

@Component
class RemoveRoleCommand(
    private val roleFinderService: RoleFinderService,
    private val roleKickService: RoleKickService,
    private val embedHelper: EmbedHelper
) : AdminCommand, SingleNameCommand {
    override suspend fun execIfPrivileged(event: MessageCreateEvent, commandText: String) {
        val guild = event.guild.awaitFirstOrNull() ?: return
        val channel = event.message.channel.awaitFirstOrNull() ?: return
        val role = roleFinderService.findAndValidateRole(guild, commandText, channel, title) ?: return
        if (roleKickService.removeRole(guild.id, role.id)) {
            embedHelper.respondTo(event, title) {
                setDescription("Removed role ${role.mention}.")
            }
        } else {
            embedHelper.respondTo(event, title) {
                setDescription("${role.mention} is not currently being tracked.")
            }
        }
    }

    override val name: String = "removerole"
}