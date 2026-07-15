package dev.lui.huaweisync

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.health.connect.client.HealthConnectClient
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.Gate1Diagnostics
import dev.lui.huaweisync.diagnostics.Gate1ExportInput
import dev.lui.huaweisync.diagnostics.Gate1RuntimeDiagnostics
import dev.lui.huaweisync.diagnostics.HealthConnectAvailability as DiagnosticAvailability
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.Gate1SyncCoordinator
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.health.HealthConnectAvailabilityChecker
import dev.lui.huaweisync.health.HealthConnectPermissions
import dev.lui.huaweisync.health.HealthConnectSyncPreflight
import dev.lui.huaweisync.health.HealthConnectWorkoutInspector
import dev.lui.huaweisync.health.HealthConnectWorkoutWriter
import java.time.Instant
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
                    val availabilityChecker = remember {
                        HealthConnectAvailabilityChecker(applicationContext)
                    }
                    val inspector = remember { HealthConnectWorkoutInspector(applicationContext) }
                    val runtime = remember {
                        val coordinator = Gate1SyncCoordinator(
                            ledgerStore = app.ledgerStore,
                            writer = HealthConnectWorkoutWriter(applicationContext),
                            preflight = HealthConnectSyncPreflight(applicationContext),
                            confirmer = inspector,
                            reconciler = inspector,
                        )
                        Gate1RuntimeDiagnostics(
                            coordinator = coordinator,
                            ledger = app.ledgerStore,
                            inspector = inspector,
                        )
                    }
                    var availability by remember { mutableStateOf(availabilityChecker.check()) }
                    var permission by remember { mutableStateOf(HealthConnectPermission.NOT_REQUESTED) }
                    var diagnostic by remember { mutableStateOf<Gate1Diagnostic?>(null) }
                    var busy by remember { mutableStateOf(false) }

                    fun refreshEnvironment() {
                        availability = availabilityChecker.check()
                        scope.launch {
                            permission = permissionStatus(availability)
                        }
                    }

                    fun perform(action: suspend () -> Gate1Diagnostic) {
                        if (busy) return
                        busy = true
                        scope.launch {
                            try {
                                diagnostic = action()
                                availability = availabilityChecker.check()
                                permission = permissionStatus(availability)
                            } finally {
                                busy = false
                            }
                        }
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = HealthConnectPermissions.requestContract,
                    ) { granted ->
                        permission = if (
                            granted.containsAll(HealthConnectPermissions.exerciseSessionPermissions)
                        ) {
                            HealthConnectPermission.GRANTED
                        } else {
                            HealthConnectPermission.NOT_GRANTED
                        }
                        perform(runtime::refresh)
                    }

                    LaunchedEffect(Unit) {
                        availability = availabilityChecker.check()
                        permission = permissionStatus(availability)
                        diagnostic = runtime.refresh()
                    }

                    Gate1DiagnosticsScreen(
                        availability = availability,
                        permission = permission,
                        diagnostic = diagnostic,
                        busy = busy,
                        onCheckAvailability = ::refreshEnvironment,
                        onRequestPermission = {
                            permissionLauncher.launch(
                                HealthConnectPermissions.exerciseSessionPermissions,
                            )
                        },
                        onRun = { perform(runtime::run) },
                        onConfirm = { perform(runtime::confirm) },
                        onReconcile = { perform(runtime::reconcile) },
                        onRefresh = { perform(runtime::refresh) },
                        onShare = { current ->
                            diagnostic = shareReport(current, availability, permission)
                        },
                    )
                }
            }
        }
    }

    private suspend fun permissionStatus(
        availability: HealthConnectAvailability,
    ): HealthConnectPermission {
        if (availability != HealthConnectAvailability.Available) {
            return HealthConnectPermission.NOT_REQUESTED
        }
        return try {
            val client = HealthConnectClient.getOrCreate(applicationContext)
            if (HealthConnectPermissions.missingPermissions(client).isEmpty()) {
                HealthConnectPermission.GRANTED
            } else {
                HealthConnectPermission.NOT_GRANTED
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            HealthConnectPermission.NOT_GRANTED
        }
    }

    private fun shareReport(
        diagnostic: Gate1Diagnostic,
        availability: HealthConnectAvailability,
        permission: HealthConnectPermission,
    ): Gate1Diagnostic = try {
        val report = Gate1Diagnostics.export(
            Gate1ExportInput(
                appVersion = appVersion(),
                buildType = if (
                    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                ) "debug" else "release",
                generatedAt = Instant.now(),
                availability = availability.toDiagnosticAvailability(),
                permission = permission,
                diagnostic = diagnostic,
            ),
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(sendIntent, "Share sanitized Gate 1 diagnostics"))
        diagnostic
    } catch (_: Exception) {
        diagnostic.copy(code = "DIAGNOSTICS_SHARE_FAILED")
    }

    private fun appVersion(): String {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        return packageInfo.versionName ?: "unknown"
    }
}

@androidx.compose.runtime.Composable
private fun Gate1DiagnosticsScreen(
    availability: HealthConnectAvailability,
    permission: HealthConnectPermission,
    diagnostic: Gate1Diagnostic?,
    busy: Boolean,
    onCheckAvailability: () -> Unit,
    onRequestPermission: () -> Unit,
    onRun: () -> Unit,
    onConfirm: () -> Unit,
    onReconcile: () -> Unit,
    onRefresh: () -> Unit,
    onShare: (Gate1Diagnostic) -> Unit,
) {
    val nextAction = diagnostic?.nextAction
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Huawei Sync - Gate 1", style = MaterialTheme.typography.headlineSmall)
        Text("Health Connect availability: $availability")
        Text("Exercise session permission: $permission")
        if (diagnostic == null) {
            Text("Loading privacy-safe diagnostics...")
        } else {
            Text("Status: ${diagnostic.statusCode}")
            Text(diagnostic.safeMessage)
            Text("Next action: ${diagnostic.nextAction}")
            Text("Code: ${diagnostic.code ?: "unknown"}")
            Text("Room rows: ${diagnostic.roomRowCount}")
            Text("Write attempts: ${diagnostic.writeAttemptCount}")
            Text("Client record version: ${diagnostic.clientRecordVersion}")
            Text("Health Connect matches: ${diagnostic.healthConnectMatchCount ?: "unknown"}")
            Text(
                "Expected-version matches: " +
                    (diagnostic.healthConnectExpectedVersionMatchCount ?: "unknown"),
            )
            Text("Exact version match: ${diagnostic.versionMatch ?: "unknown"}")
            Text("Gate 1 device evidence: BLOCKED until the runtime procedure is recorded.")
        }
        Button(onClick = onCheckAvailability, enabled = !busy) {
            Text("Check Health Connect availability")
        }
        Button(
            enabled = !busy && availability == HealthConnectAvailability.Available,
            onClick = onRequestPermission,
        ) {
            Text("Request ExerciseSessionRecord permissions")
        }
        Button(
            enabled = !busy && availability == HealthConnectAvailability.Available &&
                nextAction in setOf(DiagnosticNextAction.RUN_SYNC, DiagnosticNextAction.RETRY_SYNC),
            onClick = onRun,
        ) {
            Text("Run synthetic strength sync")
        }
        Button(
            enabled = !busy && availability == HealthConnectAvailability.Available &&
                nextAction == DiagnosticNextAction.CONFIRM,
            onClick = onConfirm,
        ) {
            Text("Confirm Health Connect record")
        }
        Button(
            enabled = !busy && availability == HealthConnectAvailability.Available &&
                nextAction == DiagnosticNextAction.RECONCILE,
            onClick = onReconcile,
        ) {
            Text("Reconcile Room and Health Connect")
        }
        Button(onClick = onRefresh, enabled = !busy) {
            Text("Refresh Room and Health Connect counts")
        }
        Button(
            onClick = { diagnostic?.let(onShare) },
            enabled = !busy && diagnostic != null,
        ) {
            Text("Share sanitized diagnostics")
        }
    }
}

private fun HealthConnectAvailability.toDiagnosticAvailability(): DiagnosticAvailability = when (this) {
    HealthConnectAvailability.Available -> DiagnosticAvailability.AVAILABLE
    HealthConnectAvailability.ProviderUpdateRequired -> DiagnosticAvailability.UPDATE_REQUIRED
    HealthConnectAvailability.Unavailable -> DiagnosticAvailability.UNAVAILABLE
}
