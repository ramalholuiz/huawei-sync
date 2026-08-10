package dev.lui.huaweisync.source

import dev.lui.huaweisync.domain.DomainWorkout
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import java.time.Clock

fun interface WorkoutSourceReader {
    suspend fun readWorkout(): DomainWorkout
}

class SyntheticWorkoutSource(
    private val clock: Clock = Clock.systemUTC(),
) : WorkoutSourceReader {
    override suspend fun readWorkout(): DomainWorkout = SyntheticWorkoutFactory.create(clock)
}
