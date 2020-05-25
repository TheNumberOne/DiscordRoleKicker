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

import io.github.thenumberone.discord.rolekickerbot.data.TrackedMemberJob
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

interface TrackedMemberJobRepository {
    suspend fun getNextTrackedMember(): TrackedMemberJob?
}

@Component
class TrackedMemberJobRepositoryImpl(private val client: DatabaseClient) : TrackedMemberJobRepository {
    override suspend fun getNextTrackedMember(): TrackedMemberJob? {
        return client.execute(
            """
            SELECT * FROM (
                SELECT m.guild_id, m.member_id, m.id AS tracked_member_id, (
                    CASE
                    WHEN NOT m.tried_warn THEN r.time_til_warning + m.started_tracking
                    WHEN NOT m.tried_kick THEN r.time_til_kick + r.time_til_warning + m.started_tracking
                    END
                ) AS time_to, NOT m.tried_warn AS to_warn, NOT m.tried_kick AS to_kick, r.warning_message
                FROM tracked_member AS m
                JOIN role_kick_rule AS r ON m.role_id = r.role_id
                WHERE NOT m.tried_warn OR NOT m.tried_kick
            )
            ORDER BY time_to
            FETCH FIRST 1 ROW ONLY
            """
        )
            .`as`(TrackedMemberJob::class.java)
            .fetch().one().awaitFirstOrNull()
    }
}