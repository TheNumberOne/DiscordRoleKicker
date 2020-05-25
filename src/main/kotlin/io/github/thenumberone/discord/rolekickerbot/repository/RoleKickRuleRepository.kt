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
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.time.Duration

// Intellij's help isn't helpful for some methods.
@Suppress("SpringDataRepositoryMethodParametersInspection")
interface RoleKickRuleRepository : ReactiveCrudRepository<RoleKickRule, Long>,
    RoleKickRuleCustomRepository {
    @Modifying
    @Query(
        """
        UPDATE role_kick_rule
        SET time_til_warning = :timeTilWarning, time_til_kick = :timeTilKick
        WHERE role_id = :roleId
        """
    )
    suspend fun updateRuleTimes(roleId: Snowflake, timeTilWarning: Duration, timeTilKick: Duration): Boolean

    suspend fun deleteByRoleId(roleId: Snowflake): Boolean

    suspend fun deleteAllByGuildId(guildId: Snowflake): Int

    fun findAllByGuildId(guildId: Snowflake): Flow<RoleKickRule>

    suspend fun findByRoleId(roleId: Snowflake): RoleKickRule?

    @Modifying
    @Query(
        """
        UPDATE role_kick_rule
        SET warning_message = :warning_message
        WHERE role_id = :roleId
        """
    )
    suspend fun updateWarningMessage(roleId: Snowflake, warningMessage: String)
}