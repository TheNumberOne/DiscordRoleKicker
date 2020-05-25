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

import discord4j.common.util.Snowflake
import io.github.thenumberone.discord.rolekickerbot.data.JustMemberId
import io.github.thenumberone.discord.rolekickerbot.data.JustRoleId
import io.github.thenumberone.discord.rolekickerbot.data.TrackedMember
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

interface TrackedMemberCustomRepository {
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
class TrackedMemberCustomRepositoryImpl(
    private val client: DatabaseClient
) : TrackedMemberCustomRepository {
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
                Criteria.where("guild_id").`is`(guildId).and("member_id").`is`(memberId).and("role_id")
                    .notIn(matchingRoles)
            )
            .fetch()
            .awaitRowsUpdated()
        val toNotInsert = client
            .select()
            .from(TrackedMember::class.java)
            .matching(
                Criteria.where("guild_id").`is`(guildId).and("member_id").`is`(memberId).and("role_id")
                    .`in`(matchingRoles)
            )
            .`as`(JustRoleId::class.java)
            .all()
            .map { it.roleId }
            .collectList()
            .awaitSingle()

        val toInsert = (matchingRoles - toNotInsert).map { roleId ->
            TrackedMember(
                memberId,
                guildId,
                roleId,
                now
            )
        }
        // getting item out because you can't convert a Mono<List<Item>> into a Flux<Item>

        client
            .insert()
            .into(TrackedMember::class.java)
            .using(toInsert.toFlux())
            .fetch()
            .all()
            .then()
            .awaitFirstOrNull()

        return deleted + toInsert.size
    }

    override suspend fun syncGuild(
        guildId: Snowflake,
        memberIdsToRoleIds: Map<Snowflake, Collection<Snowflake>>,
        now: Instant
    ): Int {
        return client.delete()
            .from(TrackedMember::class.java)
            .matching(
                Criteria.where("guild_id")
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
            .matching(
                Criteria.where("guild_id").`is`(guildId).and("role_id").`is`(roleId).and("member_id").notIn(memberIds)
            )
            .fetch()
            .awaitRowsUpdated()
        val toNotInsert = client.select()
            .from(TrackedMember::class.java)
            .matching(
                Criteria.where("guild_id").`is`(guildId).and("role_id").`is`(roleId).and("member_id").`in`(memberIds)
            )
            .project("member_id")
            .`as`(JustMemberId::class.java)
            .all()
            .map { it.memberId }
            .collectList()
            .awaitSingle()
        val toInsert = (memberIds - toNotInsert).map { memberId ->
            TrackedMember(
                memberId,
                guildId,
                roleId,
                now
            )
        }
        client.insert()
            .into(TrackedMember::class.java)
            .using(toInsert.toFlux())
            .fetch()
            .all()
            .then()
            .awaitFirstOrNull()

//        check(toInsert.size == inserted) {
//            "Inserted the wrong number of tracked members! Members: $toInsert"
//        }

        return deleted + toInsert.size
    }


}