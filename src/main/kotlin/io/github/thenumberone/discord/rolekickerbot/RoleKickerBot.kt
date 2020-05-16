package io.github.thenumberone.discord.rolekickerbot

import discord4j.core.GatewayDiscordClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener

private val logger = LoggerFactory.getLogger(RoleKickerBot::class.java)

@SpringBootApplication
class RoleKickerBot(private val client: GatewayDiscordClient) {
    @EventListener(ApplicationStartedEvent::class)
    fun blockTilLogout() {
        val permissions = 3074
        val discordAuthUrl = "https://discord.com/api/oauth2/authorize"
        val scope = "bot"
        client.applicationInfo.subscribe { info ->
            val id = info.id
            logger.info("Add bot to your server using: $discordAuthUrl?client_id=${id.asString()}&permissions=$permissions&scope=$scope")
        }

        client.onDisconnect().block()
    }
}

fun main(args: Array<String>) {
    runApplication<RoleKickerBot>(*args)
}

