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

package io.github.thenumberone.discord.rolekickerbot.data

import discord4j.rest.util.Snowflake
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

data class ServerPrefix(@Id val guildId: Snowflake, val prefix: String)

@Repository
interface PrefixRepository : R2dbcRepository<ServerPrefix, Snowflake> {
    // Intellij doesn't like this method for some reason
    @Suppress("SpringDataRepositoryMethodParametersInspection")
    suspend fun findByGuildId(guildId: Snowflake): ServerPrefix?

    @Modifying
    @Query("MERGE INTO server_prefix KEY(guild_id) VALUES (:guildId, :prefix);")
    suspend fun save(guildId: Snowflake, prefix: String): Boolean
}

@Component
class PrefixService(private val prefixRepository: PrefixRepository, private val transactional: TransactionalOperator) {
    suspend fun get(guildId: Snowflake): String {
        return transactional.executeAndAwait {
            prefixRepository.findByGuildId(guildId)
        }?.prefix ?: "."
    }

    suspend fun set(guildId: Snowflake, prefix: String) {
        transactional.executeAndAwait {
            prefixRepository.save(guildId, prefix)
        }
    }
}