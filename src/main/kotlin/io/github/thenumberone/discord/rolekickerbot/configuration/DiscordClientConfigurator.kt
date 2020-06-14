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

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.shard.GatewayBootstrap
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import io.github.thenumberone.discord.rolekickerbot.subscribers.DiscordGatewaySubscriber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.whenComplete
import reactor.util.context.Context
import kotlin.coroutines.coroutineContext

@Qualifier
annotation class ApplicationMono


@Configuration
@Profile("!test")
class DiscordClientConfigurator {
    @Bean
    fun configureDiscordClient(@Value("\${discord.bot.token}") token: String): DiscordClient {
        val builder = DiscordClientBuilder.create(token)
        return builder.build()
    }

    @Bean
    fun intents(): IntentSet {
        return IntentSet.of(Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES, Intent.GUILD_MESSAGE_REACTIONS)
    }

    @Bean
    fun beginLogin(discordClient: DiscordClient, intents: IntentSet?): Mono<GatewayDiscordClient> {
        var bootstrap = GatewayBootstrap.create(discordClient)
        if (intents != null) bootstrap = bootstrap.setEnabledIntents(intents)
        return bootstrap.login().cache()
    }

    @Bean
    @ApplicationMono
    fun subscribeGatewaySubscribers(
        gatewayMono: Mono<GatewayDiscordClient>,
        subscribers: List<DiscordGatewaySubscriber>
    ): Mono<*> {
        return gatewayMono
            .flatMap { gateway ->
                subscribers.map { subscriber ->
                    subscriber
                        .subscribe(gateway)
                }.whenComplete().injectGateway(gateway)
            }
    }

//    @Bean
//    fun gateway(monoGateway: Mono<GatewayDiscordClient>) = monoGateway.block()
}

object DiscordGatewayClientReactorContextKey

fun getCurrentGatewayMono(): Mono<GatewayDiscordClient> {
    return Mono.subscriberContext().map { context ->
        context.get<GatewayDiscordClient>(DiscordGatewayClientReactorContextKey)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun getCurrentGateway(): GatewayDiscordClient {
    return coroutineContext[ReactorContext]?.context?.get(DiscordGatewayClientReactorContextKey)
        ?: error("Couldn't find the gateway within current context.")
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> injectGateway(gateway: GatewayDiscordClient, f: suspend () -> T): T {
    val oldContext = coroutineContext[ReactorContext]?.context ?: Context.empty()
    val newContext = oldContext.put(DiscordGatewayClientReactorContextKey, gateway)
    return withContext(coroutineContext + ReactorContext(newContext)) { f() }
}

fun <T> Mono<T>.injectGateway(gateway: GatewayDiscordClient): Mono<T> {
    return subscriberContext(Context.of(DiscordGatewayClientReactorContextKey, gateway))
}