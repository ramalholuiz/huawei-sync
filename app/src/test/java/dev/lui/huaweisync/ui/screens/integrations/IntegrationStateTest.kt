package dev.lui.huaweisync.ui.screens.integrations

import dev.lui.huaweisync.ui.state.IntegrationEvidenceLevel
import dev.lui.huaweisync.ui.state.IntegrationItemState
import dev.lui.huaweisync.ui.state.IntegrationProvider
import dev.lui.huaweisync.ui.state.IntegrationState
import dev.lui.huaweisync.ui.state.IntegrationStatus
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrationStateTest {
    @Test
    fun `Health Connect is confirmed only from real readback evidence`() {
        val confirmed = IntegrationState.from(
            productState = productState(
                status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                readbackConfirmed = true,
            ),
        )
        val awaitingReadback = IntegrationState.from(
            productState = productState(
                status = ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK,
                readbackConfirmed = false,
            ),
        )

        assertEquals(
            IntegrationStatus.CONFIRMED_IN_HEALTH_CONNECT,
            confirmed.healthConnect.status,
        )
        assertEquals(IntegrationEvidenceLevel.READBACK, confirmed.healthConnect.evidenceLevel)
        assertEquals(IntegrationStatus.AWAITING_READBACK, awaitingReadback.healthConnect.status)
        assertFalse(
            awaitingReadback.healthConnect.status ==
                IntegrationStatus.CONFIRMED_IN_HEALTH_CONNECT,
        )
    }

    @Test
    fun `GymRats remains exactly ready to read before Gate 2`() {
        val gymRats = IntegrationState.from(productState = null).gymRats

        assertEquals(IntegrationStatus.READY_FOR_GYMRATS_TO_READ, gymRats.status)
        assertEquals("Available for GymRats to import", gymRats.status.label)
        assertEquals(IntegrationEvidenceLevel.LOCAL_STATE, gymRats.evidenceLevel)
    }

    @Test
    fun `Samsung only exposes configuration through Health Connect`() {
        val notConfigured = IntegrationState.from(
            productState = null,
            samsungHealthConfigured = false,
        ).samsungHealth
        val available = IntegrationState.from(
            productState = null,
            samsungHealthConfigured = true,
        ).samsungHealth

        assertEquals(IntegrationStatus.NOT_CONFIGURED, notConfigured.status)
        assertEquals(IntegrationStatus.AVAILABLE_THROUGH_HEALTH_CONNECT, available.status)
    }

    @Test
    fun `unsupported providers are roadmap only with non-operational statuses`() {
        val future = IntegrationState.from(productState = null).futureProviders

        assertEquals(
            setOf(
                IntegrationProvider.GARMIN,
                IntegrationProvider.FITBIT,
                IntegrationProvider.OURA,
                IntegrationProvider.STRAVA,
                IntegrationProvider.GOOGLE_FIT,
            ),
            future.map { it.provider }.toSet(),
        )
        assertTrue(
            future.all {
                it.status == IntegrationStatus.PREVIEW ||
                    it.status == IntegrationStatus.COMING_SOON
            },
        )
        assertTrue(future.all { it.evidenceLevel == IntegrationEvidenceLevel.ROADMAP_ONLY })
    }

    @Test
    fun `presentation copy forbids unsupported success claims`() {
        val copy = IntegrationState.from(
            productState = productState(
                status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                readbackConfirmed = true,
            ),
            samsungHealthConfigured = true,
        ).allProviders.flatMap { listOf(it.status.label, it.detail) }

        val forbiddenOutcomeWords = Regex("\\b(delivered|updated|synced)\\b", RegexOption.IGNORE_CASE)
        val unsupportedConsumerClaims = listOf(
            Regex("confirmed (in|by) gymrats", RegexOption.IGNORE_CASE),
            Regex("imported (in|by|into) gymrats", RegexOption.IGNORE_CASE),
            Regex("visible in gymrats", RegexOption.IGNORE_CASE),
            Regex("read by gymrats", RegexOption.IGNORE_CASE),
            Regex("confirmed (in|by) samsung", RegexOption.IGNORE_CASE),
            Regex("imported (in|by|into) samsung", RegexOption.IGNORE_CASE),
        )

        copy.forEach { text ->
            assertFalse("Forbidden success word in: $text", forbiddenOutcomeWords.containsMatchIn(text))
            unsupportedConsumerClaims.forEach { claim ->
                assertFalse("Unsupported consumer claim in: $text", claim.containsMatchIn(text))
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `future provider cannot be represented as operational`() {
        val base = IntegrationState.from(productState = null)
        IntegrationState(
            healthConnect = base.healthConnect,
            gymRats = base.gymRats,
            samsungHealth = base.samsungHealth,
            futureProviders = listOf(
                IntegrationItemState(
                    provider = IntegrationProvider.STRAVA,
                    status = IntegrationStatus.READY_TO_WRITE,
                    detail = "Invalid operational state.",
                    evidenceLevel = IntegrationEvidenceLevel.LOCAL_STATE,
                ),
            ),
        )
    }

    private fun productState(
        status: ProductHealthConnectStatus,
        readbackConfirmed: Boolean,
    ) = ProductSyncState(
        healthConnectStatus = status,
        gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
        phase = ProductSyncPhase.IDLE,
        attemptCount = 1,
        ledgerWorkoutCount = 1,
        verification = ProductVerificationEvidence(
            diagnosticEvidence = null,
            realReadbackConfirmed = readbackConfirmed,
            healthConnectMatchCount = if (readbackConfirmed) 1 else null,
            expectedVersionMatchCount = if (readbackConfirmed) 1 else null,
            versionMatch = if (readbackConfirmed) true else null,
        ),
        sanitizedFailureSummary = null,
    )
}
