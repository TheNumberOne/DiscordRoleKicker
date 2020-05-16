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

package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.rest.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

private val pingedRoleRegex = Regex("<@&(?<snowflake>\\d+)>")

@Component
class RoleFinderService(private val embedHelper: EmbedHelper) {
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

    /**
     * Finds the single role with [roleName]. If the role can't be found, sends an explanation to the given channel.
     */
    suspend fun findAndValidateRole(
        guild: Guild,
        roleName: String,
        errorChannel: MessageChannel,
        errorTitle: String
    ): Role? {
        val roles = findRoles(guild, roleName)
        if (roles.isEmpty()) {
            embedHelper.send(errorChannel, errorTitle) {
                setDescription("No roles with name $roleName found.")
            }
            return null
        } else if (roles.size > 1) {
            embedHelper.send(errorChannel, errorTitle) {
                setDescription("Multiple roles with $roleName found.")
            }
            return null
        }
        return roles.single()
    }
}