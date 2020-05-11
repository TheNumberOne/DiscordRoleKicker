package io.github.thenumberone.discord.rolekickerbot.command

interface MultipleNamesCommand : DiscordCommand {
    val names: Set<String>
    override suspend fun matches(name: String) = name in names
}