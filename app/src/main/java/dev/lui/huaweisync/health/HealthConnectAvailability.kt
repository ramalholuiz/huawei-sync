package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

enum class HealthConnectAvailability { Available, ProviderUpdateRequired, Unavailable }

class HealthConnectAvailabilityChecker(
    private val context: Context,
) {
    fun check(): HealthConnectAvailability = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.Available
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.ProviderUpdateRequired
        else -> HealthConnectAvailability.Unavailable
    }
}
