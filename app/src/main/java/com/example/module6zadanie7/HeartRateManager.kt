package com.example.module6zadanie7

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking

class HeartRateManager(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null
    private var currentDevice: BluetoothDevice? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _connectionState = Channel<BleConnectionState>(Channel.BUFFERED)
    val connectionState: Flow<BleConnectionState> = _connectionState.receiveAsFlow()

    private val _heartRateData = Channel<HeartRateData>(Channel.BUFFERED)
    val heartRateData: Flow<HeartRateData> = _heartRateData.receiveAsFlow()

    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when {
                newState == BluetoothProfile.STATE_CONNECTED -> {
                    reconnectAttempts = 0
                    mainHandler.postDelayed({
                        gatt.discoverServices()
                    }, 500)
                }
                newState == BluetoothProfile.STATE_DISCONNECTED -> {
                    closeConnection()
                }
                status == 133 || status == 62 || status == 8 -> {
                    if (reconnectAttempts < maxReconnectAttempts) {
                        reconnectAttempts++
                        mainHandler.postDelayed({
                            currentDevice?.let { connect(it) }
                        }, 2000)
                    } else {
                        closeConnection()
                        reconnectAttempts = 0
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(HeartRateConstants.HEART_RATE_SERVICE_UUID)
                if (service != null) {
                    heartRateCharacteristic = service.getCharacteristic(
                        HeartRateConstants.HEART_RATE_MEASUREMENT_UUID
                    )

                    heartRateCharacteristic?.let { characteristic ->
                        enableHeartRateNotifications(characteristic)
                    }
                } else {
                    runBlocking {
                        _connectionState.send(BleConnectionState.Error("Heart Rate Service not found"))
                    }
                    disconnect()
                }
            } else {
                runBlocking {
                    _connectionState.send(BleConnectionState.Error("Service discovery failed"))
                }
                disconnect()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == HeartRateConstants.HEART_RATE_MEASUREMENT_UUID) {
                parseHeartRateData(characteristic.value)
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                runBlocking {
                    _connectionState.send(BleConnectionState.Connected)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == HeartRateConstants.HEART_RATE_MEASUREMENT_UUID) {
                    parseHeartRateData(characteristic.value)
                }
            }
        }
    }

    private fun enableHeartRateNotifications(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = HeartRateConstants.CLIENT_CHARACTERISTIC_CONFIG_UUID
        val descriptor = characteristic.getDescriptor(cccdUuid)

        if (descriptor != null) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        } else {
            bluetoothGatt?.readCharacteristic(characteristic)
        }

        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
    }

    private fun parseHeartRateData(data: ByteArray) {
        if (data.isEmpty()) return

        val flags = data[0].toInt() and 0xFF

        val isHeartRate16Bit = (flags and 0x01) != 0
        val isSensorContactDetected = (flags and 0x02) != 0
        val isSensorContactSupported = (flags and 0x04) != 0
        val isEnergyExpendedPresent = (flags and 0x08) != 0
        val isRRIntervalPresent = (flags and 0x10) != 0

        var offset = 1

        val heartRate = if (isHeartRate16Bit) {
            val hr = ((data[offset + 1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
            offset += 2
            hr
        } else {
            val hr = data[offset].toInt() and 0xFF
            offset += 1
            hr
        }

        val energyExpended = if (isEnergyExpendedPresent) {
            val ee = ((data[offset + 1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
            offset += 2
            ee
        } else {
            null
        }

        val rrIntervals = mutableListOf<Float>()
        if (isRRIntervalPresent) {
            while (offset + 1 < data.size) {
                val rrValue = ((data[offset + 1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
                val rrSeconds = rrValue / 1024.0f
                rrIntervals.add(rrSeconds)
                offset += 2
            }
        }

        val deviceContactDetected = isSensorContactSupported && isSensorContactDetected

        val heartRateData = HeartRateData(
            bpm = heartRate,
            isDeviceContactDetected = deviceContactDetected,
            energyExpended = energyExpended,
            rrInterval = rrIntervals
        )

        runBlocking {
            _heartRateData.send(heartRateData)
        }
    }

    fun connect(device: BluetoothDevice) {
        disconnect()
        currentDevice = device

        runBlocking {
            _connectionState.send(BleConnectionState.Connecting)
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        heartRateCharacteristic = null
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null

        runBlocking {
            _connectionState.send(BleConnectionState.Disconnected)
        }
    }

    private fun closeConnection() {
        heartRateCharacteristic = null
        bluetoothGatt?.close()
        bluetoothGatt = null

        runBlocking {
            _connectionState.send(BleConnectionState.Disconnected)
        }
    }
}