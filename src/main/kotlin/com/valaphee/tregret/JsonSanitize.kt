/*
 * Copyright (c) 2021-2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.tregret

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

fun jsonSanitize(path: File) {
    val chat = jacksonObjectMapper().apply { disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) }.readValue<Chat>(File(path, "result.json.in"))
    jacksonObjectMapper().writeValue(File(path, "result.json"), Chat(chat.type, chat.messages.map { it.copy(from = it.from, photo = it.photo?.takeIf { File(path, it).exists() }) }))
}
