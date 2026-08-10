package dev.lui.huaweisync

import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gate1DiagnosticsScreenPolicyTest {
    @Test
    fun `verified state permits an explicit idempotency rerun`() {
        assertTrue(canRunGate1Sync(DiagnosticNextAction.NONE, SyncStatus.VERIFIED))
    }

    @Test
    fun `normal run and retry actions remain enabled`() {
        assertTrue(canRunGate1Sync(DiagnosticNextAction.RUN_SYNC, SyncStatus.PENDING))
        assertTrue(canRunGate1Sync(DiagnosticNextAction.RETRY_SYNC, SyncStatus.RETRYABLE_ERROR))
    }

    @Test
    fun `all other states remain closed`() {
        assertFalse(canRunGate1Sync(DiagnosticNextAction.NONE, SyncStatus.SYNCED))
        assertFalse(canRunGate1Sync(DiagnosticNextAction.CONFIRM, SyncStatus.VERIFICATION_PENDING))
        assertFalse(canRunGate1Sync(DiagnosticNextAction.RECONCILE, SyncStatus.RECONCILIATION_PENDING))
        assertFalse(canRunGate1Sync(DiagnosticNextAction.REQUEST_PERMISSION, SyncStatus.PERMISSION_BLOCKED))
        assertFalse(canRunGate1Sync(null, null))
    }
}
