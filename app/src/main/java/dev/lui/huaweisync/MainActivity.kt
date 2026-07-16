package dev.lui.huaweisync

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.Gate1Diagnostics
import dev.lui.huaweisync.diagnostics.Gate1ExportInput
import dev.lui.huaweisync.diagnostics.Gate1RuntimeDiagnostics
import dev.lui.huaweisync.diagnostics.HealthConnectAvailability as DiagnosticAvailability
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.health.Gate1SyncCoordinator
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.health.HealthConnectAvailabilityChecker
import dev.lui.huaweisync.health.HealthConnectPermissions
import dev.lui.huaweisync.health.HealthConnectSyncPreflight
import dev.lui.huaweisync.health.HealthConnectWorkoutInspector
import dev.lui.huaweisync.health.HealthConnectWorkoutWriter
import dev.lui.huaweisync.ui.Gate1MainSyncRuntime
import dev.lui.huaweisync.ui.HuaweiSyncRoot
import dev.lui.huaweisync.ui.MainUiEffect
import dev.lui.huaweisync.ui.MainViewModel
import dev.lui.huaweisync.ui.screens.diagnostics.DiagnosticsScreen
import java.time.Instant
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as HuaweiSyncApp
        val availabilityChecker = HealthConnectAvailabilityChecker(applicationContext)
        val inspector = HealthConnectWorkoutInspector(applicationContext)
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = app.ledgerStore,
            writer = HealthConnectWorkoutWriter(applicationContext),
            preflight = HealthConnectSyncPreflight(applicationContext),
            confirmer = inspector,
            reconciler = inspector,
        )
        val runtime = Gate1RuntimeDiagnostics(
            coordinator = coordinator,
            ledger = app.ledgerStore,
            inspector = inspector,
        )
        val syntheticWorkout = SyntheticWorkoutFactory.create()
        val viewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(
                runtime = Gate1MainSyncRuntime(runtime),
                checkAvailability = availabilityChecker::check,
                checkPermission = ::permissionStatus,
                findLedgerEntry = {
                    app.ledgerStore.findBySource(
                        syntheticWorkout.source.stableName,
                        syntheticWorkout.sourceWorkoutId,
                    )
                },
                listLedgerHistory = app.ledgerStore::listHistory,
            ),
        )[MainViewModel::class.java]

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = HealthConnectPermissions.requestContract,
            ) {
                viewModel.onPermissionsResult()
            }
            LaunchedEffect(viewModel) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        MainUiEffect.RequestHealthConnectPermissions -> permissionLauncher.launch(
                            HealthConnectPermissions.exerciseSessionPermissions,
                        )
                    }
                }
            }

            HuaweiSyncRoot(
                productSyncState = state.productSyncState,
                historyState = state.historyState,
                syncInProgress = state.busy,
                onHistoryRetry = viewModel::refresh,
                onSync = viewModel::onSyncRequested,
                gate1Entry = {
                    val availability = state.availability ?: HealthConnectAvailability.Unavailable
                    DiagnosticsScreen(
                        availability = availability,
                        permission = state.permission,
                        diagnostic = state.diagnostic,
                        busy = state.busy,
                        onCheckAvailability = viewModel::refresh,
                        onRequestPermission = viewModel::requestHealthConnectPermissions,
                        onRun = viewModel::runSync,
                        onConfirm = viewModel::confirm,
                        onReconcile = viewModel::reconcile,
                        onRefresh = viewModel::refresh,
                        onExport = { current ->
                            shareReport(current, availability, state.permission)
                        },
                    )
                },
            )
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

/** Allows the explicit third idempotency run only after authoritative verification. */
internal fun canRunGate1Sync(
    nextAction: DiagnosticNextAction?,
    durableStatus: SyncStatus?,
): Boolean = when (nextAction) {
    DiagnosticNextAction.RUN_SYNC,
    DiagnosticNextAction.RETRY_SYNC,
    -> true
    DiagnosticNextAction.NONE -> durableStatus == SyncStatus.VERIFIED
    else -> false
}

private fun HealthConnectAvailability.toDiagnosticAvailability(): DiagnosticAvailability = when (this) {
    HealthConnectAvailability.Available -> DiagnosticAvailability.AVAILABLE
    HealthConnectAvailability.ProviderUpdateRequired -> DiagnosticAvailability.UPDATE_REQUIRED
    HealthConnectAvailability.Unavailable -> DiagnosticAvailability.UNAVAILABLE
}
