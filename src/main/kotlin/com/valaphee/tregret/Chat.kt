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

import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Chat(
    val type: String,
    val messages: List<Message>
) {
    data class Message(
        val id: Int,
        val type: String,
        val date: String,
        val from: String?,
        /*val fromId: String,*/
        val file: String?,
        val thumbnail: String?,
        val mediaType: String?,
        val stickerEmoji: String?,
        val photo: String?,
        val contactVcard: String?,
        val durationSeconds: Int?,
        val width: Int?,
        val height: Int?,
        val text: Any
    )
}
