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

package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.thenumberone.discord.rolekickerbot.command.DiscordCommand
import io.github.thenumberone.discord.rolekickerbot.data.PrefixService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DiscordCommandListener(
    val commands: List<DiscordCommand>,
    val prefixService: PrefixService
) : DiscordEventListener<MessageCreateEvent> {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DiscordCommandListener::class.java)
    }

    override suspend fun on(event: MessageCreateEvent) {
        if (event.member.orElse(null)?.isBot == true) {
            return
        }
        val content = event.message.content
        val server = event.guildId.orElse(null) ?: return
        val prefix = prefixService.get(server)
        if (!content.startsWith(prefix)) {
            return
        }
        val commandText = content.substring(prefix.length)
        val parts = commandText.split(' ', limit = 2)
        val commandName = if (parts.isNotEmpty()) parts[0] else ""
        val commandArguments = if (parts.size >= 2) parts[1] else ""

        val command = commands.firstOrNull { it.matches(commandName) } ?: return
        try {
            logger.info("Received message \"$content\" for command $command")
            command.exec(event, commandArguments)
        } catch (e: Exception) {
            logger.error("Error while executing command: $content", e)
        }
    }
}