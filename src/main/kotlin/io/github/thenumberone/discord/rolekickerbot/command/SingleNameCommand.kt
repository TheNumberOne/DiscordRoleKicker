package io.github.thenumberone.discord.rolekickerbot.command

interface SingleNameCommand : DiscordCommand {
    val name: String
    override suspend fun matches(name: String) = name == this.name
}