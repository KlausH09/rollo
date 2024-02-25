package rollo.models

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

data class RolloStatus(
    val moveDirection: RolloMoveDirection,
    val position: RolloPosition,
    val timestamp: LocalDateTime,
) {
    fun applyEvent(config: RolloConfig, event: RolloEvent): RolloStatus {
        val status = statusAtTime(config, event.timestamp)
        val newMoveDirection = when (status.moveDirection) {
            RolloMoveDirection.UP -> when (event.moveDirection) {
                RolloMoveDirection.DOWN -> RolloMoveDirection.STOPPED
                RolloMoveDirection.UP -> RolloMoveDirection.UP
                RolloMoveDirection.STOPPED -> throw NotImplementedError()
            }

            RolloMoveDirection.DOWN -> when (event.moveDirection) {
                RolloMoveDirection.UP -> RolloMoveDirection.STOPPED
                RolloMoveDirection.DOWN -> RolloMoveDirection.DOWN
                RolloMoveDirection.STOPPED -> throw NotImplementedError()
            }

            RolloMoveDirection.STOPPED -> when (event.moveDirection) {
                RolloMoveDirection.UP -> RolloMoveDirection.UP
                RolloMoveDirection.DOWN -> RolloMoveDirection.DOWN
                RolloMoveDirection.STOPPED -> throw NotImplementedError()
            }
        }
        return status.copy(moveDirection = newMoveDirection)
    }

    fun statusAtTime(config: RolloConfig, newTimestamp: LocalDateTime = LocalDateTime.now()): RolloStatus {
        if (moveDirection == RolloMoveDirection.STOPPED)
            return copy(timestamp = LocalDateTime.now())
        if (newTimestamp < timestamp) throw NotImplementedError()
        val dt = ChronoUnit.SECONDS.between(timestamp, newTimestamp)
        val speed = config.getSpeed(moveDirection)
        return copy(
            position = position.move(length = dt * speed, direction = moveDirection),
            timestamp = newTimestamp,
        )
    }

    fun whenReachPosition(config: RolloConfig, newPosition: RolloPosition): LocalDateTime? {
        if (isFixedAtPosition(newPosition)) return LocalDateTime.now()
        if (moveDirection == RolloMoveDirection.STOPPED) return null
        val expectedDirection = RolloMoveDirection.fromPositions(
            fromPos = position,
            toPos = newPosition,
        ) ?: return null
        if (expectedDirection != moveDirection) return null
        val speed = config.getSpeed(moveDirection)
        val dt = (position.pos - newPosition.pos).absoluteValue / speed
        return timestamp.plusSeconds(dt.roundToLong())
    }

    fun isFixedAtPosition(newPosition: RolloPosition): Boolean {
        if (moveDirection != RolloMoveDirection.STOPPED)
            return false
        if (!position.isApprox(newPosition))
            return false
        return true
    }

}