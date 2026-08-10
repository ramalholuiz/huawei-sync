package dev.lui.huaweisync.ui

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial refresh composes environment diagnostics and durable ledger facts`() = runTest(dispatcher) {
        val runtime = FakeRuntime(refreshDiagnostic = diagnostic())
        val viewModel = viewModel(runtime = runtime, ledgerEntry = ledger())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.busy)
        assertEquals(HealthConnectAvailability.Available, state.availability)
        assertEquals(HealthConnectPermission.GRANTED, state.permission)
        assertEquals(ProductHealthConnectStatus.READY_TO_SYNC, state.productSyncState?.healthConnectStatus)
        assertEquals(1, state.productSyncState?.ledgerWorkoutCount)
        assertEquals(4, state.productSyncState?.attemptCount)
        assertEquals(1, runtime.refreshCalls)
        assertNull(state.controllerFailureCode)
    }

    @Test
    fun `sync requests permission without entering runtime when permission is missing`() = runTest(dispatcher) {
        val runtime = FakeRuntime(refreshDiagnostic = diagnostic(nextAction = DiagnosticNextAction.REQUEST_PERMISSION))
        val viewModel = viewModel(runtime = runtime, permission = HealthConnectPermission.NOT_GRANTED)
        advanceUntilIdle()
        val effect = backgroundScope.async { viewModel.effects.first() }
        runCurrent()

        viewModel.onSyncRequested()

        assertEquals(MainUiEffect.RequestHealthConnectPermissions, effect.await())
        assertEquals(0, runtime.runCalls)
        assertEquals(0, runtime.confirmCalls)
        assertEquals(0, runtime.reconcileCalls)
    }

    @Test
    fun `ready sync action enters only existing run path`() = runTest(dispatcher) {
        val runtime = FakeRuntime(refreshDiagnostic = diagnostic())
        val viewModel = viewModel(runtime = runtime)
        advanceUntilIdle()

        viewModel.onSyncRequested()
        advanceUntilIdle()

        assertEquals(1, runtime.runCalls)
        assertEquals(0, runtime.confirmCalls)
        assertEquals(0, runtime.reconcileCalls)
    }

    @Test
    fun `accepted outcome routes next action to confirm and refreshes truthful state`() = runTest(dispatcher) {
        val runtime = FakeRuntime(
            refreshDiagnostic = diagnostic(
                nextAction = DiagnosticNextAction.CONFIRM,
                evidence = DiagnosticEvidence.NEEDS_CONFIRMATION,
                durableStatus = SyncStatus.VERIFICATION_PENDING,
            ),
            confirmDiagnostic = diagnostic(
                nextAction = DiagnosticNextAction.NONE,
                evidence = DiagnosticEvidence.VERIFIED,
                durableStatus = SyncStatus.VERIFIED,
                matches = 1,
                expectedMatches = 1,
                versionMatch = true,
            ),
        )
        val viewModel = viewModel(runtime = runtime)
        advanceUntilIdle()

        viewModel.onSyncRequested()
        advanceUntilIdle()

        assertEquals(0, runtime.runCalls)
        assertEquals(1, runtime.confirmCalls)
        assertEquals(0, runtime.reconcileCalls)
        assertEquals(
            ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
            viewModel.uiState.value.productSyncState?.healthConnectStatus,
        )
        assertFalse(viewModel.uiState.value.busy)
    }

    @Test
    fun `reconciliation-required outcome uses only existing reconcile runtime path`() = runTest(dispatcher) {
        val runtime = FakeRuntime(
            refreshDiagnostic = diagnostic(
                nextAction = DiagnosticNextAction.RECONCILE,
                evidence = DiagnosticEvidence.NEEDS_RECONCILIATION,
                durableStatus = SyncStatus.RECONCILIATION_PENDING,
            ),
            reconcileDiagnostic = diagnostic(
                nextAction = DiagnosticNextAction.CONFIRM,
                evidence = DiagnosticEvidence.NEEDS_CONFIRMATION,
                durableStatus = SyncStatus.VERIFICATION_PENDING,
            ),
        )
        val viewModel = viewModel(runtime = runtime)
        advanceUntilIdle()

        viewModel.onSyncRequested()
        advanceUntilIdle()

        assertEquals(0, runtime.runCalls)
        assertEquals(0, runtime.confirmCalls)
        assertEquals(1, runtime.reconcileCalls)
    }

    @Test
    fun `controller failures expose only stable sanitized code`() = runTest(dispatcher) {
        val secret = "private-provider-payload"
        val runtime = FakeRuntime(refreshDiagnostic = diagnostic())
        val viewModel = viewModel(
            runtime = runtime,
            availability = { throw IllegalStateException(secret) },
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("SYNC_STATE_REFRESH_FAILED", state.controllerFailureCode)
        assertFalse(state.toString().contains(secret))
        assertFalse(state.busy)
        assertEquals(2, runtime.refreshCalls)
    }

    private fun viewModel(
        runtime: FakeRuntime,
        permission: HealthConnectPermission = HealthConnectPermission.GRANTED,
        availability: () -> HealthConnectAvailability = { HealthConnectAvailability.Available },
        ledgerEntry: SyncLedgerEntry? = null,
    ) = MainViewModel(
        runtime = runtime,
        checkAvailability = availability,
        checkPermission = { permission },
        findLedgerEntry = { ledgerEntry },
    )

    private fun diagnostic(
        nextAction: DiagnosticNextAction = DiagnosticNextAction.RUN_SYNC,
        evidence: DiagnosticEvidence = DiagnosticEvidence.READY,
        durableStatus: SyncStatus? = null,
        matches: Int? = 0,
        expectedMatches: Int? = 0,
        versionMatch: Boolean? = false,
    ) = Gate1Diagnostic(
        statusCode = "GATE1_READY",
        safeMessage = "Gate 1 state is ready.",
        nextAction = nextAction,
        evidence = evidence,
        durableStatus = durableStatus,
        phase = null,
        code = null,
        roomRowCount = if (durableStatus == null) 0 else 1,
        writeAttemptCount = 0,
        clientRecordVersion = 1,
        healthConnectMatchCount = matches,
        healthConnectExpectedVersionMatchCount = expectedMatches,
        versionMatch = versionMatch,
        localFinalization = null,
    )

    private fun ledger() = SyncLedgerEntry(
        clientRecordId = "client-id",
        sourceProvider = "synthetic",
        sourceRecordId = "source-id",
        sourceVersion = "1",
        contentHash = "a".repeat(64),
        clientRecordVersion = 1,
        healthConnectRecordId = null,
        status = SyncStatus.PENDING,
        attemptCount = 4,
        acceptedAtEpochMillis = null,
        confirmedAtEpochMillis = null,
        createdAtEpochMillis = 100,
        updatedAtEpochMillis = 400,
        lastErrorCode = null,
        lastErrorPhase = null,
        lastErrorAtEpochMillis = null,
        lastErrorMessage = null,
    )

    private class FakeRuntime(
        private val refreshDiagnostic: Gate1Diagnostic,
        private val runDiagnostic: Gate1Diagnostic = refreshDiagnostic,
        private val confirmDiagnostic: Gate1Diagnostic = refreshDiagnostic,
        private val reconcileDiagnostic: Gate1Diagnostic = refreshDiagnostic,
    ) : MainSyncRuntime {
        var refreshCalls = 0
        var runCalls = 0
        var confirmCalls = 0
        var reconcileCalls = 0

        override suspend fun run(): Gate1Diagnostic {
            runCalls += 1
            return runDiagnostic
        }

        override suspend fun confirm(): Gate1Diagnostic {
            confirmCalls += 1
            return confirmDiagnostic
        }

        override suspend fun reconcile(): Gate1Diagnostic {
            reconcileCalls += 1
            return reconcileDiagnostic
        }

        override suspend fun refresh(): Gate1Diagnostic {
            refreshCalls += 1
            return refreshDiagnostic
        }
    }
}
