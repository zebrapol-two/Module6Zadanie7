package com.example.module6zadanie7

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScanner(
    private val context: Context
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothManager.adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var isScanning = false
    private var currentScanCallback: ScanCallback? = null

    fun startScan(): Flow<BleDevice> = callbackFlow {
        val scanner = bluetoothLeScanner
        if (scanner == null) {
            close(Exception("Bluetooth LE Scanner not available"))
            return@callbackFlow
        }

        if (!BluetoothPermissionHelper.isBluetoothEnabled(context)) {
            close(Exception("Bluetooth is not enabled"))
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val rssi = result.rssi

                // Показываем ВСЕ устройства с именем (без фильтрации по UUID)
                if (device.name != null && device.name.isNotEmpty()) {
                    trySend(BleDevice(device, rssi))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                close(Exception("Scan failed with error code: $errorCode"))
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(null, scanSettings, scanCallback)
        isScanning = true

        awaitClose {
            if (isScanning) {
                scanner.stopScan(scanCallback)
                isScanning = false
            }
        }
    }

    fun stopScan() {
        if (isScanning) {
            currentScanCallback?.let { callback ->
                bluetoothLeScanner?.stopScan(callback)
            }
            isScanning = false
            currentScanCallback = null
        }
    }
}
