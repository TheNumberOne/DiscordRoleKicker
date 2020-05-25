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

package io.github.thenumberone.discord.rolekickerbot.subscribers

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import io.github.thenumberone.discord.rolekickerbot.listeners.DiscordEventListener
import io.github.thenumberone.discord.rolekickerbot.listeners.getEventType
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.whenComplete

private val logger = KotlinLogging.logger {}

@Component
class DiscordEventListenerSubscriber(
    val listeners: List<DiscordEventListener<*>>
) : DiscordGatewaySubscriber {

    override fun subscribe(gateway: GatewayDiscordClient): Mono<*> {
        return mono {
            logger.debug { "Registering event listeners" }
            val myTypes = listeners.groupBy { getEventType(it) }
            myTypes.map { (eventType, eventListeners) ->
                gateway.on(eventType).flatMap { event ->
                    mono {
                        for (eventListener in eventListeners) {
                            try {
                                logger.debug("Processing event $eventType for $eventListener")
                                @Suppress("UNCHECKED_CAST")
                                (eventListener as DiscordEventListener<Event>).on(event)
                                logger.debug("Processed event $eventType for $eventListener")
                            } catch (e: Exception) {
                                logger.error(e) { "Error while processing $eventType for $eventListener." }
                            }
                        }
                    }
                }
            }.whenComplete().awaitFirstOrNull()
            logger.debug { "Registered event listeners" }
        }
    }
}