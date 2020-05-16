@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.`object`.entity.channel.MessageChannel
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
        val channel = mockk<MessageChannel>()
        val message = mockk<MessageCreateEvent> {
            every { guild } returns Mono.just(mockk {
                every { roles } returns Flux.just(mockk {
                    every { name } returns "bad role"
                })
            })
            every { message } returns mockk message@{
                every { this@message.channel } returns Mono.just(channel)
            }
        }

        val embedSpec = mockk<EmbedCreateSpec>(relaxed = true)

        val embedHelper = mockk<EmbedHelper> {
            coEvery { send(channel, any(), invoke(embedSpec)) } returns Unit
        }

        val command = AddOrEditRoleCommand(embedHelper, mockk(), RoleFinderService(embedHelper))

        command.execIfPrivileged(message, "test 1h 1w")

        verify { embedSpec.setDescription("No roles with name test found.") }
        coVerify(exactly = 1) { embedHelper.send(channel, any(), any()) }
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
                    every { mention } returns mention(roleId)
                })
                every { name } returns "Test Guild"
                every { id } returns guildId
            })
            every { getGuildId() } returns Optional.of(guildId)
            every { message } returns mockk {
                every { channel } returns Mono.just(mockk())
            }
        }

        val service = mockk<RoleKickService> {
            coEvery { addOrUpdateRule(any()) } returns RoleKickService.AddedOrUpdated.Added
        }
        val embedHelper = mockk<EmbedHelper>(relaxed = true)
        val command = AddOrEditRoleCommand(embedHelper, service, RoleFinderService(embedHelper))
        command.execIfPrivileged(message, "<@&697276661095333958> 1w 1w")

        coVerify {
            service.addOrUpdateRule(match { it.roleId == roleId })
        }
    }
}