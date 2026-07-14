package dev.lui.huaweisync.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class WorkoutMetadataPolicyTest {
    private val workout = DomainWorkout(
        source = WorkoutSource.SYNTHETIC,
        sourceWorkoutId = "source:α/β",
        title = "Strength: α",
        activityKind = DomainActivityKind.STRENGTH_TRAINING,
        startTime = Instant.parse("2026-07-14T11:15:00Z"),
        endTime = Instant.parse("2026-07-14T12:00:00Z"),
        startZoneOffset = ZoneOffset.ofHours(-3),
        endZoneOffset = null,
        notes = "Synthetic Gate 1 record written by huawei-sync.",
        deviceName = "ignored until mapped",
    )

    @Test
    fun identityIsVersionedDeterministicAndUnambiguousForUtf8Components() {
        val first = WorkoutMetadataPolicy.clientRecordIdFor("synthetic:a", "b")
        val ambiguousIfJoined = WorkoutMetadataPolicy.clientRecordIdFor("synthetic", "a:b")
        val unicode = WorkoutMetadataPolicy.clientRecordIdFor("synthetic", "treino-ação")

        assertEquals(first, WorkoutMetadataPolicy.clientRecordIdFor("synthetic:a", "b"))
        assertNotEquals(first, ambiguousIfJoined)
        assertEquals(
            "huawei-sync:v1:0708ca72cf570f7d793ef6ae14bf5e0fcf1b60bdb23facee8a1d38b4de6c2931",
            unicode,
        )
    }

    @Test
    fun equivalentSemanticContentHasTheSameCanonicalHash() {
        assertEquals(
            "39626b67b2592f95966178cdbe28e30ece825d8219b3a9c7abe4c748f94964bd",
            WorkoutMetadataPolicy.contentHashFor(workout),
        )
        assertEquals(
            WorkoutMetadataPolicy.contentHashFor(workout),
            WorkoutMetadataPolicy.contentHashFor(workout.copy(deviceName = "not emitted")),
        )
    }

    @Test
    fun everyEmittedExerciseSessionFieldChangesTheSemanticHash() {
        val baseline = WorkoutMetadataPolicy.contentHashFor(workout)
        val changes = listOf(
            workout.copy(startTime = workout.startTime.minusSeconds(1)),
            workout.copy(endTime = workout.endTime.plusSeconds(1)),
            workout.copy(startZoneOffset = ZoneOffset.UTC),
            workout.copy(endZoneOffset = ZoneOffset.ofHours(1)),
            workout.copy(title = "Different title"),
            workout.copy(notes = "Different notes"),
        )

        changes.forEach { changed -> assertNotEquals(baseline, WorkoutMetadataPolicy.contentHashFor(changed)) }
    }

    @Test
    fun sourceIdentityDoesNotAffectSemanticHashButDoesAffectClientIdentity() {
        val changedSource = workout.copy(sourceWorkoutId = "another-source")

        assertEquals(
            WorkoutMetadataPolicy.contentHashFor(workout),
            WorkoutMetadataPolicy.contentHashFor(changedSource),
        )
        assertNotEquals(
            WorkoutMetadataPolicy.resolve(workout).clientRecordId,
            WorkoutMetadataPolicy.resolve(changedSource).clientRecordId,
        )
    }

    @Test
    fun versionStartsAtOnePreservesSameHashAndIncrementsChangedHashExactlyOnce() {
        val fresh = WorkoutMetadataPolicy.resolve(workout)
        val unchanged = WorkoutMetadataPolicy.resolve(workout, fresh.asPrevious())
        val changedWorkout = workout.copy(title = "Changed")
        val changed = WorkoutMetadataPolicy.resolve(changedWorkout, unchanged.asPrevious())
        val repeatedChange = WorkoutMetadataPolicy.resolve(changedWorkout, changed.asPrevious())

        assertEquals(1L, fresh.clientRecordVersion)
        assertEquals(1L, unchanged.clientRecordVersion)
        assertEquals(2L, changed.clientRecordVersion)
        assertEquals(2L, repeatedChange.clientRecordVersion)
    }

    @Test(expected = IllegalStateException::class)
    fun changedContentRejectsVersionOverflow() {
        WorkoutMetadataPolicy.resolve(
            workout.copy(title = "Changed"),
            PreviousWorkoutMetadata(
                contentHash = WorkoutMetadataPolicy.contentHashFor(workout),
                clientRecordVersion = Long.MAX_VALUE,
            ),
        )
    }

    @Test
    fun rejectsMalformedPriorMetadataAndInvalidWorkoutBoundaries() {
        assertFails<IllegalArgumentException> {
            WorkoutMetadataPolicy.resolve(
                workout,
                PreviousWorkoutMetadata(contentHash = "not-a-sha256", clientRecordVersion = 1),
            )
        }
        assertFails<IllegalArgumentException> {
            WorkoutMetadataPolicy.resolve(
                workout,
                PreviousWorkoutMetadata(
                    contentHash = WorkoutMetadataPolicy.contentHashFor(workout),
                    clientRecordVersion = 0,
                ),
            )
        }
        assertFails<IllegalArgumentException> {
            WorkoutMetadataPolicy.resolve(workout.copy(sourceWorkoutId = " "))
        }
        assertFails<IllegalArgumentException> {
            WorkoutMetadataPolicy.resolve(workout.copy(endTime = workout.startTime))
        }
    }

    private fun ResolvedWorkoutMetadata.asPrevious() = PreviousWorkoutMetadata(
        contentHash = contentHash,
        clientRecordVersion = clientRecordVersion,
    )

    private inline fun <reified T : Throwable> assertFails(block: () -> Unit) {
        val failure = runCatching(block).exceptionOrNull()
        assertTrue("Expected ${T::class.java.simpleName}, got $failure", failure is T)
    }
}
