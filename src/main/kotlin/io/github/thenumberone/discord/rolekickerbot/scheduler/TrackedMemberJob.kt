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

package io.github.thenumberone.discord.rolekickerbot.scheduler

import discord4j.core.GatewayDiscordClient
import discord4j.rest.http.client.ClientException
import io.github.thenumberone.discord.rolekickerbot.configuration.getCurrentGateway
import io.github.thenumberone.discord.rolekickerbot.configuration.injectGateway
import io.github.thenumberone.discord.rolekickerbot.data.TrackedMemberJob
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMemberRepository
import io.github.thenumberone.discord.rolekickerbot.util.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.util.SelfBotInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirstOrNull
import mu.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.ofType
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

interface TrackedMemberScheduler {
    suspend fun refresh()
}

private val logger = KotlinLogging.logger {}

private sealed class Result {
    class Refresh(val gateway: GatewayDiscordClient) : Result()
    object NoJob : Result()
}

@Component
class TrackedMemberSchedulerImpl(
    private val trackedMembersRepository: TrackedMemberRepository,
    private val embedHelper: EmbedHelper,
    private val selfBotInfo: SelfBotInfo
) : TrackedMemberScheduler {
    private val scheduler = SingleJobScheduler<Result>()

    override suspend fun refresh() {
        val gateway = getCurrentGateway()
        scheduler.launch { refresh(gateway) }
    }

    private suspend fun refresh(gateway: GatewayDiscordClient): Result {
        logger.debug("Scanning for next user")
        val jobData = getJobData()
        jobData ?: return Result.NoJob
        logger.debug {
            "Next user to ${if (jobData.toWarn) "warn" else "kick"} will be user ${jobData.memberId} at ${LocalDateTime.ofInstant(
                jobData.timeTo,
                ZoneId.systemDefault()
            )}"
        }
        warnOrKickEventually(gateway, jobData)
        return Result.Refresh(gateway)
    }

    private suspend fun getJobData(): TrackedMemberJob? {
        return trackedMembersRepository.getNextTrackedMember()
    }

    suspend fun warnOrKickEventually(gateway: GatewayDiscordClient, trackedMember: TrackedMemberJob) {
        val now = Instant.now()
        delay(Duration.between(now, trackedMember.timeTo).toMillis())
        if (trackedMember.toWarn) {
            warn(gateway, trackedMember)
        } else if (trackedMember.toKick) {
            kick(gateway, trackedMember)
        }
    }

    private suspend fun kick(gateway: GatewayDiscordClient, trackedMember: TrackedMemberJob) {
        logger.debug { "Attempting to kick user ${trackedMember.memberId}" }
        val guild = gateway.getGuildById(trackedMember.guildId).awaitFirstOrNull()
            ?: error("Missing guild ${trackedMember.guildId} for ${trackedMember.memberId}")
        val user = guild.getMemberById(trackedMember.memberId).awaitFirstOrNull()
            ?: error("Can't find member ${trackedMember.memberId} of guild ${guild.id}")
        if (selfBotInfo.canBan(user)) {
            guild.kick(trackedMember.memberId).awaitFirstOrNull()
            logger.info { "Kicked user ${trackedMember.memberId}" }
        } else {
            logger.info { "Failed to kick user ${trackedMember.memberId}" }
        }
        trackedMembersRepository.markKicked(trackedMember.trackedMemberId)
    }

    suspend fun warn(gateway: GatewayDiscordClient, trackedMember: TrackedMemberJob) {
        logger.debug { "Attempting to warn user ${trackedMember.memberId}" }
        val member = gateway.getMemberById(trackedMember.guildId, trackedMember.memberId).awaitFirstOrNull()
            ?: error("Can't find member ${trackedMember.memberId} of guild ${trackedMember.guildId}")
        val channel = member.privateChannel.awaitFirstOrNull()
            ?: error("Couldn't find the private channel of ${member.id}.")
        if (member.isBot) {
            logger.info { "Failed to warn bot ${member.id} because they are a bot." }
        } else {
            try {
                injectGateway(gateway) {
                    embedHelper.send(channel, "Warning") {
                        setDescription(trackedMember.warningMessage)
                    }
                }
            } catch (e: ClientException) {
                logger.error(e) { "Failed to warn user ${member.id} with error" }
            }
            logger.info { "Warned user ${trackedMember.memberId}" }
        }
        trackedMembersRepository.markWarned(trackedMember.trackedMemberId)
    }

    fun refresher(): Mono<*> {
        return scheduler.jobResults
            .doOnNext {
                if (it is JobResult.Failed) {
                    logger.error(it.error) { "error while trying to warn/kick users" }
                }
            }
            .ofType<JobResult.Successful<Result>>()
            .map { it.result }
            .ofType<Result.Refresh>()
            .map { it.gateway }
            .doOnNext { gateway -> scheduler.launch { refresh(gateway) } }
            .then()
    }
}