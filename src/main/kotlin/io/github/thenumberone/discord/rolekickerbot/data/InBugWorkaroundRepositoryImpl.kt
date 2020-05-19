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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Component

@Component
class InBugWorkaroundRepositoryImpl(private val client: DatabaseClient) : InBugWorkaroundRepository {
    override fun findAllByGuildIdAndRoleIdIn(guildId: Snowflake, roleId: Collection<Snowflake>): Flow<RoleKickRule> {
        return client
            .select()
            .from(RoleKickRule::class.java)
            .matching(where("guild_id").`is`(guildId).and("role_id").`in`(roleId))
            .`as`(RoleKickRule::class.java)
            .all()
            .asFlow()
    }

    override suspend fun deleteAllByGuildIdAndRoleIdNotIn(guildId: Snowflake, roleId: Collection<Snowflake>): Int {
        return client
            .delete()
            .from(RoleKickRule::class.java)
            .matching(where("guild_id").`is`(guildId).and("role_id").notIn(roleId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun deleteAllByRoleIdIn(roleId: Collection<Snowflake>): Int {
        return client
            .delete()
            .from(RoleKickRule::class.java)
            .matching(where("role_id").`in`(roleId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}