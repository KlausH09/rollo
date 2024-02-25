package rollo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File


@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> loadFromJson(file: File): T = withContext(Dispatchers.IO) {
    Json.decodeFromStream<T>(file.inputStream())
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> saveToJson(data: T, file: File) = withContext(Dispatchers.IO) {
    file.outputStream().use {
        Json.encodeToStream(data, it)
    }
}