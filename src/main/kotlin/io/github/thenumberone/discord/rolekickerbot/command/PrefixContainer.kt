package io.github.thenumberone.discord.rolekickerbot.command

import discord4j.core.`object`.util.Snowflake
import org.springframework.stereotype.Component

@Component
class PrefixContainer {
    suspend fun get(server: Snowflake): String {
        return "."
    }

}
