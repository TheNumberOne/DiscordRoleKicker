package io.github.thenumberone.discord.rolekickerbot

import discord4j.core.GatewayDiscordClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener

@SpringBootApplication
class RoleKickerBot(private val client: GatewayDiscordClient) {
    @EventListener(ApplicationStartedEvent::class)
    fun blockTilLogout() {
        client.onDisconnect().block()
    }
}

fun main(args: Array<String>) {
    runApplication<RoleKickerBot>(*args)
}

