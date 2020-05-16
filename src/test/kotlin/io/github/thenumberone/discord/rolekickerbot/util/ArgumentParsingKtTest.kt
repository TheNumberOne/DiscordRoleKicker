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

package io.github.thenumberone.discord.rolekickerbot.util

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

internal class ArgumentParsingKtTest {
    @Test
    fun `Should parse basic args`() {
        parseArguments("one two three") shouldContainExactly listOf("one", "two", "three")
    }

    @Test
    fun `Backslashes should escape spaces when not quoted`() {
        parseArguments("one\\ two three") shouldContainExactly listOf("one two", "three")
    }

    @Test
    fun `Double quotes should quote`() {
        parseArguments("\" some quoted stuff\" not quoted") shouldContainExactly listOf(
            " some quoted stuff",
            "not",
            "quoted"
        )
    }

    @Test
    fun `Single quotes should quote`() {
        parseArguments("' some quoted stuff' not quoted") shouldContainExactly listOf(
            " some quoted stuff",
            "not",
            "quoted"
        )
    }

    @Test
    fun `Backticks should quote`() {
        parseArguments("` some quoted stuff` not quoted") shouldContainExactly listOf(
            " some quoted stuff",
            "not",
            "quoted"
        )
    }
}