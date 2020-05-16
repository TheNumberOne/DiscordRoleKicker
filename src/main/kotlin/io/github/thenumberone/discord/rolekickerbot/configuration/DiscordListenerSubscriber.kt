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