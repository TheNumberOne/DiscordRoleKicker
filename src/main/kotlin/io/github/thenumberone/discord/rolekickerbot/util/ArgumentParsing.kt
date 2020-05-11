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