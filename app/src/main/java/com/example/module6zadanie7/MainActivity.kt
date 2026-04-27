package com.example.module6zadanie7

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.module6zadanie7.ui.theme.Module6Zadanie7Theme

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Module6Zadanie7Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasPermissions = remember { mutableStateOf(checkPermissions()) }
                    val isBluetoothEnabled = remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }

                    LaunchedEffect(Unit) {
                        hasPermissions.value = checkPermissions()
                        isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
                    }

                    when {
                        !hasPermissions.value -> {
                            PermissionScreen(
                                onRequestPermissions = {
                                    requestPermissions()
                                    hasPermissions.value = checkPermissions()
                                }
                            )
                        }
                        !isBluetoothEnabled.value -> {
                            EnableBluetoothScreen(
                                onEnableBluetooth = {
                                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                                    isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
                                }
                            )
                        }
                        else -> {
                            BleApp()
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        ActivityCompat.requestPermissions(this, permissions, 100)
    }
}