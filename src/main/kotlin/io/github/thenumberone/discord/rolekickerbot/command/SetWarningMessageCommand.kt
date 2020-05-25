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
import io.github.thenumberone.discord.rolekickerbot.service.RoleFinderService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.github.thenumberone.discord.rolekickerbot.util.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.util.parseArguments
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import javax.annotation.Priority

private const val title = "Set Warning"

@Component
@Priority(0)
class SetWarningMessageCommand(
    private val embedHelper: EmbedHelper,
    private val roleFinderService: RoleFinderService,
    private val roleKickService: RoleKickService
) : SingleNameCommand, AdminCommand {
    override val name: String = "setwarningmessage"

    override suspend fun execIfPrivileged(event: MessageCreateEvent, commandText: String) {
        val args = parseArguments(commandText)
        if (args.size != 2 || args[1].isBlank()) {
            embedHelper.respondTo(event, title) {
                setDescription(
                    """
                    |syntax: `.setwarningmessage <role name> <warning message>`
                    |You can include arguments in single quotes ', double quotes ", or backticks `.
                    |Warning message must not be blank.
                    """.trimMargin()
                )
            }
            return
        }
        val guild = event.guild.awaitFirstOrNull() ?: return
        val channel = event.message.channel.awaitFirstOrNull() ?: return
        val role = roleFinderService.findAndValidateRole(guild, args[0], channel, title) ?: return
        val warning = args[1]
        roleKickService.updateWarningMessage(role.id, warning)
        embedHelper.respondTo(event, title) {
            setDescription("Updated warning message.")
            addField("Role", role.mention, true)
            addField("Warning Message", warning, true)
        }
    }
}