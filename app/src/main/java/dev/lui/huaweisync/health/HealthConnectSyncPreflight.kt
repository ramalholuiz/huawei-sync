package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dev.lui.huaweisync.data.SyncBlock
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase

/** Real Health Connect readiness boundary used immediately before a coordinated write attempt. */
class HealthConnectSyncPreflight internal constructor(
    private val availabilityCheck: () -> HealthConnectAvailability,
    private val grantedPermissions: suspend () -> Set<String>,
    private val requiredPermissions: Set<String>,
) : SyncPreflight {
    constructor(context: Context) : this(
        availabilityCheck = HealthConnectAvailabilityChecker(context)::check,
        grantedPermissions = {
            HealthConnectClient.getOrCreate(context)
                .permissionController
                .getGrantedPermissions()
        },
        requiredPermissions = HealthConnectPermissions.exerciseSessionPermissions,
    )

    override suspend fun check(): SyncPreflightResult = when (availabilityCheck()) {
        HealthConnectAvailability.ProviderUpdateRequired -> environmentBlocked(
            code = "HEALTH_CONNECT_PROVIDER_UPDATE_REQUIRED",
        )
        HealthConnectAvailability.Unavailable -> environmentBlocked(
            code = "HEALTH_CONNECT_UNAVAILABLE",
        )
        HealthConnectAvailability.Available -> {
            if (grantedPermissions().containsAll(requiredPermissions)) {
                SyncPreflightResult.Ready
            } else {
                SyncPreflightResult.Blocked(
                    SyncBlock(
                        reason = SyncBlockReason.PERMISSION,
                        code = "EXERCISE_SESSION_PERMISSION_REQUIRED",
                        phase = SyncErrorPhase.PREPARATION,
                        safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
                    ),
                )
            }
        }
    }

    private fun environmentBlocked(code: String) = SyncPreflightResult.Blocked(
        SyncBlock(
            reason = SyncBlockReason.ENVIRONMENT,
            code = code,
            phase = SyncErrorPhase.PREPARATION,
            safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
        ),
    )
}
