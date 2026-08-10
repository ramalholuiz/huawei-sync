package dev.lui.huaweisync.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class AndroidBluetoothCapabilityReader(
    private val context: Context,
) : BluetoothCapabilityReader {
    @SuppressLint("MissingPermission")
    override fun snapshot(): BluetoothCapabilitySnapshot {
        val packageManager = context.packageManager
        val adapterPresent = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ||
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val scanPermissionGranted = hasBluetoothScanPermission()
        val connectPermissionGranted = hasBluetoothConnectPermission()
        val legacyLocationPermissionGranted = hasLegacyLocationPermission()
        val enabled = adapterPresent &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || connectPermissionGranted) &&
            runCatching {
                context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
            }.getOrDefault(false)

        return BluetoothCapabilitySnapshot(
            adapterPresent = adapterPresent,
            enabled = enabled,
            scanPermissionGranted = scanPermissionGranted,
            connectPermissionGranted = connectPermissionGranted,
            companionPairingAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && adapterPresent,
            legacyLocationPermissionGranted = legacyLocationPermissionGranted,
        )
    }

    private fun hasBluetoothScanPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

    private fun hasBluetoothConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    private fun hasLegacyLocationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ||
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
