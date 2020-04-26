package io.github.thenumberone.discord.rolekickerbot.configuration

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordClientConfigurator {
    @Bean
    fun configureDiscordClient(@Value("\${discord.bot.token}") token: String): DiscordClient {
        val builder = DiscordClientBuilder.create(token)
        return builder.build()
    }

    @Bean
    fun login(discordClient: DiscordClient): GatewayDiscordClient {
        return discordClient.login().block()!!
    }
}