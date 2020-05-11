package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.User
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component

@Component
class SelfBotInfo(val discordClient: GatewayDiscordClient) {
    suspend fun getBotName(): String {
        return getUser().username
    }

    suspend fun getUser(): User = discordClient.self.awaitSingle()

    suspend fun getImgUrl(): String {
        return getUser().avatarUrl
    }
}