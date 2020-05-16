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

package io.github.thenumberone.discord.rolekickerbot.configuration

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import io.github.thenumberone.discord.rolekickerbot.listeners.DiscordEventListener
import io.github.thenumberone.discord.rolekickerbot.listeners.getEventType
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DiscordListenerSubscriber(
    client: GatewayDiscordClient,
    listeners: List<DiscordEventListener<*>>
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DiscordListenerSubscriber::class.java)
    }

    init {
        val myTypes = listeners.groupBy { getEventType(it) }
        for ((eventType, eventListeners) in myTypes) {
            client.on(eventType).flatMap { event ->
                mono {
                    for (eventListener in eventListeners) {
                        try {
                            logger.info("Processing event $eventType for $eventListener")
                            @Suppress("UNCHECKED_CAST")
                            (eventListener as DiscordEventListener<Event>).on(event)
                            logger.debug("Processed event $eventType for $eventListener")
                        } catch (e: Exception) {
                            logger.error("Error while processing $eventType for $eventListener.")
                        }
                    }
                }
            }.subscribe()
        }
    }
}