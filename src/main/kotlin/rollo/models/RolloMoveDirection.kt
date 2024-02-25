package rollo.models

enum class RolloMoveDirection {
    UP, DOWN, STOPPED;

    val reverse
        get(): RolloMoveDirection = when (this) {
            UP -> DOWN
            DOWN -> UP
            STOPPED -> STOPPED
        }

    companion object {
        fun fromPositions(fromPos: RolloPosition, toPos: RolloPosition): RolloMoveDirection? {
            return if (fromPos.isApprox(toPos))
                null
            else if (fromPos.pos < toPos.pos)
                DOWN
            else
                UP
        }
    }
}
