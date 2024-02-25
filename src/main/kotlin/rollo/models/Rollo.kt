package rollo.models

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rollo.LoggerDelegate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Rollo(
    var config: RolloConfig,
    private var _status: RolloStatus,
    private var _job: Job? = null,
    private val statusLock: Mutex = Mutex(),
    private val jobLock: Mutex = Mutex(),
) {

    constructor(
        config: RolloConfig,
        moveDirection: RolloMoveDirection = RolloMoveDirection.STOPPED,
        position: RolloPosition = RolloPosition.OpenedLimit,
    ) : this(
        config = config,
        _status = RolloStatus(
            moveDirection = moveDirection,
            position = position,
            timestamp = LocalDateTime.now(),
        )
    )

    suspend fun getStatus() = statusLock.withLock { _status }
    suspend fun getCurrentStatus() = getStatus().statusAtTime(config)

    suspend fun applyEvent(event: RolloEvent) {
        jobLock.withLock { cancelNextJob() }
        changeStatus(event)
        log.info("Rollo '${config.name}' applied event '${event.moveDirection}'")
    }

    private suspend fun changeStatus(event: RolloEvent) {
        statusLock.withLock { _status = _status.applyEvent(config, event) }
    }

    suspend fun send(moveDirection: RolloMoveDirection) {
        // TODO
        log.info("Rollo '${config.name}' send $moveDirection")
        changeStatus(RolloEvent(moveDirection = moveDirection))
    }

    suspend fun cancelNextJob() {
        _job?.cancelAndJoin()
        if (_job != null) log.info("Rollo '${config.name}' canceled job")
        _job = null
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun move(position: RolloPosition) = jobLock.withLock {
        cancelNextJob()

        if (position == RolloPosition.OpenedLimit) {
            send(RolloMoveDirection.UP)
            send(RolloMoveDirection.UP)
            return
        }
        if (position == RolloPosition.ClosedLimit) {
            send(RolloMoveDirection.DOWN)
            send(RolloMoveDirection.DOWN)
            return
        }
        var status = getStatus()
        if (status.isFixedAtPosition(position))
            return

        val direction = RolloMoveDirection.fromPositions(
            fromPos = status.statusAtTime(config).position,
            toPos = position,
        ) ?: throw NotImplementedError()
        send(direction)
        send(direction)
        status = getStatus()
        val timeStop = status.whenReachPosition(config, position) ?: throw NotImplementedError()
        val moveDirection = status.moveDirection.reverse
        val executeTime = timeStop
        _job = GlobalScope.launch {

            val dt = ChronoUnit.MILLIS.between(LocalDateTime.now(), executeTime)
            if (dt > 0) delay(dt)
            send(moveDirection)
        }
    }


    suspend fun sendAsync(
        moveDirection: RolloMoveDirection,
        executeTime: LocalDateTime,
    ) {
        val dt = ChronoUnit.MILLIS.between(LocalDateTime.now(), executeTime)
        if (dt > 0) delay(dt)
        send(moveDirection)
    }

    companion object {
        val log by LoggerDelegate()
    }
}
