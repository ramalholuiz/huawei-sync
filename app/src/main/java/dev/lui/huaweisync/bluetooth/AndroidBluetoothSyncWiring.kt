package dev.lui.huaweisync.bluetooth

import android.content.Context
import dev.lui.huaweisync.health.Gate1SyncCoordinator
import dev.lui.huaweisync.health.Gate1SyncLedger
import dev.lui.huaweisync.health.HealthConnectWorkoutInspector
import dev.lui.huaweisync.health.HealthConnectWorkoutWriter

object AndroidBluetoothSyncWiring {
    fun workoutSource(context: Context): BluetoothWorkoutSource =
        BluetoothWorkoutSource(AndroidBleWorkoutPayloadReader(context)::readPayload)

    fun coordinator(
        context: Context,
        ledgerStore: Gate1SyncLedger,
    ): Gate1SyncCoordinator {
        val inspector = HealthConnectWorkoutInspector(context)
        return Gate1SyncCoordinator(
            ledgerStore = ledgerStore,
            writer = HealthConnectWorkoutWriter(context),
            sourceReader = workoutSource(context),
            preflight = BluetoothSyncPreflight(AndroidBluetoothCapabilityReader(context)),
            confirmer = inspector,
            reconciler = inspector,
        )
    }
}

