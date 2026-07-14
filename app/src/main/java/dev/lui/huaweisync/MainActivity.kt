package dev.lui.huaweisync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.health.Gate1SyncCoordinator
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.health.HealthConnectAvailabilityChecker
import dev.lui.huaweisync.health.HealthConnectPermissions
import dev.lui.huaweisync.health.HealthConnectSyncPreflight
import dev.lui.huaweisync.health.HealthConnectWorkoutWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as HuaweiSyncApp
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val scope = rememberCoroutineScope()
                    val availabilityChecker = remember { HealthConnectAvailabilityChecker(this) }
                    var availability by remember { mutableStateOf(availabilityChecker.check()) }
                    var status by remember { mutableStateOf("Gate 1 ready. Check Health Connect, grant permissions, then run synthetic sync.") }
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = HealthConnectPermissions.requestContract,
                    ) { granted ->
                        status = if (granted.containsAll(HealthConnectPermissions.exerciseSessionPermissions)) {
                            "Exercise session read/write permissions granted."
                        } else {
                            "Missing ExerciseSessionRecord read/write permissions."
                        }
                    }

                    LaunchedEffect(Unit) {
                        availability = availabilityChecker.check()
                    }

                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("Huawei Sync — Gate 1")
                        Text("Health Connect availability: $availability")
                        Text(status)
                        Button(onClick = { availability = availabilityChecker.check() }) {
                            Text("Check Health Connect availability")
                        }
                        Button(
                            enabled = availability == HealthConnectAvailability.Available,
                            onClick = {
                                permissionLauncher.launch(HealthConnectPermissions.exerciseSessionPermissions)
                            },
                        ) {
                            Text("Request ExerciseSessionRecord permissions")
                        }
                        Button(
                            enabled = availability == HealthConnectAvailability.Available,
                            onClick = {
                                scope.launch {
                                    status = try {
                                        val result = Gate1SyncCoordinator(
                                            ledgerStore = app.ledgerStore,
                                            writer = HealthConnectWorkoutWriter(applicationContext),
                                            preflight = HealthConnectSyncPreflight(applicationContext),
                                        ).runSyntheticStrengthSync()
                                        when (result) {
                                            is Gate1SyncResult.Blocked ->
                                                "Sync blocked [${result.code}]. Writes: ${result.writeCountForClientRecordId}."
                                            is Gate1SyncResult.WriteFailed ->
                                                "Sync write failed [${result.code}]. Attempts: ${result.writeCountForClientRecordId}."
                                            else ->
                                                "Sync outcome recorded for ${result.clientRecordId} v${result.clientRecordVersion}. Ledger rows: ${result.ledgerRowsForClientRecordId}; writes: ${result.writeCountForClientRecordId}."
                                        }
                                    } catch (cancellation: CancellationException) {
                                        throw cancellation
                                    } catch (_: Exception) {
                                        "Gate 1 sync failed before a structured outcome."
                                    }
                                }
                            },
                        ) {
                            Text("Run synthetic strength sync")
                        }
                    }
                }
            }
        }
    }
}
