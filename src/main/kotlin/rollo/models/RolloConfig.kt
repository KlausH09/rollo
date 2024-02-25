package rollo.models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class RolloConfig(
    val name: String,
    val closeDuration: Duration,
    val openDuration: Duration,
) {
    fun getSpeed(direction: RolloMoveDirection): Double {
        val fullDurationInSeconds = when (direction) {
            RolloMoveDirection.UP -> openDuration
            RolloMoveDirection.DOWN -> closeDuration
            RolloMoveDirection.STOPPED -> return 0.0
        }.inWholeMilliseconds.toDouble() / 1000.0
        return 1.0 / fullDurationInSeconds
    }
}
