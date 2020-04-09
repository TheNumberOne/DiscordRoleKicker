package io.github.thenumberone.discord.rolekickerbot.configuration

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DiscordClientConfigurator {
    @Bean
    open fun configureDiscordClient(@Value("\${discord.bot.token}") token: String): DiscordClient {
        val builder = DiscordClientBuilder.create(token)
        return builder.build()
    }
}