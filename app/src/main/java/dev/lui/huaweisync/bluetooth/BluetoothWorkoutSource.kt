package dev.lui.huaweisync.bluetooth

import dev.lui.huaweisync.domain.DomainWorkout
import dev.lui.huaweisync.source.WorkoutSourceReader

class BluetoothWorkoutSource(
    private val readPayload: suspend () -> ByteArray,
) : WorkoutSourceReader {
    override suspend fun readWorkout(): DomainWorkout =
        BluetoothWorkoutSummaryCodec.decode(readPayload())
}

