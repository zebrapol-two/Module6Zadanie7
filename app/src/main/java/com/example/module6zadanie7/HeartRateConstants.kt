package com.example.module6zadanie7

import java.util.UUID

object HeartRateConstants {
    // Heart Rate Service UUID (0x180D)
    val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

    // Heart Rate Measurement Characteristic UUID (0x2A37)
    val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    // Body Sensor Location Characteristic UUID (0x2A38) - optional
    val BODY_SENSOR_LOCATION_UUID = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb")

    // Client Characteristic Configuration Descriptor UUID (0x2902)
    val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}