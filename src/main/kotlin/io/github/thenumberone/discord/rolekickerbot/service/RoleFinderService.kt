package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

private val pingedRoleRegex = Regex("<@&(?<snowflake>\\d+)>")

@Component
class RoleFinderService {
    suspend fun findRoles(guild: Guild, s: String): List<Role> {
        val pingedRole = pingedRoleRegex.matchEntire(s)
        if (pingedRole != null) {
            val id = Snowflake.of(pingedRole.groups["snowflake"]!!.value)
            val role = guild.getRoleById(id).awaitFirstOrNull()
            if (role != null) return listOf(role)
        }

        val withoutAt = s.removePrefix("@")
        return guild.roles.filter { it.name == withoutAt }.collectList().awaitFirstOrNull() ?: emptyList()
    }
}