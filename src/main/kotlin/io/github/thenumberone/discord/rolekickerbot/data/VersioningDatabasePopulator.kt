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

package io.github.thenumberone.discord.rolekickerbot.data

import io.r2dbc.spi.Connection
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.data.r2dbc.connectionfactory.init.DatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@ConstructorBinding
@ConfigurationProperties(prefix = "app.db")
class DatabaseConfiguration(
    val version: String,
    val showTablesCmd: String = "SHOW TABLES",
    val tableNameCol: String = "TABLE_NAME",
    val initTable: String = "APP_PROPERTIES",
    val initDbFiles: List<Resource> = listOf(ClassPathResource("db/h2/schema.sql"), ClassPathResource("db/h2/data.sql"))
)

@Component
@EnableConfigurationProperties(DatabaseConfiguration::class)
class VersioningDatabasePopulator(private val config: DatabaseConfiguration) : DatabasePopulator {
    override fun populate(connection: Connection): Mono<Void> {
        return connection.createStatement(config.showTablesCmd)
            .execute()
            .toFlux()
            .flatMap { r ->
                r.map { row, _ ->
                    row.get(config.tableNameCol, String::class.java)!!
                }
            }
            // Check for the creation of the app properties table to create the database.
            .any { it.toUpperCase() == config.initTable }
            .flatMap { created ->
                if (created) upgrade(connection) else createDb(connection)
            }
    }

    /**
     * Upgrades the database to the most recent version. This assumes that the
     * APP_PROPERTIES table has already been made.
     */
    fun upgrade(connection: Connection): Mono<Void> {
        return Mono.empty()
    }

    /**
     * Initialize database
     */
    fun createDb(connection: Connection): Mono<Void> {
        val populator = ResourceDatabasePopulator(*config.initDbFiles.toTypedArray())
        return populator.populate(connection)
    }
}