package dev.lui.huaweisync.health

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord

fun interface HealthWorkoutWriter {
    suspend fun write(record: ExerciseSessionRecord): String?
}

class HealthConnectWorkoutWriter(
    private val client: HealthConnectClient,
) : HealthWorkoutWriter {
    override suspend fun write(record: ExerciseSessionRecord): String? {
        val response = client.insertRecords(listOf(record))
        return response.recordIdsList.firstOrNull()
    }
}
