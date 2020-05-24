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

import discord4j.common.util.Snowflake
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Duration

@Table
data class RoleKickRule(
    @Id
    val id: Long?,
    val guildId: Snowflake,
    val roleId: Snowflake,
    val timeTilWarning: Duration,
    /**
     * This is the time between the warning and the kick
     */
    val timeTilKick: Duration,
    val warningMessage: String
) {
    constructor(
        guildId: Snowflake,
        roleId: Snowflake,
        timeTilWarning: Duration,
        timeTilKick: Duration,
        warningMessage: String
    ) : this(
        null, guildId, roleId, timeTilWarning, timeTilKick, warningMessage
    )
}
