package com.example.module6zadanie7

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleApp() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    val scanningState by viewModel.scanningState.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val heartRate by viewModel.heartRate.collectAsStateWithLifecycle()

    MainScreen(
        scanningState = scanningState,
        discoveredDevices = discoveredDevices,
        connectionState = connectionState,
        heartRate = heartRate,
        onStartScanning = { viewModel.startScanning() },
        onStopScanning = { viewModel.stopScanning() },
        onConnectDevice = { device -> viewModel.connectToDevice(device.device) },
        onDisconnect = { viewModel.disconnect() }
    )
}

@Composable
fun PermissionScreen(onRequestPermissions: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bluetooth Permissions Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This app needs Bluetooth permissions to scan for and connect to heart rate monitors.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(onClick = onRequestPermissions) {
                Text("Grant Permissions")
            }
        }
    }
}

@Composable
fun EnableBluetoothScreen(onEnableBluetooth: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bluetooth is Disabled",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Please enable Bluetooth to use this app.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(onClick = onEnableBluetooth) {
                Text("Enable Bluetooth")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    scanningState: ScanningState,
    discoveredDevices: List<BleDevice>,
    connectionState: BleConnectionState,
    heartRate: HeartRateData,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onConnectDevice: (BleDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    Scaffold(
        topBar = { TopBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeartRateCard(
                heartRate = heartRate,
                connectionState = connectionState
            )

            ConnectionControls(
                connectionState = connectionState,
                onDisconnect = onDisconnect
            )

            ScanSection(
                scanningState = scanningState,
                discoveredDevices = discoveredDevices,
                connectionState = connectionState,
                onStartScanning = onStartScanning,
                onStopScanning = onStopScanning,
                onConnectDevice = onConnectDevice
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "BLE",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Heart Rate Monitor",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun HeartRateCard(heartRate: HeartRateData, connectionState: BleConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Heart Rate",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (connectionState) {
                is BleConnectionState.Connected -> {
                    Text(
                        text = heartRate.formattedBpm(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (heartRate.isDeviceContactDetected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Device contact detected",
                            fontSize = 12.sp,
                            color = Color.Green
                        )
                    }

                    if (heartRate.energyExpended != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Energy Expended: ${heartRate.energyExpended} kcal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                is BleConnectionState.Connecting -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connecting...",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                is BleConnectionState.Disconnected -> {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Not Connected",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Not Connected",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                is BleConnectionState.Error -> {
                    Icon(
                        imageVector = Icons.Default.DeviceUnknown,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error: ${connectionState.message}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionControls(
    connectionState: BleConnectionState,
    onDisconnect: () -> Unit
) {
    if (connectionState is BleConnectionState.Connected) {
        Button(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothConnected,
                contentDescription = "Disconnect"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disconnect")
        }
    }
}

@Composable
fun ScanSection(
    scanningState: ScanningState,
    discoveredDevices: List<BleDevice>,
    connectionState: BleConnectionState,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onConnectDevice: (BleDevice) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {
                when (scanningState) {
                    ScanningState.Scanning -> onStopScanning()
                    ScanningState.Idle -> onStartScanning()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = connectionState !is BleConnectionState.Connecting
        ) {
            Icon(
                imageVector = if (scanningState is ScanningState.Scanning)
                    Icons.Default.BluetoothSearching
                else
                    Icons.Default.Bluetooth,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                when (scanningState) {
                    ScanningState.Scanning -> "Stop Scanning"
                    ScanningState.Idle -> "Start Scanning"
                }
            )
        }

        if (discoveredDevices.isNotEmpty() && connectionState !is BleConnectionState.Connected) {
            Text(
                text = "Found Devices (${discoveredDevices.size})",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(discoveredDevices) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onConnectDevice(device) },
                        enabled = connectionState !is BleConnectionState.Connecting &&
                                connectionState !is BleConnectionState.Connected
                    )
                }
            }
        } else if (connectionState !is BleConnectionState.Connected && discoveredDevices.isEmpty() && scanningState is ScanningState.Scanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scanning for heart rate devices...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (connectionState !is BleConnectionState.Connected && discoveredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No devices found. Tap 'Start Scanning' to begin.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (connectionState is BleConnectionState.Connected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BluetoothConnected,
                        contentDescription = "Connected",
                        tint = Color.Green
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connected to heart rate monitor",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: BleDevice,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = device.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (enabled) {
                Text(
                    text = "Connect",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}