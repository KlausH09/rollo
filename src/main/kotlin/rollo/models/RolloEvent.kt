package rollo.models

import java.time.LocalDateTime


data class RolloEvent(
    val moveDirection: RolloMoveDirection,
    val timestamp: LocalDateTime,
) {
    constructor(moveDirection: RolloMoveDirection) : this(
        moveDirection=moveDirection,
        timestamp = LocalDateTime.now()
    )
}