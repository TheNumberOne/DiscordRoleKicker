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