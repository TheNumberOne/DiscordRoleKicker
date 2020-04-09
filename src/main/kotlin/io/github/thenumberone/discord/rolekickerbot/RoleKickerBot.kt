package io.github.thenumberone.discord.rolekickerbot

import discord4j.core.DiscordClient
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.SimpleCommandLinePropertySource

@Configuration
@ComponentScan("io.github.thenumberone.discord.rolekickerbot")
open class RoleKickerBot {
    companion object {
        @Bean
        fun propertySourcesPlaceholderConfigurer() = PropertySourcesPlaceholderConfigurer()
    }
}

fun main(args: Array<String>) {
    val commandLineProperties = SimpleCommandLinePropertySource(*args)
    val ctx = AnnotationConfigApplicationContext()
    ctx.environment.propertySources.addFirst(commandLineProperties)
    ctx.register(RoleKickerBot::class.java)
    ctx.refresh()

    runBlocking {
        ctx.getBean<DiscordClient>().login().awaitSingle()
    }
}