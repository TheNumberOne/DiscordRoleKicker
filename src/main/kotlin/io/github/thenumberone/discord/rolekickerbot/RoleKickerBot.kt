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

