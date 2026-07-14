package dev.lui.huaweisync.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class SyntheticWorkoutFactoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC"))

    @Test
    fun syntheticWorkoutUsesDeterministicClientRecordIdAcrossRuns() {
        val first = SyntheticWorkoutFactory.create(clock)
        val second = SyntheticWorkoutFactory.create(clock)

        assertEquals(
            "huawei-sync:synthetic:gate1-strength-training",
            SyntheticWorkoutFactory.clientRecordIdFor(first),
        )
        assertEquals(
            SyntheticWorkoutFactory.clientRecordIdFor(first),
            SyntheticWorkoutFactory.clientRecordIdFor(second),
        )
    }

    @Test
    fun syntheticWorkoutDefinesStableClientRecordVersionStrategy() {
        val workout = SyntheticWorkoutFactory.create(clock)

        assertEquals(1L, workout.version)
        assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_VERSION, workout.version)
    }
}
