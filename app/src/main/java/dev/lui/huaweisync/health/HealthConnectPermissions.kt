package dev.lui.huaweisync.health

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord

object HealthConnectPermissions {
    val exerciseSessionPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    )

    val requestContract = PermissionController.createRequestPermissionResultContract()

    suspend fun missingPermissions(client: HealthConnectClient): Set<String> {
        val granted = client.permissionController.getGrantedPermissions()
        return exerciseSessionPermissions - granted
    }
}
