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
import io.github.thenumberone.discord.rolekickerbot.RoleKickerBotSpringTest
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRule
import io.github.thenumberone.discord.rolekickerbot.data.RoleKickRuleRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Hooks
import java.time.Duration

@RoleKickerBotSpringTest
class RuleRepositoryTest(
    @Autowired
    val ruleRepository: RoleKickRuleRepository
) {
    private val rule = RoleKickRule(
        Snowflake.of("1"),
        Snowflake.of("2"),
        Duration.ZERO,
        Duration.ZERO,
        "Hello world"
    )

    companion object {
        @JvmStatic
        @BeforeAll
        fun enableDebugHooks() {
            Hooks.onOperatorDebug()
        }
    }

    lateinit var withId: RoleKickRule


    @BeforeEach
    fun insertRule() = runBlocking {
        withId = ruleRepository.findByRoleId(rule.roleId) ?: ruleRepository.save(rule).awaitSingle()
    }

    @Test
    fun `Should be able to insert snowflake stuff`() = runBlocking<Unit> {
        val rules = ruleRepository.findAll().collectList().awaitSingle()
        rules shouldHaveSize 1
        rules[0].warningMessage shouldBe "Hello world"
        rules[0].id.shouldNotBeNull()
    }

    @Test
    fun `Should be able to update snowflake`() = runBlocking {
        ruleRepository.updateRuleTimes(rule.roleId, Duration.ofDays(1), Duration.ofHours(3)) shouldBe true
        val result = ruleRepository.findById(withId.id!!).awaitSingle()
        result.warningMessage shouldBe rule.warningMessage
        result.timeTilWarning.toDays() shouldBe 1
        result.timeTilKick.toHours() shouldBe 3
    }

    @Test
    fun `Should be able to remove role`() = runBlocking {
        ruleRepository.deleteByRoleId(withId.roleId) shouldBe true
        ruleRepository.findById(withId.id!!).awaitFirstOrNull().shouldBeNull()
    }
}