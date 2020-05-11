@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
internal class AddOrEditRoleCommandTest {


    @Test
    fun `Should return an error message if no roles found`() = runBlocking {
        val message = mockk<MessageCreateEvent> {
            every { guild } returns Mono.just(mockk {
                every { roles } returns Flux.just(mockk {
                    every { name } returns "bad role"
                })
            })
        }

        val embedSpec = mockk<EmbedCreateSpec>(relaxed = true)

        val embedHelper = mockk<EmbedHelper> {
            coEvery { respondTo(message, any(), invoke(embedSpec)) } returns Unit
        }

        val command = AddOrEditRoleCommand(embedHelper, mockk())

        command.execIfPrivileged(message, "test 1h 1w")

        verify { embedSpec.setDescription("No roles with name `test` found.") }
        coVerify(exactly = 1) { embedHelper.respondTo(message, any(), any()) }
    }
}