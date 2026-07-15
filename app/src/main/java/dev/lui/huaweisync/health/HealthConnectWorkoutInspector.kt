package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailure
import dev.lui.huaweisync.data.SyncFailureDisposition
import java.time.Instant
import kotlinx.coroutines.CancellationException

/** Privacy-safe input for an exact-identity inspection within fixed workout bounds. */
data class HealthWorkoutInspectionRequest(
    val clientRecordId: String,
    val clientRecordVersion: Long,
    val startTime: Instant,
    val endTime: Instant,
) {
    init {
        requireValidInspectionIdentityAndBounds(
            clientRecordId,
            clientRecordVersion,
            startTime,
            endTime,
        )
    }
}

/**
 * Low-cardinality inspection facts. No health payload or deterministic client ID is exposed;
 * an external ID is retained only when reconciliation has exactly one unambiguous match.
 */
data class HealthWorkoutInspection(
    val matchingRecordCount: Int,
    val expectedVersionMatchCount: Int,
    val externalRecordId: String?,
) {
    init {
        require(matchingRecordCount >= 0)
        require(expectedVersionMatchCount in 0..matchingRecordCount)
        require(externalRecordId == null || externalRecordId.isNotBlank())
        require(matchingRecordCount == 1 || externalRecordId == null) {
            "An external record ID is only unambiguous for exactly one match."
        }
    }
}

sealed interface HealthWorkoutInspectionResult {
    data class Complete(val inspection: HealthWorkoutInspection) : HealthWorkoutInspectionResult

    /** Stable code only; provider exceptions and record payloads never cross this boundary. */
    data class Inconclusive(val code: String) : HealthWorkoutInspectionResult {
        init {
            require(code.matches(Regex("[A-Z][A-Z0-9_]{0,63}")))
        }
    }
}

fun interface HealthWorkoutInspector {
    suspend fun inspect(request: HealthWorkoutInspectionRequest): HealthWorkoutInspectionResult
}

/** SDK-independent page seam used to exercise pagination and error behavior in local tests. */
internal data class HealthWorkoutInspectionPage(
    val records: List<HealthWorkoutInspectionRecord>,
    val nextPageToken: String?,
)

internal data class HealthWorkoutInspectionRecord(
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val externalRecordId: String,
)

/**
 * Reads every official Health Connect page inside explicit workout bounds and then filters the
 * exact deterministic client record ID. Only a complete bounded query can report absence.
 */
class HealthConnectWorkoutInspector internal constructor(
    private val readPage: suspend (HealthWorkoutInspectionRequest, String?) ->
        HealthWorkoutInspectionPage,
) : HealthWorkoutInspector, HealthWorkoutConfirmer, HealthWorkoutReconciler {
    constructor(client: HealthConnectClient) : this(
        readPage = { request, pageToken -> client.readInspectionPage(request, pageToken) },
    )

    /** Defers provider creation until the user explicitly invokes an inspection action. */
    constructor(context: Context) : this(
        lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HealthConnectClient.getOrCreate(context.applicationContext)
        },
    )

    private constructor(client: Lazy<HealthConnectClient>) : this(
        readPage = { request, pageToken ->
            client.value.readInspectionPage(request, pageToken)
        },
    )

    override suspend fun inspect(
        request: HealthWorkoutInspectionRequest,
    ): HealthWorkoutInspectionResult = try {
        inspectAllPages(request)
    } catch (failure: CancellationException) {
        throw failure
    } catch (failure: SecurityException) {
        HealthWorkoutInspectionResult.Inconclusive(INSPECTION_NOT_AUTHORIZED)
    } catch (failure: Exception) {
        HealthWorkoutInspectionResult.Inconclusive(INSPECTION_FAILED)
    }

    override suspend fun confirm(request: HealthConfirmationRequest): HealthConfirmationResult =
        when (val result = inspect(request.toInspectionRequest())) {
            is HealthWorkoutInspectionResult.Inconclusive -> HealthConfirmationResult.Failed(
                inspectionFailure(result.code, SyncErrorPhase.VERIFICATION),
            )
            is HealthWorkoutInspectionResult.Complete -> result.inspection.toConfirmationResult()
        }

    override suspend fun reconcile(
        request: HealthReconciliationRequest,
    ): HealthReconciliationResult = when (val result = inspect(request.toInspectionRequest())) {
        is HealthWorkoutInspectionResult.Inconclusive -> HealthReconciliationResult.Failed(
            inspectionFailure(result.code, SyncErrorPhase.RECONCILIATION),
        )
        is HealthWorkoutInspectionResult.Complete -> result.inspection.toReconciliationResult()
    }

    private suspend fun inspectAllPages(
        request: HealthWorkoutInspectionRequest,
    ): HealthWorkoutInspectionResult {
        var pageToken: String? = null
        val seenPageTokens = mutableSetOf<String>()
        var matchingRecordCount = 0
        var expectedVersionMatchCount = 0
        var singleExternalRecordId: String? = null

        do {
            val page = readPage(request, pageToken)
            page.records.forEach { record ->
                if (record.clientRecordId == request.clientRecordId) {
                    matchingRecordCount = Math.addExact(matchingRecordCount, 1)
                    if (record.clientRecordVersion == request.clientRecordVersion) {
                        expectedVersionMatchCount = Math.addExact(expectedVersionMatchCount, 1)
                    }
                    singleExternalRecordId = if (matchingRecordCount == 1) {
                        record.externalRecordId
                    } else {
                        null
                    }
                }
            }

            pageToken = page.nextPageToken
            if (
                pageToken != null &&
                (pageToken.isBlank() || !seenPageTokens.add(pageToken))
            ) {
                return HealthWorkoutInspectionResult.Inconclusive(PAGINATION_INVALID)
            }
        } while (pageToken != null)

        return HealthWorkoutInspectionResult.Complete(
            HealthWorkoutInspection(
                matchingRecordCount = matchingRecordCount,
                expectedVersionMatchCount = expectedVersionMatchCount,
                externalRecordId = singleExternalRecordId,
            ),
        )
    }

    private fun HealthWorkoutInspection.toConfirmationResult(): HealthConfirmationResult = when {
        matchingRecordCount == 0 -> HealthConfirmationResult.Absent(RECORD_NOT_FOUND)
        matchingRecordCount > 1 -> HealthConfirmationResult.Inconclusive(MULTIPLE_RECORDS_FOUND)
        expectedVersionMatchCount != 1 ->
            HealthConfirmationResult.Inconclusive(RECORD_VERSION_MISMATCH)
        else -> HealthConfirmationResult.Confirmed
    }

    private fun HealthWorkoutInspection.toReconciliationResult(): HealthReconciliationResult = when {
        matchingRecordCount == 0 -> HealthReconciliationResult.AuthoritativelyAbsent(RECORD_NOT_FOUND)
        matchingRecordCount > 1 -> HealthReconciliationResult.Inconclusive(MULTIPLE_RECORDS_FOUND)
        expectedVersionMatchCount != 1 ->
            HealthReconciliationResult.Inconclusive(RECORD_VERSION_MISMATCH)
        else -> HealthReconciliationResult.Found(checkNotNull(externalRecordId))
    }

    private fun inspectionFailure(code: String, phase: SyncErrorPhase) = SyncFailure(
        disposition = SyncFailureDisposition.RETRYABLE,
        code = code,
        phase = phase,
        safeMessage = SyncDiagnosticMessage.VERIFICATION_FAILED,
    )

    private companion object {
        const val INSPECTION_FAILED = "HEALTH_CONNECT_INSPECTION_FAILED"
        const val INSPECTION_NOT_AUTHORIZED = "HEALTH_CONNECT_INSPECTION_NOT_AUTHORIZED"
        const val PAGINATION_INVALID = "HEALTH_CONNECT_PAGINATION_INVALID"
        const val RECORD_NOT_FOUND = "HEALTH_RECORD_NOT_FOUND"
        const val RECORD_VERSION_MISMATCH = "HEALTH_RECORD_VERSION_MISMATCH"
        const val MULTIPLE_RECORDS_FOUND = "MULTIPLE_HEALTH_RECORDS_FOUND"
    }
}

private const val INSPECTION_PAGE_SIZE = 1000

private suspend fun HealthConnectClient.readInspectionPage(
    request: HealthWorkoutInspectionRequest,
    pageToken: String?,
): HealthWorkoutInspectionPage {
    val response = readRecords(
        ReadRecordsRequest<ExerciseSessionRecord>(
            timeRangeFilter = TimeRangeFilter.between(request.startTime, request.endTime),
            ascendingOrder = true,
            pageSize = INSPECTION_PAGE_SIZE,
            pageToken = pageToken,
        ),
    )
    return HealthWorkoutInspectionPage(
        records = response.records.map { record ->
            HealthWorkoutInspectionRecord(
                clientRecordId = record.metadata.clientRecordId,
                clientRecordVersion = record.metadata.clientRecordVersion,
                externalRecordId = record.metadata.id,
            )
        },
        nextPageToken = response.pageToken,
    )
}

private fun HealthConfirmationRequest.toInspectionRequest() = HealthWorkoutInspectionRequest(
    clientRecordId = clientRecordId,
    clientRecordVersion = clientRecordVersion,
    startTime = startTime,
    endTime = endTime,
)

private fun HealthReconciliationRequest.toInspectionRequest() = HealthWorkoutInspectionRequest(
    clientRecordId = clientRecordId,
    clientRecordVersion = clientRecordVersion,
    startTime = startTime,
    endTime = endTime,
)
