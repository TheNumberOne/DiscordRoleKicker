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
import org.springframework.stereotype.Component
import javax.annotation.Priority

@Component
@Priority(Integer.MAX_VALUE)
class HelpCommand(private val embedHelper: EmbedHelper) : DiscordCommand {

    override suspend fun matches(name: String): Boolean = true

    private val help = """
        |`.setrolekickerprefix <prefix>` - set the prefix that the bot uses for the server to what you desire.
        |`.(addrole|editrole) <role name> <X> <Y> [<warning message>]` - adds or updates a role for the bot to watch
        |`.listrole(s)` - list the roles that are currently being watched
        |`.removerole <role name>` - removes the role from the list of those that are watched
        |`.setwarningmessage <role name> <warning message>` - sets the warning messages sent to users
        |`.listmember(s)` - list the members that are currently being tracked
    """.trimMargin().lines().map { line -> line.split(" - ") }


    override suspend fun exec(event: MessageCreateEvent, commandText: String) {
        embedHelper.respondTo(event, "Help") {
            for ((title, description) in help) {
                addField(title, description, false)
            }
        }
    }
}