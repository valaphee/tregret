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

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.jpeg.JpegDirectory
import com.drew.metadata.mp4.media.Mp4VideoDirectory
import com.drew.metadata.webp.WebpDirectory
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.io.File
import java.time.Duration
import java.time.format.DateTimeFormatter

fun main() {
    val path = File("M:\\DataExport_2022-02-11")

    val messages = mutableListOf<Chat.Message>()
    lateinit var from: String

    var i = 0
    while (true) {
        val htmlFile = File(path, "chats\\chat_008\\messages${if (i != 0) i + 1 else ""}.html")
        if (!htmlFile.exists()) break

        Jsoup.parse(htmlFile, null).select(".message.default").forEach {
            if (it.`is`(".default")) {
                it.selectFirst(".from_name")?.let { from = it.text() }
                var file: File? = null
                var thumbnail: File? = null
                var mediaType: String? = null
                var stickerEmoji: String? = null
                var photo: File? = null
                var durationSeconds: Int? = null
                var width: Int? = null
                var height: Int? = null
                it.selectFirst(".photo_wrap")?.let {
                    photo = File(htmlFile.parent, it.attr("href")).also {
                        val jpegDirectory = ImageMetadataReader.readMetadata(it).getFirstDirectoryOfType(JpegDirectory::class.java)
                        width = jpegDirectory.imageWidth
                        height = jpegDirectory.imageHeight
                    }
                } ?: it.selectFirst(".animated_wrap")?.let {
                    file = File(htmlFile.parent, it.attr("href"))
                    thumbnail = File(htmlFile.parent, it.selectFirst(".animated")!!.attr("src"))
                    mediaType = "animation"
                } ?: it.selectFirst(".media_video")?.let {
                    file = File(htmlFile.parent, it.attr("href"))
                    mediaType = "animation"
                } ?: it.selectFirst(".video_file_wrap")?.let {
                    file = File(htmlFile.parent, it.attr("href")).also {
                        val mp4VideoDirectory = ImageMetadataReader.readMetadata(it).getFirstDirectoryOfType(Mp4VideoDirectory::class.java)
                        width = mp4VideoDirectory.getInt(Mp4VideoDirectory.TAG_WIDTH)
                        height = mp4VideoDirectory.getInt(Mp4VideoDirectory.TAG_HEIGHT)
                    }
                    thumbnail = File(htmlFile.parent, it.selectFirst(".video_file")!!.attr("src"))
                    mediaType = "video_file"
                    durationSeconds = Duration.parse(it.selectFirst(".video_duration")!!.text().replace(durationPattern, "PT$1M$2S")).toSeconds().toInt()
                } ?: it.selectFirst(".sticker_wrap")?.let {
                    file = File(htmlFile.parent, it.attr("href")).also {
                        val webpDirectory = ImageMetadataReader.readMetadata(it).getFirstDirectoryOfType(WebpDirectory::class.java)
                        width = webpDirectory.getInt(WebpDirectory.TAG_IMAGE_WIDTH)
                        height = webpDirectory.getInt(WebpDirectory.TAG_IMAGE_HEIGHT)
                    }
                    thumbnail = File(htmlFile.parent, it.selectFirst(".sticker")!!.attr("src"))
                    mediaType = "sticker"
                    stickerEmoji = ""
                } ?: it.selectFirst(".media_photo")?.let {
                    file = File(htmlFile.parent, it.attr("href"))
                    mediaType = "sticker"
                    stickerEmoji = it.selectFirst(".status.details")!!.text()
                    width = 512
                    height = 512
                } ?: it.selectFirst(".media_voice_message")?.let {
                    file = File(htmlFile.parent, it.attr("href"))
                    mediaType = "voice_message"
                    durationSeconds = Duration.parse(it.selectFirst(".status.details")!!.text().replace(durationPattern, "PT$1M$2S")).toSeconds().toInt()
                } ?: it.selectFirst(".media_file")?.let {
                    mediaType = ""
                    file = File(htmlFile.parent, it.attr("href"))
                }
                messages += Chat.Message(it.attr("id").substring(7).toInt(), "message", jsonDateTimeFormatter.format(htmlDateTimeFormatter.parse(it.selectFirst(".date.details")!!.attr("title"))), from, "user", file?.toRelativeString(path)?.replace('\\', '/'), thumbnail?.toRelativeString(path)?.replace('\\', '/'), mediaType, stickerEmoji, photo?.toRelativeString(path)?.replace('\\', '/'), null, durationSeconds, width, height, it.selectFirst(".text")?.let {
                    StringBuilder().apply {
                        it.childNodes().forEach {
                            when (it) {
                                is TextNode -> append(it.text())
                                is Element -> when (it.tagName()) {
                                    "br" -> appendLine()
                                    "a" -> append(it.attr("href"))
                                    "u" -> append("_${it.text()}_")
                                    "s" -> append("~${it.text()}~")
                                }
                            }
                        }
                    }.toString().trim()
                } ?: "")
            }
        }
        i++
    }

    jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        /*setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        enable(SerializationFeature.INDENT_OUTPUT)*/
    }.writeValue(File(path, "result.json"), Chat("personal_chat", messages))
}

private val htmlDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
private val jsonDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
private val durationPattern = "(\\d{1,2}):(\\d{1,2})".toRegex()
