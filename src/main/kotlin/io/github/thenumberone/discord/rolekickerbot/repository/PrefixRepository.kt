package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import org.springframework.stereotype.Repository

@Repository
class PrefixRepository {
    suspend fun get(server: Snowflake): String {
        return "."
    }
}
