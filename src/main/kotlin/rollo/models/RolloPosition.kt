package rollo.models

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@JvmInline
value class RolloPosition(
    val pos: Double,
) {
    init {
        assert(pos in 0.0..1.0)
    }

    val intPosition get() = (pos * 100.0).roundToInt()

    fun isApprox(other: RolloPosition): Boolean =
        (pos - other.pos).absoluteValue < 0.05

    fun move(length: Double, direction: RolloMoveDirection): RolloPosition {
        val sign = when (direction) {
            RolloMoveDirection.UP -> -1.0
            RolloMoveDirection.DOWN -> 1.0
            RolloMoveDirection.STOPPED -> return this
        }
        return (pos + sign * length).toRolloPosition()
    }

    companion object {
        val ClosedLimit = RolloPosition(1.0)
        val OpenedLimit = RolloPosition(0.0)

        fun Double.toRolloPosition() =
            RolloPosition(min(1.0, max(0.0, this)))

        fun Int.toRolloPosition() =
            (toDouble() / 100.0).toRolloPosition()
    }
}
