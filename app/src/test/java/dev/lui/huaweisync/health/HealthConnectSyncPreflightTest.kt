package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HealthConnectSyncPreflightTest {
    @Test
    fun availableProviderAndAllPermissionsAreReady() = runTest {
        val preflight = preflight(
            availability = HealthConnectAvailability.Available,
            grantedPermissions = REQUIRED_PERMISSIONS,
        )

        assertEquals(SyncPreflightResult.Ready, preflight.check())
    }

    @Test
    fun providerUpdateRequirementIsAnEnvironmentBlockWithoutPermissionLookup() = runTest {
        var permissionLookups = 0
        val preflight = HealthConnectSyncPreflight(
            availabilityCheck = { HealthConnectAvailability.ProviderUpdateRequired },
            grantedPermissions = {
                permissionLookups += 1
                REQUIRED_PERMISSIONS
            },
            requiredPermissions = REQUIRED_PERMISSIONS,
        )

        val result = preflight.check() as SyncPreflightResult.Blocked

        assertBlock(
            result,
            reason = SyncBlockReason.ENVIRONMENT,
            code = "HEALTH_CONNECT_PROVIDER_UPDATE_REQUIRED",
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        )
        assertEquals(0, permissionLookups)
    }

    @Test
    fun unavailableProviderIsAnEnvironmentBlockWithoutPermissionLookup() = runTest {
        var permissionLookups = 0
        val preflight = HealthConnectSyncPreflight(
            availabilityCheck = { HealthConnectAvailability.Unavailable },
            grantedPermissions = {
                permissionLookups += 1
                REQUIRED_PERMISSIONS
            },
            requiredPermissions = REQUIRED_PERMISSIONS,
        )

        val result = preflight.check() as SyncPreflightResult.Blocked

        assertBlock(
            result,
            reason = SyncBlockReason.ENVIRONMENT,
            code = "HEALTH_CONNECT_UNAVAILABLE",
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        )
        assertEquals(0, permissionLookups)
    }

    @Test
    fun anyMissingExercisePermissionIsAPermissionBlock() = runTest {
        val preflight = preflight(
            availability = HealthConnectAvailability.Available,
            grantedPermissions = setOf("exercise-read"),
        )

        val result = preflight.check() as SyncPreflightResult.Blocked

        assertBlock(
            result,
            reason = SyncBlockReason.PERMISSION,
            code = "EXERCISE_SESSION_PERMISSION_REQUIRED",
            safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
        )
    }

    @Test
    fun unexpectedPermissionLookupFailureBubbles() = runTest {
        val expected = IllegalStateException("private SDK failure")
        val preflight = HealthConnectSyncPreflight(
            availabilityCheck = { HealthConnectAvailability.Available },
            grantedPermissions = { throw expected },
            requiredPermissions = REQUIRED_PERMISSIONS,
        )

        val observed = try {
            preflight.check()
            throw AssertionError("Expected permission lookup failure")
        } catch (failure: IllegalStateException) {
            failure
        }

        assertSame(expected, observed)
    }

    @Test
    fun cancellationIsNeverConvertedToABlocker() = runTest {
        val expected = CancellationException("cancel")
        val preflight = HealthConnectSyncPreflight(
            availabilityCheck = { HealthConnectAvailability.Available },
            grantedPermissions = { throw expected },
            requiredPermissions = REQUIRED_PERMISSIONS,
        )

        val observed = try {
            preflight.check()
            throw AssertionError("Expected cancellation")
        } catch (failure: CancellationException) {
            failure
        }

        assertSame(expected, observed)
    }

    private fun preflight(
        availability: HealthConnectAvailability,
        grantedPermissions: Set<String>,
    ) = HealthConnectSyncPreflight(
        availabilityCheck = { availability },
        grantedPermissions = { grantedPermissions },
        requiredPermissions = REQUIRED_PERMISSIONS,
    )

    private fun assertBlock(
        result: SyncPreflightResult.Blocked,
        reason: SyncBlockReason,
        code: String,
        safeMessage: SyncDiagnosticMessage,
    ) {
        assertEquals(reason, result.block.reason)
        assertEquals(code, result.block.code)
        assertEquals(SyncErrorPhase.PREPARATION, result.block.phase)
        assertEquals(safeMessage, result.block.safeMessage)
    }

    private companion object {
        val REQUIRED_PERMISSIONS = setOf("exercise-read", "exercise-write")
    }
}
