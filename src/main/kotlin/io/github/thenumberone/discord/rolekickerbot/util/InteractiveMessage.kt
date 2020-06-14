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

package io.github.thenumberone.discord.rolekickerbot.util

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.spec.MessageEditSpec
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.BiFunction


data class MessageInteraction(
    val message: Mono<Message>,
    val reactionAdds: Flux<ReactionAddEvent>,
    val reactionRemoves: Flux<ReactionRemoveEvent>
)


fun interactiveMessage(
    channel: MessageChannel,
    time: Duration,
    interactiveMessage: (MessageCreateSpec) -> Unit
): MessageInteraction {
    val messageMono = channel.createMessage(interactiveMessage).cache()
    val stopMessage = Mono.delay(time).then()
    val reactionAdds = channel.client.on(ReactionAddEvent::class.java)
        .withLatestFrom(
            messageMono,
            BiFunction<ReactionAddEvent, Message, Pair<ReactionAddEvent, Message>> { event, message ->
                Pair(event, message)
            }
        )
        // Only look at events for this message
        .filter { (event, message) -> event.messageId == message.id }
        // Ignore events from this bot
        .filter { (event, message) ->
            val botId = message.author.orElse(null)?.id ?: return@filter false
            event.userId != botId
        }
        .map { (event, _) -> event }
        .takeUntilOther(stopMessage)


    val reactionRemoves = channel.client.on(ReactionRemoveEvent::class.java)
        .withLatestFrom(
            messageMono,
            BiFunction<ReactionRemoveEvent, Message, Pair<ReactionRemoveEvent, Message>> { event, message ->
                Pair(event, message)
            })
        // Only look at events for this message
        .filter { (event, message) -> event.messageId == message.id }
        // Ignore events from this bot
        .filter { (event, message) ->
            val botId = message.author.orElse(null)?.id ?: return@filter false
            event.userId != botId
        }
        .map { (event, _) -> event }
        .takeUntilOther(stopMessage)

    return MessageInteraction(messageMono, reactionAdds, reactionRemoves)
}

private val leftEmoji = ReactionEmoji.unicode("⬅")
private val rightEmoji = ReactionEmoji.unicode("➡")
private val cancelEmoji = ReactionEmoji.unicode("❌")

class StopPagingException : Exception()

fun pagedMessage(
    channel: MessageChannel,
    numPages: Int,
    firstMessage: (MessageCreateSpec) -> Unit,
    pages: (index: Int, MessageEditSpec) -> Unit,
    time: Duration = Duration.ofMinutes(5)
): Mono<Void> {
    val (messageMono, reactionAdds, _) = interactiveMessage(channel, time, firstMessage)
    return messageMono.flatMap { originalMessage ->
        mono {
            var page = 0
            var message = originalMessage
            suspend fun updateMessage() {
                message = message.edit { editSpec -> pages(page, editSpec) }.awaitFirstOrNull()
                val neededEmojis = mutableListOf<ReactionEmoji>()
                neededEmojis.add(leftEmoji)
                neededEmojis.add(rightEmoji)
                neededEmojis.add(cancelEmoji)
                val currentEmojis = message.reactions.map { it.emoji }.toSet()
                if (currentEmojis != neededEmojis.toSet()) {
                    if (currentEmojis.isNotEmpty()) {
                        message.removeAllReactions().awaitFirstOrNull()
                    }
                    for (emoji in neededEmojis) {
                        message.addReaction(emoji).awaitFirstOrNull()
                    }
                }
            }
            updateMessage()

            try {
                reactionAdds.asFlow().collect { event ->
                    val emoji = event.emoji
                    when (emoji) {
                        cancelEmoji -> throw StopPagingException()
                        leftEmoji -> {
                            if (page > 0) {
                                page--
                                updateMessage()
                            }
                        }
                        rightEmoji -> {
                            if (page < numPages - 1) {
                                page++
                                updateMessage()
                            }
                        }
                    }
                    message.removeReaction(emoji, event.userId).awaitFirstOrNull()
                }
            } catch (e: StopPagingException) {
            }
            message.removeAllReactions().awaitFirstOrNull()
        }
    }.then()
}

fun pagedMessage(
    channel: MessageChannel,
    numPages: Int,
    time: Duration = Duration.ofMinutes(5),
    pages: (index: Int, EmbedCreateSpec) -> Unit
): Mono<Void> {
    return pagedMessage(
        channel,
        numPages,
        { message -> message.setEmbed { pages(0, it) } },
        { page, message -> message.setEmbed { pages(page, it) } },
        time
    )
}