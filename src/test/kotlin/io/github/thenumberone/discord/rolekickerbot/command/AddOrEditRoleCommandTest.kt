@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.RoleFinderService
import io.github.thenumberone.discord.rolekickerbot.service.RoleKickService
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

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

        val command = AddOrEditRoleCommand(embedHelper, mockk(), RoleFinderService())

        command.execIfPrivileged(message, "test 1h 1w")

        verify { embedSpec.setDescription("No roles with name test found.") }
        coVerify(exactly = 1) { embedHelper.respondTo(message, any(), any()) }
    }

    @Test
    fun `Should work if @role is done`() = runBlocking {
        val roleId = Snowflake.of(697276661095333958L)
        val guildId = Snowflake.of(123L)

        val message = mockk<MessageCreateEvent> {
            every { guild } returns Mono.just(mockk {
                every { getRoleById(roleId) } returns Mono.just(mockk {
                    every { name } returns "something"
                    every { id } returns roleId
                })
            })
            every { getGuildId() } returns Optional.of(guildId)
        }

        val service = mockk<RoleKickService> {
            coEvery { addOrUpdateRole(any()) } returns RoleKickService.AddedOrUpdated.Added
        }

        val command = AddOrEditRoleCommand(mockk(relaxed = true), service, RoleFinderService())
        command.execIfPrivileged(message, "<@&697276661095333958> 1w 1w")

        coVerify {
            service.addOrUpdateRole(match { it.roleId == roleId })
        }
    }
}