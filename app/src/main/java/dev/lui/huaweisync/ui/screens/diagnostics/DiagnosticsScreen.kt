package dev.lui.huaweisync.ui.screens.diagnostics

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Composable
fun DiagnosticsScreen(
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
    onExport: (Gate1Diagnostic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation = diagnostic?.toDiagnosticsPresentation(availability, permission, busy)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.canvasBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(HuaweiSyncSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        item {
            DiagnosticsHeader()
        }
        item {
            EnvironmentSummary(availability, permission)
        }
        if (presentation == null) {
            item {
                LoadingDiagnostics(
                    busy = busy,
                    onCheckAvailability = onCheckAvailability,
                    onRefresh = onRefresh,
                )
            }
        } else {
            item {
                StatusSummary(presentation)
            }
            item {
                FactSection("Record identity", "DETERMINISTIC METADATA", presentation.identityFacts)
            }
            item {
                FactSection("Room ledger", "DURABLE LOCAL EVIDENCE", presentation.ledgerFacts)
            }
            item {
                FactSection("Health Connect readback", "BOUNDED OFFICIAL INSPECTION", presentation.readbackFacts)
            }
            item {
                FactSection("Classification", "PRIVACY-SAFE DIAGNOSTICS", presentation.classificationFacts)
            }
            item {
                DiagnosticsActions(
                    presentation = presentation,
                    onCheckAvailability = onCheckAvailability,
                    onRequestPermission = onRequestPermission,
                    onRun = onRun,
                    onConfirm = onConfirm,
                    onReconcile = onReconcile,
                    onRefresh = onRefresh,
                    onExport = { onExport(diagnostic) },
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
        TechnicalMicrocopy("GATE 1 / RUNTIME EVIDENCE")
        Text(
            text = "Diagnostics",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            color = HuaweiSyncTheme.colors.ink,
        )
        Text(
            text = "One privacy-safe view of Room state, write attempts, and exact Health Connect readback.",
            style = MaterialTheme.typography.bodyLarge,
            color = HuaweiSyncTheme.colors.ink2,
        )
    }
}

@Composable
private fun EnvironmentSummary(
    availability: HealthConnectAvailability,
    permission: HealthConnectPermission,
) {
    ModernistSurface(modifier = Modifier.fillMaxWidth(), backgroundColor = HuaweiSyncTheme.colors.surface2) {
        TechnicalMicrocopy("HEALTH CONNECT ENVIRONMENT")
        FactRow("Availability", availability.name)
        FactRow("Exercise session permission", permission.name)
    }
}

@Composable
private fun LoadingDiagnostics(
    busy: Boolean,
    onCheckAvailability: () -> Unit,
    onRefresh: () -> Unit,
) {
    ModernistSurface(modifier = Modifier.fillMaxWidth()) {
        Text("Loading privacy-safe diagnostics...", color = HuaweiSyncTheme.colors.ink2)
        StraightEdgeButton(
            label = "Check Health Connect",
            onClick = onCheckAvailability,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        StraightEdgeButton(
            label = "Refresh Room and Health Connect counts",
            onClick = onRefresh,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatusSummary(presentation: DiagnosticsPresentation) {
    val evidenceColor = evidenceColor(presentation.evidence)
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = HuaweiSyncTheme.colors.surface1,
        borderColor = evidenceColor,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TechnicalMicrocopy("CURRENT RESULT")
            Text(
                text = presentation.evidence.name,
                modifier = Modifier
                    .border(HuaweiSyncGeometry.borderThin, evidenceColor, RectangleShape)
                    .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs),
                color = evidenceColor,
                style = HuaweiSyncTheme.technicalTypography.microcopy,
            )
        }
        Text(
            text = presentation.statusCode,
            style = MaterialTheme.typography.headlineSmall,
            color = HuaweiSyncTheme.colors.ink,
        )
        Text(
            text = presentation.safeMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = HuaweiSyncTheme.colors.ink2,
        )
        Text(
            text = "Next action: ${presentation.nextAction.name}",
            style = HuaweiSyncTheme.technicalTypography.label,
            color = HuaweiSyncTheme.colors.ink2,
        )
    }
}

@Composable
private fun FactSection(title: String, eyebrow: String, facts: List<DiagnosticFact>) {
    Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
        SectionHeader(title = title, eyebrow = eyebrow)
        ModernistSurface(modifier = Modifier.fillMaxWidth()) {
            facts.forEach { fact -> FactRow(fact.label, fact.value) }
        }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            style = MaterialTheme.typography.bodyMedium,
            color = HuaweiSyncTheme.colors.ink2,
        )
        SelectionContainer(modifier = Modifier.weight(0.58f)) {
            Text(
                text = value,
                style = HuaweiSyncTheme.technicalTypography.label,
                color = HuaweiSyncTheme.colors.ink,
            )
        }
    }
}

@Composable
private fun DiagnosticsActions(
    presentation: DiagnosticsPresentation,
    onCheckAvailability: () -> Unit,
    onRequestPermission: () -> Unit,
    onRun: () -> Unit,
    onConfirm: () -> Unit,
    onReconcile: () -> Unit,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
        SectionHeader(title = "Closed next actions", eyebrow = "MODEL-GATED CONTROLS")
        ModernistSurface(modifier = Modifier.fillMaxWidth()) {
            presentation.primaryAction?.let { action ->
                DiagnosticActionButton(
                    action = action,
                    accent = true,
                    onCheckAvailability = onCheckAvailability,
                    onRequestPermission = onRequestPermission,
                    onRun = onRun,
                    onConfirm = onConfirm,
                    onReconcile = onReconcile,
                    onRefresh = onRefresh,
                    onExport = onExport,
                )
            } ?: Text(
                text = "No recovery action is required by the current diagnostic state.",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyMedium,
            )
            presentation.utilityActions.forEach { action ->
                DiagnosticActionButton(
                    action = action,
                    accent = false,
                    onCheckAvailability = onCheckAvailability,
                    onRequestPermission = onRequestPermission,
                    onRun = onRun,
                    onConfirm = onConfirm,
                    onReconcile = onReconcile,
                    onRefresh = onRefresh,
                    onExport = onExport,
                )
            }
        }
    }
}

@Composable
private fun DiagnosticActionButton(
    action: DiagnosticsActionPresentation,
    accent: Boolean,
    onCheckAvailability: () -> Unit,
    onRequestPermission: () -> Unit,
    onRun: () -> Unit,
    onConfirm: () -> Unit,
    onReconcile: () -> Unit,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
) {
    StraightEdgeButton(
        label = action.label,
        onClick = {
            when (action.action) {
                DiagnosticsAction.CHECK_HEALTH_CONNECT -> onCheckAvailability()
                DiagnosticsAction.REQUEST_PERMISSION -> onRequestPermission()
                DiagnosticsAction.RUN_SYNC -> onRun()
                DiagnosticsAction.CONFIRM -> onConfirm()
                DiagnosticsAction.RECONCILE -> onReconcile()
                DiagnosticsAction.REFRESH -> onRefresh()
                DiagnosticsAction.EXPORT -> onExport()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = action.enabled,
        accent = accent,
    )
}

@Composable
private fun evidenceColor(evidence: DiagnosticEvidence): Color = when (evidence) {
    DiagnosticEvidence.VERIFIED -> HuaweiSyncTheme.colors.ok
    DiagnosticEvidence.READY,
    DiagnosticEvidence.ACCEPTED,
    DiagnosticEvidence.NEEDS_CONFIRMATION,
    -> HuaweiSyncTheme.colors.info
    DiagnosticEvidence.NEEDS_RECONCILIATION,
    DiagnosticEvidence.BLOCKED,
    -> HuaweiSyncTheme.colors.warning
    DiagnosticEvidence.FAILED -> HuaweiSyncTheme.colors.accent
}

@Preview(name = "Diagnostics / Verified", showBackground = true, widthDp = 412, heightDp = 1100)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun VerifiedDiagnosticsPreview() {
    HuaweiSyncTheme {
        DiagnosticsScreen(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            diagnostic = previewDiagnostic(
                statusCode = "VERIFIED",
                safeMessage = "Health Connect contains exactly one matching record at the expected version.",
                nextAction = DiagnosticNextAction.NONE,
                evidence = DiagnosticEvidence.VERIFIED,
                durableStatus = SyncStatus.VERIFIED,
                matches = 1,
                expectedVersionMatches = 1,
                versionMatch = true,
            ),
            busy = false,
            onCheckAvailability = {},
            onRequestPermission = {},
            onRun = {},
            onConfirm = {},
            onReconcile = {},
            onRefresh = {},
            onExport = {},
        )
    }
}

@Preview(
    name = "Diagnostics / Reconciliation",
    showBackground = true,
    widthDp = 412,
    heightDp = 1100,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ReconciliationDiagnosticsPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        DiagnosticsScreen(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            diagnostic = previewDiagnostic(
                statusCode = "WRITE_INTERRUPTED",
                safeMessage = "A write may have been interrupted and must be reconciled.",
                nextAction = DiagnosticNextAction.RECONCILE,
                evidence = DiagnosticEvidence.NEEDS_RECONCILIATION,
                durableStatus = SyncStatus.WRITING,
                matches = null,
                expectedVersionMatches = null,
                versionMatch = null,
            ),
            busy = false,
            onCheckAvailability = {},
            onRequestPermission = {},
            onRun = {},
            onConfirm = {},
            onReconcile = {},
            onRefresh = {},
            onExport = {},
        )
    }
}

@Preview(name = "Diagnostics / Loading", showBackground = true, widthDp = 412, heightDp = 720)
@Composable
private fun LoadingDiagnosticsPreview() {
    HuaweiSyncTheme {
        DiagnosticsScreen(
            availability = HealthConnectAvailability.ProviderUpdateRequired,
            permission = HealthConnectPermission.NOT_REQUESTED,
            diagnostic = null,
            busy = true,
            onCheckAvailability = {},
            onRequestPermission = {},
            onRun = {},
            onConfirm = {},
            onReconcile = {},
            onRefresh = {},
            onExport = {},
        )
    }
}

private fun previewDiagnostic(
    statusCode: String,
    safeMessage: String,
    nextAction: DiagnosticNextAction,
    evidence: DiagnosticEvidence,
    durableStatus: SyncStatus,
    matches: Int?,
    expectedVersionMatches: Int?,
    versionMatch: Boolean?,
) = Gate1Diagnostic(
    statusCode = statusCode,
    safeMessage = safeMessage,
    nextAction = nextAction,
    evidence = evidence,
    durableStatus = durableStatus,
    phase = null,
    code = null,
    roomRowCount = 1,
    writeAttemptCount = 3,
    clientRecordVersion = 1,
    healthConnectMatchCount = matches,
    healthConnectExpectedVersionMatchCount = expectedVersionMatches,
    versionMatch = versionMatch,
    localFinalization = "COMPLETED",
)
