package dev.lui.huaweisync

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HealthConnectPermissionManifestTest {
    @Test
    fun `health connect permission rationale intent resolves inside the app`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE")
            .setPackage(context.packageName)

        val matches = context.packageManager.queryIntentActivities(intent, 0)

        assertTrue(
            "Health Connect requires the app to handle its permission rationale intent",
            matches.any { it.activityInfo.name == "dev.lui.huaweisync.PermissionsRationaleActivity" },
        )
    }

    @Test
    fun `bluetooth sync declares the current Android 12 plus loopback permissions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val permissions = packageInfo.requestedPermissions.orEmpty().toSet()

        assertTrue(permissions.contains("android.permission.BLUETOOTH_SCAN"))
        assertTrue(permissions.contains("android.permission.BLUETOOTH_CONNECT"))
        assertTrue(permissions.contains("android.permission.BLUETOOTH_ADVERTISE"))
        assertTrue(!permissions.contains("android.permission.BLUETOOTH"))
        assertTrue(!permissions.contains("android.permission.BLUETOOTH_ADMIN"))
        assertTrue(!permissions.contains("android.permission.ACCESS_FINE_LOCATION"))
    }

    @Test
    @Config(sdk = [30])
    fun `bluetooth sync declares legacy permissions before Android 12`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val permissions = packageInfo.requestedPermissions.orEmpty().toSet()

        assertTrue(permissions.contains("android.permission.BLUETOOTH"))
        assertTrue(permissions.contains("android.permission.BLUETOOTH_ADMIN"))
        assertTrue(permissions.contains("android.permission.ACCESS_FINE_LOCATION"))
    }
}
