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

import discord4j.core.spec.EmbedCreateSpec
import java.time.Duration

private const val durationRegexString = """(?x) # turn on comments
    \s* # optional leading whitespace
    (?<num> # match a number
        (?:[+-]\s*)? # leading sign
        (?:[\d+]) # digits
    )
    \s* # optional whitespace
    (?<mod>(?:[wdhms]|ms|ns)) # modifiers
"""

private val durationRegex = Regex(durationRegexString)
private val matchesAll = Regex("(?:$durationRegexString)+")

fun parseDuration(s: String): Duration? {
    if (!matchesAll.matches(s)) return null
    val parts = durationRegex.findAll(s)
    var duration = Duration.ZERO
    for (part in parts) {
        val num = part.groups["num"]?.value?.toLongOrNull() ?: return null
        val mod = part.groups["mod"]?.value ?: return null
        duration = when (mod) {
            "w" -> duration.plusDays(7 * num)
            "d" -> duration.plusDays(num)
            "h" -> duration.plusHours(num)
            "m" -> duration.plusMinutes(num)
            "s" -> duration.plusSeconds(num)
            "ms" -> duration.plusMillis(num)
            "ns" -> duration.plusNanos(num)
            else -> return null
        }
    }
    return duration
}

fun Duration.toAbbreviatedString() = buildString {
    if (toDaysPart() >= 7) {
        append(toDaysPart() / 7).append('w')
    }
    if (toDaysPart() % 7 != 0L) {
        append(toDaysPart() % 7).append('d')
    }
    if (toHoursPart() > 0) {
        append(toHoursPart()).append('h')
    }
    if (toMinutesPart() > 0) {
        append(toMinutesPart()).append('m')
    }
    if (toSecondsPart() > 0) {
        append(toSecondsPart()).append('s')
    }
    if (toMillisPart() > 0) {
        append(toMillisPart()).append("ms")
    }
    if (toNanosPart() > 0) {
        append(toNanosPart()).append("ns")
    }
    if (isZero) {
        append("0s")
    }
}


fun EmbedCreateSpec.displayDurationHelp() {
    addField("Syntax", "`(<number><modifier>)+`", false)
    addField("Number", "Can be positive or negative. No decimal digits/fractional numbers.", false)
    addField("Modifiers", "w, d, h, m, s, ms, ns", false)
    addField("w", "weeks", true)
    addField("d", "days", true)
    addField("h", "hours", true)
    addField("m", "minutes", true)
    addField("s", "seconds", true)
    addField("ms", "milliseconds", true)
    addField("ns", "nanoseconds", true)
    addField(
        "Example", """
        |`1w-1m` would be 1 minute less than a week.
        |`5h3m` would be 5 hours and one minute.
        |`1w1d` would be 1 week and a day.
    """.trimMargin(), false
    )
}