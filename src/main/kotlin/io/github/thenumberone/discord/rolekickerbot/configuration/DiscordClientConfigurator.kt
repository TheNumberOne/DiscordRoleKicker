package io.github.thenumberone.discord.rolekickerbot.configuration

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.shard.GatewayBootstrap
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks

@Configuration
class DiscordClientConfigurator {
    @Bean
    fun configureDiscordClient(@Value("\${discord.bot.token}") token: String): DiscordClient {
        Hooks.onOperatorDebug()
        val builder = DiscordClientBuilder.create(token)
        return builder.build()
    }

    @Bean
    fun intents(): IntentSet {
        return IntentSet.of(Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES)
    }

    @Bean
    fun login(discordClient: DiscordClient, intents: IntentSet): GatewayDiscordClient {
        return GatewayBootstrap.create(discordClient).setEnabledIntents(intents).login().block()!!
    }
}