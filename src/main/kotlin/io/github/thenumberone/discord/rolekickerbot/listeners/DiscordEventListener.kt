package io.github.thenumberone.discord.rolekickerbot.listeners

import discord4j.core.event.domain.Event
import java.lang.reflect.ParameterizedType

interface DiscordEventListener<in T> where T : Event {
    suspend fun on(event: T)
}

fun getEventType(eventImpl: DiscordEventListener<*>): Class<out Event> {
    val clazz = eventImpl.javaClass
    val interfaces = clazz.genericInterfaces
    val type = interfaces.filterIsInstance<ParameterizedType>().single {
        it.rawType == DiscordEventListener::class.java
    }
    val (argument) = type.actualTypeArguments

    require(argument is Class<*>) {
        "Type parameter of ${DiscordEventListener<*>::javaClass.name} for ${clazz.name} must be a concrete type."
    }

    @Suppress("UNCHECKED_CAST")
    return argument as Class<out Event>
}