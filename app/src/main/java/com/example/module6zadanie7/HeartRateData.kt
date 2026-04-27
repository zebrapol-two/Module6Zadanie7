package com.example.module6zadanie7

data class HeartRateData(
    val bpm: Int,
    val isDeviceContactDetected: Boolean = false,
    val energyExpended: Int? = null,
    val rrInterval: List<Float> = emptyList()
) {
    fun formattedBpm(): String = "$bpm bpm"

    companion object {
        fun empty(): HeartRateData = HeartRateData(0)
    }
}