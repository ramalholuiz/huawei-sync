package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailure
import dev.lui.huaweisync.data.SyncFailureDisposition
import kotlinx.coroutines.CancellationException

sealed interface HealthWriteResult {
    data class Accepted(val externalRecordId: String?) : HealthWriteResult

    data class Failed(val failure: SyncFailure) : HealthWriteResult
}

fun interface HealthWorkoutWriter {
    suspend fun write(record: ExerciseSessionRecord): HealthWriteResult
}

class HealthConnectWorkoutWriter internal constructor(
    private val insertRecord: suspend (ExerciseSessionRecord) -> String?,
) : HealthWorkoutWriter {
    constructor(client: HealthConnectClient) : this(
        insertRecord = { record ->
            client.insertRecords(listOf(record)).recordIdsList.firstOrNull()
        },
    )

    /** Defers client creation until after the coordinator's preflight has reported ready. */
    constructor(context: Context) : this(
        insertRecord = { record ->
            HealthConnectClient.getOrCreate(context)
                .insertRecords(listOf(record))
                .recordIdsList
                .firstOrNull()
        },
    )

    override suspend fun write(record: ExerciseSessionRecord): HealthWriteResult = try {
        HealthWriteResult.Accepted(insertRecord(record))
    } catch (failure: CancellationException) {
        throw failure
    } catch (failure: SecurityException) {
        HealthWriteResult.Failed(
            permanentFailure(code = "HEALTH_CONNECT_WRITE_NOT_AUTHORIZED"),
        )
    } catch (failure: IllegalArgumentException) {
        HealthWriteResult.Failed(
            permanentFailure(code = "INVALID_EXERCISE_RECORD"),
        )
    } catch (failure: Exception) {
        HealthWriteResult.Failed(
            SyncFailure(
                disposition = SyncFailureDisposition.RETRYABLE,
                code = "HEALTH_CONNECT_WRITE_FAILED",
                phase = SyncErrorPhase.WRITE,
                safeMessage = SyncDiagnosticMessage.WRITE_FAILED,
            ),
        )
    }

    private fun permanentFailure(code: String) = SyncFailure(
        disposition = SyncFailureDisposition.PERMANENT,
        code = code,
        phase = SyncErrorPhase.WRITE,
        safeMessage = SyncDiagnosticMessage.WRITE_FAILED,
    )
}
