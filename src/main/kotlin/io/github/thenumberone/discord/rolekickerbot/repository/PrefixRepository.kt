package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import org.springframework.stereotype.Repository

@Repository
class PrefixRepository {
    private val prefixes = mutableMapOf<Snowflake, String>()

    suspend fun get(server: Snowflake): String {
        return prefixes.getOrDefault(server, ".")
    }

    suspend fun set(server: Snowflake, value: String) {
        prefixes[server] = value
    }
}
