package dev.lui.huaweisync.ui.screens.history

import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.ui.state.ActivityReadbackState
import dev.lui.huaweisync.ui.state.HistoryState
import dev.lui.huaweisync.ui.state.HistoryStateMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class HistoryStateMapperTest {
    @Test
    fun `empty ledger maps to honest empty state`() {
        assertSame(HistoryState.Empty, HistoryStateMapper.from(emptyList()))
    }

    @Test
    fun `ledger facts map all readback and recovery states without inference`() {
        val entries = listOf(
            entry("verified", SyncStatus.VERIFIED, acceptedAt = 10, confirmedAt = 20),
            entry("pending", SyncStatus.VERIFICATION_PENDING, acceptedAt = 10),
            entry("reconcile", SyncStatus.RECONCILIATION_PENDING, acceptedAt = 10),
            entry("writing", SyncStatus.WRITING),
            entry("retry", SyncStatus.RETRYABLE_ERROR, errorCode = "HC_RETRYABLE"),
            entry("prepared", SyncStatus.PENDING),
            entry("blocked", SyncStatus.PERMISSION_BLOCKED, errorCode = "PERMISSION_REQUIRED"),
        )

        val content = HistoryStateMapper.from(entries) as HistoryState.Content

        assertEquals(
            listOf(
                ActivityReadbackState.VERIFIED,
                ActivityReadbackState.PENDING_READBACK,
                ActivityReadbackState.RECONCILIATION_REQUIRED,
                ActivityReadbackState.RECONCILIATION_REQUIRED,
                ActivityReadbackState.RETRYABLE_ERROR,
                ActivityReadbackState.PREPARED,
                ActivityReadbackState.ACTION_REQUIRED,
            ),
            content.activities.map { it.readbackState },
        )
        assertEquals(3, content.activities.first().attemptCount)
        assertEquals(10L, content.activities.first().acceptedAtEpochMillis)
        assertEquals(20L, content.activities.first().confirmedAtEpochMillis)
        assertEquals("HC_RETRYABLE", content.activities[4].safeErrorCode)
    }

    @Test
    fun `selection uses exact deterministic client identity only`() {
        val first = entry("client-a", SyncStatus.VERIFIED, sourceRecordId = "same-source")
        val second = entry("client-b", SyncStatus.VERIFIED, sourceRecordId = "same-source")
        val state = HistoryStateMapper.from(listOf(first, second))

        assertEquals("client-b", HistoryStateMapper.select(state, "client-b")?.clientRecordId)
        assertNull(HistoryStateMapper.select(state, "same-source"))
        assertNull(HistoryStateMapper.select(state, "CLIENT-B"))
        assertNull(HistoryStateMapper.select(HistoryState.Loading, "client-b"))
    }

    private fun entry(
        clientRecordId: String,
        status: SyncStatus,
        sourceRecordId: String = "source-$clientRecordId",
        acceptedAt: Long? = null,
        confirmedAt: Long? = null,
        errorCode: String? = null,
    ) = SyncLedgerEntry(
        clientRecordId = clientRecordId,
        sourceProvider = "synthetic",
        sourceRecordId = sourceRecordId,
        sourceVersion = "1",
        contentHash = "a".repeat(64),
        clientRecordVersion = 2,
        healthConnectRecordId = null,
        status = status,
        attemptCount = 3,
        acceptedAtEpochMillis = acceptedAt,
        confirmedAtEpochMillis = confirmedAt,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 30,
        lastErrorCode = errorCode,
        lastErrorPhase = errorCode?.let { SyncErrorPhase.WRITE },
        lastErrorAtEpochMillis = errorCode?.let { 30 },
        lastErrorMessage = null,
    )
}
