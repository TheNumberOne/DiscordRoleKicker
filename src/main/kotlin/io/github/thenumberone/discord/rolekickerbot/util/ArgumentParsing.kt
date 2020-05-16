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

/**
 * Parses the arguments in [args]. Separates them by spaces. Can also be quoted via ' or " or `.
 * Backslashes escape everything.
 */
fun parseArguments(args: String): List<String> {
    val result = mutableListOf<String>()

    var currentQuote: Char? = null
    val currentArg = StringBuilder()
    var backSlashed = false
    for (c in args) {
        if (backSlashed) {
            currentArg.append(c)
            backSlashed = false
        } else if (c == '\\') {
            backSlashed = true
        } else if (currentQuote == null && currentArg.isEmpty()) {
            if (c in "`'\"") {
                currentQuote = c
            } else if (!c.isWhitespace()) {
                currentArg.append(c)
            }
        } else if ((currentQuote == null && c.isWhitespace()) || c == currentQuote) {
            result.add(currentArg.toString())
            currentArg.clear()
            currentQuote = null
        } else {
            currentArg.append(c)
        }
    }

    if (backSlashed) {
        currentArg.append('\\')
    }
    if (currentArg.isNotEmpty()) {
        result.add(currentArg.toString())
    }

    return result
}