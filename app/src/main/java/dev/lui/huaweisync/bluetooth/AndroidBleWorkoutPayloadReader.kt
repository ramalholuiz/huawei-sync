package dev.lui.huaweisync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

object BleWorkoutProfile {
    val SERVICE_UUID: UUID = UUID.fromString("c4a6f6f0-6c2f-4f65-9df1-3698b9db2c01")
    val WORKOUT_SUMMARY_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("c4a6f6f1-6c2f-4f65-9df1-3698b9db2c01")
}

class AndroidBleWorkoutPayloadReader(
    private val context: Context,
    private val serviceUuid: UUID = BleWorkoutProfile.SERVICE_UUID,
    private val characteristicUuid: UUID = BleWorkoutProfile.WORKOUT_SUMMARY_CHARACTERISTIC_UUID,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) {
    @SuppressLint("MissingPermission")
    suspend fun readPayload(): ByteArray = withTimeout(timeoutMillis) {
        val adapter = checkNotNull(context.getSystemService(BluetoothManager::class.java)?.adapter) {
            "Bluetooth adapter is unavailable."
        }
        val scanner = checkNotNull(adapter.bluetoothLeScanner) {
            "Bluetooth LE scanner is unavailable."
        }

        suspendCancellableCoroutine { continuation ->
            val finished = AtomicBoolean(false)
            val deviceSelected = AtomicBoolean(false)
            var gatt: BluetoothGatt? = null
            var scanCallback: ScanCallback? = null

            fun close() {
                scanCallback?.let { callback -> runCatching { scanner.stopScan(callback) } }
                runCatching { gatt?.close() }
            }

            fun fail(message: String) {
                if (finished.compareAndSet(false, true)) {
                    close()
                    continuation.resumeWithException(IllegalStateException(message))
                }
            }

            fun succeed(payload: ByteArray) {
                if (finished.compareAndSet(false, true)) {
                    close()
                    continuation.resume(payload)
                }
            }

            val gattCallback = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        fail("Bluetooth GATT connection failed.")
                        return
                    }
                    if (newState == BluetoothProfile.STATE_CONNECTED && !gatt.discoverServices()) {
                        fail("Bluetooth GATT service discovery did not start.")
                    }
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        fail("Bluetooth GATT disconnected before read completed.")
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        fail("Bluetooth GATT service discovery failed.")
                        return
                    }
                    val characteristic = gatt
                        .getService(serviceUuid)
                        ?.getCharacteristic(characteristicUuid)
                    if (characteristic == null) {
                        fail("Bluetooth workout characteristic is unavailable.")
                        return
                    }
                    if (!gatt.readCharacteristic(characteristic)) {
                        fail("Bluetooth workout characteristic read did not start.")
                    }
                }

                @Suppress("DEPRECATION")
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int,
                ) {
                    finishRead(characteristic.uuid, characteristic.value, status)
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray,
                    status: Int,
                ) {
                    finishRead(characteristic.uuid, value, status)
                }

                private fun finishRead(uuid: UUID, value: ByteArray?, status: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        fail("Bluetooth workout characteristic read failed.")
                        return
                    }
                    if (uuid != characteristicUuid || value == null) {
                        fail("Bluetooth workout characteristic response is invalid.")
                        return
                    }
                    succeed(value)
                }
            }

            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    if (finished.get() || !deviceSelected.compareAndSet(false, true)) return
                    runCatching { scanner.stopScan(this) }
                    gatt = try {
                        result.device.connectGatt(
                            context,
                            false,
                            gattCallback,
                            BluetoothDevice.TRANSPORT_LE,
                        )
                    } catch (_: RuntimeException) {
                        null
                    }
                    if (gatt == null) {
                        fail("Bluetooth GATT connection did not start.")
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    fail("Bluetooth scan failed.")
                }
            }

            continuation.invokeOnCancellation {
                if (finished.compareAndSet(false, true)) close()
            }
            try {
                scanner.startScan(
                    listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build()),
                    ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build(),
                    checkNotNull(scanCallback),
                )
            } catch (_: RuntimeException) {
                fail("Bluetooth scan did not start.")
            }
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 15_000L
    }
}
