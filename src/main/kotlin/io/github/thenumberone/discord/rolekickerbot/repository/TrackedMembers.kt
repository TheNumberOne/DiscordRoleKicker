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

package io.github.thenumberone.discord.rolekickerbot.repository

import discord4j.rest.util.Snowflake
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant


data class TrackedMember(
    @Id val id: Long?,
    val memberId: Snowflake,
    val guildId: Snowflake,
    val roleId: Snowflake,
    val startedTracking: Instant
) {
    constructor(memberId: Snowflake, guildId: Snowflake, roleId: Snowflake, startedTracking: Instant) : this(
        null,
        memberId,
        guildId,
        roleId,
        startedTracking
    )
}

interface TrackedMembersCustomParts {
    suspend fun syncMember(
        guildId: Snowflake,
        memberId: Snowflake,
        matchingRoles: Collection<Snowflake>,
        now: Instant
    ): Int

    suspend fun syncGuild(
        guildId: Snowflake,
        memberIdsToRoleIds: Map<Snowflake, Collection<Snowflake>>,
        now: Instant
    ): Int

    suspend fun syncRole(guildId: Snowflake, roleId: Snowflake, memberIds: Collection<Snowflake>, now: Instant): Int
}

@Component
class TrackedMembersCustomPartsImpl(
    private val client: DatabaseClient
) : TrackedMembersCustomParts {
    override suspend fun syncMember(
        guildId: Snowflake,
        memberId: Snowflake,
        matchingRoles: Collection<Snowflake>,
        now: Instant
    ): Int {
        val deleted = client
            .delete()
            .from(TrackedMember::class.java)
            .matching(
                where("guild_id").`is`(guildId).and("member_id").`is`(memberId).and("role_id").notIn(matchingRoles)
            )
            .fetch()
            .awaitRowsUpdated()
        val toInsert = client
            .select()
            .from(TrackedMember::class.java)
            .matching(
                where("guild_id").`is`(guildId).and("member_id").`is`(memberId).and("role_id").`in`(matchingRoles)
            )
            .project("role_id")
            .`as`(Snowflake::class.java)
            .all()
            .collectList()
            .map { toNotInsert ->
                (matchingRoles - toNotInsert).map { roleId ->
                    TrackedMember(memberId, guildId, roleId, now)
                }
            }
            // getting item out because you can't convert a Mono<List<Item>> into a Flux<Item>
            .awaitSingle()

        val inserted = client
            .insert()
            .into(TrackedMember::class.java)
            .using(toInsert.toFlux())
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return deleted + inserted
    }

    override suspend fun syncGuild(
        guildId: Snowflake,
        memberIdsToRoleIds: Map<Snowflake, Collection<Snowflake>>,
        now: Instant
    ): Int {
        return client.delete()
            .from(TrackedMember::class.java)
            .matching(
                where("guild_id")
                    .`is`(guildId)
                    .and("member_id")
                    .notIn(memberIdsToRoleIds.keys)
            )
            .fetch()
            .rowsUpdated()
            .awaitSingle() +
                memberIdsToRoleIds.map { (memberId, roleIds) ->
                    syncMember(guildId, memberId, roleIds, now)
                }.sum()
    }

    override suspend fun syncRole(
        guildId: Snowflake,
        roleId: Snowflake,
        memberIds: Collection<Snowflake>,
        now: Instant
    ): Int {
        val deleted = client.delete()
            .from(TrackedMember::class.java)
            .matching(where("guild_id").`is`(guildId).and("role_id").`is`(roleId).and("member_id").notIn(memberIds))
            .fetch()
            .awaitRowsUpdated()
        val toNotInsert = client.select()
            .from(TrackedMember::class.java)
            .matching(where("guild_id").`is`(guildId).and("role_id").`is`(roleId).and("member_id").`in`(memberIds))
            .project("member_id")
            .`as`(Snowflake::class.java)
            .all()
            .collectList()
            .awaitSingle()
        val toInsert = (memberIds - toNotInsert).map { memberId -> TrackedMember(memberId, guildId, roleId, now) }
        val inserted = client.insert()
            .into(TrackedMember::class.java)
            .using(toInsert.toFlux())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return deleted + inserted
    }

}

@Suppress("SpringDataRepositoryMethodParametersInspection")
interface TrackedMembersRepository2 : ReactiveCrudRepository<TrackedMember, Long>, TrackedMembersCustomParts {
    fun findAllByGuildId(guildId: Snowflake): Flow<TrackedMember>
    suspend fun deleteAllByGuildIdAndRoleId(guildId: Snowflake, roleId: Snowflake): Int
    suspend fun deleteAllByGuildId(guildId: Snowflake): Int
    suspend fun deleteAllByGuildIdAndMemberId(guildId: Snowflake, memberId: Snowflake): Int
}