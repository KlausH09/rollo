package rollo.services

import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import rollo.loadFromJson
import rollo.models.Rollo
import rollo.models.RolloConfig
import rollo.models.RolloEvent
import rollo.models.RolloPosition
import rollo.saveToJson
import java.io.File


class RolloService(
    private val rolloMap: Map<String, Rollo>
) {
    constructor() : this(rolloMap = runBlocking { createRolloMapFromJson() })

    private fun getRollo(name: String) = rolloMap[name] ?: throw NotFoundException()

    suspend fun move(name: String, position: RolloPosition) =
        getRollo(name).move(position)

    suspend fun changeStatus(name: String, event: RolloEvent) =
        getRollo(name).applyEvent(event)

    suspend fun getPosition(name: String) =
        getRollo(name).getCurrentStatus().position

    fun getConfig() = rolloMap.values.map { it.config.copy() }
    fun getConfig(name: String) = getRollo(name).config.copy()

    suspend fun updateConfig(config: RolloConfig) {
        getRollo(config.name).config = config
        rolloMap.values.map { it.config }.saveRolloConfigToJson()
    }

    companion object {
        private val rolloConfigJsonFile = File("./src/main/resources/rollos.json")

        private suspend fun loadRolloConfigFromJson(): Collection<RolloConfig> = loadFromJson(rolloConfigJsonFile)
        private suspend fun Iterable<RolloConfig>.saveRolloConfigToJson() =
            saveToJson(this.sortedBy { it.name }, rolloConfigJsonFile)

        private suspend fun createRolloMapFromJson() =
            loadRolloConfigFromJson().associate { it.name to Rollo(config = it) }
    }
}