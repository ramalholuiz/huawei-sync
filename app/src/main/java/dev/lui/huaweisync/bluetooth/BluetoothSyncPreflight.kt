package dev.lui.huaweisync.bluetooth

import dev.lui.huaweisync.data.SyncBlock
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.health.SyncPreflight
import dev.lui.huaweisync.health.SyncPreflightResult

data class BluetoothCapabilitySnapshot(
    val adapterPresent: Boolean,
    val enabled: Boolean,
    val scanPermissionGranted: Boolean,
    val connectPermissionGranted: Boolean,
    val companionPairingAvailable: Boolean,
    val legacyLocationPermissionGranted: Boolean = true,
)

fun interface BluetoothCapabilityReader {
    fun snapshot(): BluetoothCapabilitySnapshot
}

class BluetoothSyncPreflight(
    private val capabilities: BluetoothCapabilityReader,
) : SyncPreflight {
    override suspend fun check(): SyncPreflightResult {
        val snapshot = capabilities.snapshot()
        return when {
            !snapshot.adapterPresent -> environmentBlocked("BLUETOOTH_ADAPTER_UNAVAILABLE")
            !snapshot.enabled -> environmentBlocked("BLUETOOTH_DISABLED")
            !snapshot.scanPermissionGranted ||
                !snapshot.connectPermissionGranted ||
                !snapshot.legacyLocationPermissionGranted -> permissionBlocked()
            !snapshot.companionPairingAvailable -> environmentBlocked("BLUETOOTH_PAIRING_UNAVAILABLE")
            else -> SyncPreflightResult.Ready
        }
    }

    private fun permissionBlocked() = SyncPreflightResult.Blocked(
        SyncBlock(
            reason = SyncBlockReason.PERMISSION,
            code = "BLUETOOTH_PERMISSION_REQUIRED",
            phase = SyncErrorPhase.PREPARATION,
            safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
        ),
    )

    private fun environmentBlocked(code: String) = SyncPreflightResult.Blocked(
        SyncBlock(
            reason = SyncBlockReason.ENVIRONMENT,
            code = code,
            phase = SyncErrorPhase.PREPARATION,
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        ),
    )
}
