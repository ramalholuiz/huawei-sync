package dev.lui.huaweisync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.Gate1RuntimeDiagnostics
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.ui.state.HistoryState
import dev.lui.huaweisync.ui.state.HistoryStateMapper
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductSyncStateMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATE_REFRESH_FAILED = "SYNC_STATE_REFRESH_FAILED"

data class MainUiState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val availability: HealthConnectAvailability? = null,
    val permission: HealthConnectPermission = HealthConnectPermission.NOT_REQUESTED,
    val diagnostic: Gate1Diagnostic? = null,
    val productSyncState: ProductSyncState? = null,
    val historyState: HistoryState = HistoryState.Loading,
    val controllerFailureCode: String? = null,
)

sealed interface MainUiEffect {
    data object RequestHealthConnectPermissions : MainUiEffect
}

internal interface MainSyncRuntime {
    suspend fun run(): Gate1Diagnostic
    suspend fun confirm(): Gate1Diagnostic
    suspend fun reconcile(): Gate1Diagnostic
    suspend fun refresh(): Gate1Diagnostic
}

internal class Gate1MainSyncRuntime(
    private val runtime: Gate1RuntimeDiagnostics,
) : MainSyncRuntime {
    override suspend fun run(): Gate1Diagnostic = runtime.run()
    override suspend fun confirm(): Gate1Diagnostic = runtime.confirm()
    override suspend fun reconcile(): Gate1Diagnostic = runtime.reconcile()
    override suspend fun refresh(): Gate1Diagnostic = runtime.refresh()
}

class MainViewModel internal constructor(
    private val runtime: MainSyncRuntime,
    private val checkAvailability: () -> HealthConnectAvailability,
    private val checkPermission: suspend (HealthConnectAvailability) -> HealthConnectPermission,
    private val findLedgerEntry: suspend () -> SyncLedgerEntry?,
    private val listLedgerHistory: suspend () -> List<SyncLedgerEntry> = {
        listOfNotNull(findLedgerEntry())
    },
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<MainUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<MainUiEffect> = _effects.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() = launchExclusive {
        refreshFacts(runtime.refresh())
    }

    fun requestHealthConnectPermissions() {
        if (_uiState.value.busy) return
        _effects.tryEmit(MainUiEffect.RequestHealthConnectPermissions)
    }

    fun onPermissionsResult() {
        refresh()
    }

    fun onSyncRequested() {
        val state = _uiState.value
        if (state.busy) return
        if (state.availability != HealthConnectAvailability.Available) {
            refresh()
            return
        }
        if (state.permission != HealthConnectPermission.GRANTED) {
            requestHealthConnectPermissions()
            return
        }

        when {
            state.diagnostic?.nextAction == DiagnosticNextAction.RECONCILE ||
                state.productSyncState?.healthConnectStatus ==
                ProductHealthConnectStatus.RECONCILIATION_REQUIRED -> reconcile()
            state.diagnostic?.nextAction == DiagnosticNextAction.CONFIRM ||
                state.productSyncState?.healthConnectStatus ==
                ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK -> confirm()
            else -> runSync()
        }
    }

    fun runSync() = launchExclusive { refreshFacts(runtime.run()) }

    fun confirm() = launchExclusive { refreshFacts(runtime.confirm()) }

    fun reconcile() = launchExclusive { refreshFacts(runtime.reconcile()) }

    private fun launchExclusive(action: suspend () -> Unit) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, controllerFailureCode = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                recoverAfterControllerFailure()
            } finally {
                _uiState.update { it.copy(loading = false, busy = false) }
            }
        }
    }

    private suspend fun recoverAfterControllerFailure() {
        val recovered = try {
            refreshFacts(runtime.refresh())
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
        _uiState.update {
            it.copy(
                loading = !recovered && it.productSyncState == null,
                controllerFailureCode = STATE_REFRESH_FAILED,
            )
        }
    }

    private suspend fun refreshFacts(diagnostic: Gate1Diagnostic) {
        val availability = checkAvailability()
        val permission = checkPermission(availability)
        val ledgerEntries = listOfNotNull(findLedgerEntry())
        val historyState = try {
            HistoryStateMapper.from(listLedgerHistory())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            HistoryState.RetryableError()
        }
        val productState = ProductSyncStateMapper.from(
            availability = availability,
            permission = permission,
            diagnostic = diagnostic,
            ledgerEntries = ledgerEntries,
        )
        _uiState.value = MainUiState(
            loading = false,
            busy = true,
            availability = availability,
            permission = permission,
            diagnostic = diagnostic,
            productSyncState = productState,
            historyState = historyState,
        )
    }

    internal class Factory(
        private val runtime: MainSyncRuntime,
        private val checkAvailability: () -> HealthConnectAvailability,
        private val checkPermission: suspend (HealthConnectAvailability) -> HealthConnectPermission,
        private val findLedgerEntry: suspend () -> SyncLedgerEntry?,
        private val listLedgerHistory: suspend () -> List<SyncLedgerEntry> = {
            listOfNotNull(findLedgerEntry())
        },
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(
                runtime = runtime,
                checkAvailability = checkAvailability,
                checkPermission = checkPermission,
                findLedgerEntry = findLedgerEntry,
                listLedgerHistory = listLedgerHistory,
            ) as T
        }
    }
}
