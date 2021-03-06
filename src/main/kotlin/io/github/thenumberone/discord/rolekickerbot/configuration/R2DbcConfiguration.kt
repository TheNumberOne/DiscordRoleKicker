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

package io.github.thenumberone.discord.rolekickerbot.configuration

import discord4j.common.util.Snowflake
import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.h2.api.Interval
import org.h2.util.JSR310Utils
import org.h2.value.ValueInterval
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import java.time.Duration
import java.time.Instant

@Configuration
@EnableConfigurationProperties(R2dbcProperties::class)
@Import(VersioningDatabasePopulator::class)
class R2DbcConfiguration(private val props: R2dbcProperties) : AbstractR2dbcConfiguration() {
    override fun getCustomConverters(): MutableList<Any> {
        return mutableListOf(
            SnowflakeToLongConverter,
            LongToSnowflakeConverter,
            KeepInstantTheSameConverter,
            KeepDurationTheSameConverter,
            IntervalToDurationConverter
        )
    }

    @Bean
    fun connectionFactoryInitializer(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        factory: ConnectionFactory,
        versioningDatabasePopulator: VersioningDatabasePopulator
    ): ConnectionFactoryInitializer {
        return ConnectionFactoryInitializer().apply {
            setConnectionFactory(factory)
            setDatabasePopulator(versioningDatabasePopulator)
        }
    }

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val url = props.url
        val expectedPrefix = "r2dbc:h2:"
        require(url.startsWith(expectedPrefix)) { "Database url must be to a reactive h2 database currently." }

        val config = H2ConnectionConfiguration.builder().url(url.removePrefix(expectedPrefix)).build()
        return H2ConnectionFactory(config)
    }
}

@WritingConverter
object SnowflakeToLongConverter : Converter<Snowflake, Long> {
    override fun convert(source: Snowflake): Long = source.asLong()
}

@ReadingConverter
object LongToSnowflakeConverter : Converter<Long, Snowflake> {
    override fun convert(source: Long): Snowflake = Snowflake.of(source)
}

@WritingConverter
object KeepInstantTheSameConverter : Converter<Instant, Instant> {
    override fun convert(source: Instant): Instant = source
}

@WritingConverter
object KeepDurationTheSameConverter : Converter<Duration, Duration> {
    override fun convert(source: Duration) = source
}

@ReadingConverter
object IntervalToDurationConverter : Converter<Interval, Duration> {
    override fun convert(source: Interval): Duration {
        return JSR310Utils.valueToDuration(
            ValueInterval.from(
                source.qualifier,
                source.isNegative,
                source.leading,
                source.remaining
            )
        ) as Duration
    }

}