package io.github.thenumberone.discord.rolekickerbot.service

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class EmbedHelper(val self: SelfBotInfo) {
    suspend fun withTemplate(builder: EmbedCreateSpec.() -> Unit): EmbedCreateSpec.() -> Unit {
        val name = self.getBotName()
        val imgUrl = self.getImgUrl()
        return {
            setAuthor(name, null, imgUrl)
            setTimestamp(Instant.now())
            builder()
        }
    }

    suspend fun respondTo(message: MessageCreateEvent, builder: EmbedCreateSpec.() -> Unit) {
        message.message.channel.awaitFirstOrNull()?.createEmbed(withTemplate(builder))?.awaitFirstOrNull()
    }
}