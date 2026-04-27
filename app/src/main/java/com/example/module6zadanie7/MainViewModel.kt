package com.example.module6zadanie7

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var scanJob: Job? = null
    private var bleScanner: BleScanner? = null
    private var heartRateManager: HeartRateManager? = null

    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.Idle)
    val scanningState: StateFlow<ScanningState> = _scanningState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDevice>> = _discoveredDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _heartRate = MutableStateFlow<HeartRateData>(HeartRateData.empty())
    val heartRate: StateFlow<HeartRateData> = _heartRate.asStateFlow()

    fun initialize(context: Context) {
        bleScanner = BleScanner(context)
        heartRateManager = HeartRateManager(context)

        viewModelScope.launch {
            heartRateManager?.connectionState?.collect { state ->
                _connectionState.value = state
                if (state is BleConnectionState.Disconnected) {
                    _heartRate.value = HeartRateData.empty()
                }
            }
        }

        viewModelScope.launch {
            heartRateManager?.heartRateData?.collect { data ->
                _heartRate.value = data
            }
        }
    }

    fun startScanning() {
        if (_scanningState.value is ScanningState.Scanning) {
            stopScanning()
        }

        _discoveredDevices.value = emptyList()
        _scanningState.value = ScanningState.Scanning

        scanJob = viewModelScope.launch {
            bleScanner?.startScan()?.collect { device ->
                val currentDevices = _discoveredDevices.value.toMutableList()
                val existingIndex = currentDevices.indexOfFirst { it.address == device.address }

                if (existingIndex != -1) {
                    currentDevices[existingIndex] = device
                } else {
                    currentDevices.add(device)
                }

                _discoveredDevices.value = currentDevices.sortedBy { it.name }
            }
        }
    }

    fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        bleScanner?.stopScan()
        _scanningState.value = ScanningState.Idle
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScanning()
        heartRateManager?.connect(device)
    }

    fun disconnect() {
        heartRateManager?.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        heartRateManager?.disconnect()
    }
}

sealed class ScanningState {
    object Idle : ScanningState()
    object Scanning : ScanningState()
}