package dev.lui.huaweisync.bluetooth

import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.health.SyncPreflightResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BluetoothSyncPreflightTest {
    @Test
    fun readyWhenAdapterPermissionsAndPairingAreAvailable() = runTest {
        assertEquals(SyncPreflightResult.Ready, preflight(READY).check())
    }

    @Test
    fun missingAdapterBlocksEnvironment() = runTest {
        assertBlock(
            preflight(READY.copy(adapterPresent = false)).check() as SyncPreflightResult.Blocked,
            reason = SyncBlockReason.ENVIRONMENT,
            code = "BLUETOOTH_ADAPTER_UNAVAILABLE",
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        )
    }

    @Test
    fun disabledBluetoothBlocksEnvironment() = runTest {
        assertBlock(
            preflight(READY.copy(enabled = false)).check() as SyncPreflightResult.Blocked,
            reason = SyncBlockReason.ENVIRONMENT,
            code = "BLUETOOTH_DISABLED",
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        )
    }

    @Test
    fun missingScanOrConnectPermissionBlocksPermission() = runTest {
        listOf(
            READY.copy(scanPermissionGranted = false),
            READY.copy(connectPermissionGranted = false),
            READY.copy(legacyLocationPermissionGranted = false),
        ).forEach { snapshot ->
            assertBlock(
                preflight(snapshot).check() as SyncPreflightResult.Blocked,
                reason = SyncBlockReason.PERMISSION,
                code = "BLUETOOTH_PERMISSION_REQUIRED",
                safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
            )
        }
    }

    @Test
    fun unavailablePairingBlocksEnvironmentBeforeAnyTransportClaim() = runTest {
        assertBlock(
            preflight(READY.copy(companionPairingAvailable = false)).check() as SyncPreflightResult.Blocked,
            reason = SyncBlockReason.ENVIRONMENT,
            code = "BLUETOOTH_PAIRING_UNAVAILABLE",
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        )
    }

    private fun preflight(snapshot: BluetoothCapabilitySnapshot) =
        BluetoothSyncPreflight(BluetoothCapabilityReader { snapshot })

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
        val READY = BluetoothCapabilitySnapshot(
            adapterPresent = true,
            enabled = true,
            scanPermissionGranted = true,
            connectPermissionGranted = true,
            companionPairingAvailable = true,
        )
    }
}
