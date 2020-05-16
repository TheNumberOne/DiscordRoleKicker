package io.github.thenumberone.discord.rolekickerbot.data

import discord4j.rest.util.Snowflake
import java.time.Instant

data class TrackedMember(val id: TrackedMemberId, val trackedRoleIds: Map<Snowflake, Instant>) {
    constructor(guildId: Snowflake, memberId: Snowflake, trackedRoleIds: Map<Snowflake, Instant>) : this(
        TrackedMemberId(
            guildId,
            memberId
        ), trackedRoleIds
    )
}

data class TrackedMemberId(val guildId: Snowflake, val memberId: Snowflake)