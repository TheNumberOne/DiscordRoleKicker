package io.github.thenumberone.discord.rolekickerbot.data

import discord4j.rest.util.Snowflake
import java.time.Duration

data class RoleKickRule(
    val server: Snowflake,
    val role: Snowflake,
    val timeTilWarning: Duration,
    /**
     * This is the time between the warning and the kick
     */
    val timeTilKick: Duration,
    val warningMessage: String
)