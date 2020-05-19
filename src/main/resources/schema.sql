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

-- noinspection SqlResolveForFile @ object-type/"SERIAL"

CREATE TABLE IF NOT EXISTS role_kick_rule
(
    id               SERIAL PRIMARY KEY,
    guild_id         LONG    NOT NULL,
    role_id          LONG    NOT NULL UNIQUE,
    time_til_warning VARCHAR NOT NULL,
    time_til_kick    VARCHAR NOT NULL,
    warning_message  VARCHAR NOT NULL
);

CREATE INDEX IF NOT EXISTS role_kick_rule_guild_id_index ON role_kick_rule (guild_id);
CREATE INDEX IF NOT EXISTS role_kick_rule_role_id_index ON role_kick_rule (role_id);

CREATE TABLE IF NOT EXISTS server_prefix
(
    guild_id LONG PRIMARY KEY NOT NULL,
    prefix   VARCHAR          NOT NULL
)