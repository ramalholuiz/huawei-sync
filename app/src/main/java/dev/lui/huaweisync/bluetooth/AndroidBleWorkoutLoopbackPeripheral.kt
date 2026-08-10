package dev.lui.huaweisync.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid

class AndroidBleWorkoutLoopbackPeripheral(
    private val context: Context,
) {
    @SuppressLint("MissingPermission")
    fun start(payload: ByteArray): AutoCloseable {
        // ponytail: single-read GATT harness; add chunking only if payloads exceed 512 bytes.
        require(payload.size <= 512) { "Loopback BLE payload must fit a single GATT read." }

        val manager = checkNotNull(context.getSystemService(BluetoothManager::class.java)) {
            "Bluetooth manager is unavailable."
        }
        val advertiser = checkNotNull(manager.adapter?.bluetoothLeAdvertiser) {
            "Bluetooth LE advertiser is unavailable."
        }

        lateinit var server: BluetoothGattServer
        val callback = object : BluetoothGattServerCallback() {
            override fun onCharacteristicReadRequest(
                device: android.bluetooth.BluetoothDevice,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic,
            ) {
                val value = if (
                    characteristic.uuid == BleWorkoutProfile.WORKOUT_SUMMARY_CHARACTERISTIC_UUID &&
                    offset in 0..payload.size
                ) {
                    payload.copyOfRange(offset, payload.size)
                } else {
                    null
                }
                server.sendResponse(
                    device,
                    requestId,
                    if (value == null) BluetoothGatt.GATT_FAILURE else BluetoothGatt.GATT_SUCCESS,
                    offset,
                    value ?: ByteArray(0),
                )
            }
        }

        server = checkNotNull(manager.openGattServer(context, callback)) {
            "Bluetooth GATT server is unavailable."
        }
        val service = BluetoothGattService(
            BleWorkoutProfile.SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY,
        )
        service.addCharacteristic(
            BluetoothGattCharacteristic(
                BleWorkoutProfile.WORKOUT_SUMMARY_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ,
            ),
        )
        if (!server.addService(service)) {
            server.close()
            error("Bluetooth workout service was not added.")
        }

        val advertiseCallback = object : AdvertiseCallback() {}
        try {
            advertiser.startAdvertising(
                AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setConnectable(true)
                    .build(),
                AdvertiseData.Builder()
                    .addServiceUuid(ParcelUuid(BleWorkoutProfile.SERVICE_UUID))
                    .setIncludeDeviceName(false)
                    .build(),
                advertiseCallback,
            )
        } catch (_: RuntimeException) {
            server.close()
            error("Bluetooth advertising did not start.")
        }

        return AutoCloseable {
            runCatching { advertiser.stopAdvertising(advertiseCallback) }
            runCatching { server.close() }
        }
    }
}
