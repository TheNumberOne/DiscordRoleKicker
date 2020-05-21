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
import io.github.thenumberone.discord.rolekickerbot.configuration.getCurrentGateway
import io.github.thenumberone.discord.rolekickerbot.configuration.injectGateway
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMemberJob
import io.github.thenumberone.discord.rolekickerbot.repository.TrackedMembersRepository
import io.github.thenumberone.discord.rolekickerbot.service.EmbedHelper
import io.github.thenumberone.discord.rolekickerbot.service.SelfBotInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicReference

interface TrackedMemberScheduler {
    suspend fun refresh()
}

private val logger = KotlinLogging.logger {}

@Component
class TrackedMemberSchedulerImpl(
    private val trackedMembersRepository: TrackedMembersRepository,
    private val embedHelper: EmbedHelper,
    private val selfBotInfo: SelfBotInfo
) : TrackedMemberScheduler {
    private var currentJob = AtomicReference<Job>()
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun refresh() {
        refresh(getCurrentGateway())
    }

    suspend fun refresh(gateway: GatewayDiscordClient) {
        logger.info("Scanning for next user")
        val jobData = getJobData()
        jobData ?: return
        logger.info {
            "Next user to ${if (jobData.toWarn) "warn" else "kick"} will be user ${jobData.memberId} at ${LocalDateTime.ofInstant(
                jobData.timeTo,
                ZoneId.systemDefault()
            )}"
        }
        val newJob = scope.launch(start = CoroutineStart.LAZY) {
            try {
                warnOrKickEventually(gateway, jobData)
                refresh(gateway)
            } catch (e: Exception) {
                logger.error("failed to warn or kick user ${jobData.memberId}")
            }
        }
        val oldJob = currentJob.getAndSet(newJob)
        oldJob?.cancel()
        newJob.start()
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
        logger.info { "Attempting to kick user ${trackedMember.memberId}" }
        val guild = gateway.getGuildById(trackedMember.guildId).awaitSingle()
        val user = guild.getMemberById(trackedMember.memberId).awaitSingle()
        if (selfBotInfo.canBan(user)) {
            guild.kick(trackedMember.memberId).awaitSingle()
            logger.info { "Kicked user ${trackedMember.memberId}" }
        } else {
            logger.info { "Can't kick user ${trackedMember.memberId}" }
        }
        trackedMembersRepository.markKicked(trackedMember.trackedMemberId)
    }

    suspend fun warn(gateway: GatewayDiscordClient, trackedMember: TrackedMemberJob) {
        logger.info { "Attempting to warn user ${trackedMember.memberId}" }
        val member = gateway.getMemberById(trackedMember.guildId, trackedMember.memberId).awaitSingle()
        val channel = member.privateChannel.awaitSingle()
        injectGateway(gateway) {
            embedHelper.send(channel, "Warning") {
                setDescription(trackedMember.warningMessage)
            }
        }
        trackedMembersRepository.markWarned(trackedMember.trackedMemberId)
        logger.info { "Warned user ${trackedMember.memberId}" }
    }
}