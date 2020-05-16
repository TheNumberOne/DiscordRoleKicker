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