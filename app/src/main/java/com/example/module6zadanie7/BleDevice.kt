package com.example.module6zadanie7

import android.bluetooth.BluetoothDevice

data class BleDevice(
    val device: BluetoothDevice,
    val rssi: Int,
    val name: String
) {
    val address: String get() = device.address

    constructor(device: BluetoothDevice, rssi: Int) : this(
        device = device,
        rssi = rssi,
        name = device.name ?: "Unknown Device"
    )
}