package com.takniatech.contec.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.takniatech.contec.data.model.ContecDevice
import com.takniatech.contec.presentation.shared.components.ContecButton
import com.takniatech.contec.ui.theme.ContecColors
import com.takniatech.contec.ui.theme.Montserrat
import com.takniatech.contec.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val foundDevices by viewModel.foundDevices.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val connectStatus by viewModel.connectStatus.collectAsState(null)
    val connectedDeviceAddress by viewModel.connectedDeviceAddress.collectAsState()

    val isBleScanSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val permissions = if (isBleScanSupported) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var arePermissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        arePermissionsGranted = perms.values.all { it }
        if (!arePermissionsGranted) {
            Toast.makeText(
                viewModel.context,
                "Permissions denied. Cannot scan for devices. Enable from App settings",
                Toast.LENGTH_LONG
            ).show()
        } else {
            viewModel.toggleSearch(true)
        }
    }

    // Check permissions on screen launch
    LaunchedEffect(Unit) {
        arePermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(
                viewModel.context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(connectedDeviceAddress) {
        connectedDeviceAddress?.let { address ->
            Log.d("ScanScreen", "Connection successful. Returning to profile screen with: $address")
            Toast.makeText(viewModel.context, "Device connected: $address", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Device Scan",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ContecColors.AdaptiveText
                    )
                }, navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ContecColors.AdaptiveText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Roboto),
                    color = ContecColors.AdaptiveText,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
                val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
                // Scan Button
                ContecButton(

                    onClick = {
                        if (arePermissionsGranted) {
                            if (isSearching) {
                                // Stop scanning if already scanning
                                viewModel.toggleSearch(false)
                            } else {
                                // Start scanning
                                viewModel.toggleSearch(true)
                            }
                        } else {
                            permissionLauncher.launch(permissions)
                        }
                    },
                    text = when {
                        !bluetoothEnabled -> "Start Scan"
                        isSearching -> "Stop Scan"
                        else -> "Start Scan"
                    },
                    enabled = connectStatus != 2 && connectStatus != 3 , // Disable button while connecting or connected
                    isLoading = isSearching && connectStatus != 3 ,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    ),
                    isOutlined = false
                )

                // Disconnect Button (Only visible when connected)
                if (connectStatus == 3) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { viewModel.disconnectDevice() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Disconnect Device")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Found Devices (${foundDevices.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ContecColors.AdaptiveText,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)), // Clip list to rounded corners
                    tonalElevation = 2.dp, // Subtle elevation for light/dark theme contrast
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (foundDevices.isEmpty() && !isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Tap 'Start Scan' to find devices.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else if (foundDevices.isEmpty() && isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Searching...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(foundDevices, key = { it.bluetoothDevice.address }) { device ->
                                DeviceListItem(
                                    device = device,
                                    isConnected = connectStatus == 3 && viewModel.sdkManager.connectedDeviceAddress == device.bluetoothDevice.address,
                                    onConnectClick = { viewModel.connectToDevice(device) },
                                    isConnecting = connectStatus == 2,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                // Use a thinner, more subtle divider
                                if (foundDevices.last() != device) {
                                    Divider(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceListItem(
    device: ContecDevice,
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceName = try {
        device.bluetoothDevice.name
    } catch (e: SecurityException) {
        Log.e("DeviceListItem", "SecurityException accessing device name: ${e.message}")
        null
    } catch (e: Exception) {
        Log.e("DeviceListItem", "Error accessing device name: ${e.message}")
        null
    } ?: "Unknown Device" // Fallback to "Unknown Device"

    val isConnectActionAvailable = !isConnected && !isConnecting
    val statusAreaModifier = Modifier
        .widthIn(min = 75.dp) // Ensure button/status area has defined min width

    Card(
        onClick = { if (isConnectActionAvailable) onConnectClick() },
        enabled = isConnectActionAvailable,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Subtle background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth Device",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ContecColors.AdaptiveText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.bluetoothDevice.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = ContecColors.AdaptiveText.copy(alpha = 0.6f)
                )
                Text(
                    text = "RSSI: ${device.rssi} dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = ContecColors.AdaptiveText.copy(alpha = 0.6f)
                )
                Text(
                    text = "New Data: ${if (device.hasNewData) "✅" else "❌"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ContecColors.AdaptiveText.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.width(8.dp))

            when {
                isConnected -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Connected",
                        tint = Color.Green.copy(alpha = 0.8f), // Use the same success green
                        modifier = Modifier.size(32.dp)
                    )
                }
                isConnecting -> {
                    Button(
                        onClick = {}, // No action during connection
                        enabled = false, // Must be disabled
                        modifier = statusAreaModifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) // Faded color for disabled
                        )
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White // Loader over the button background
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onConnectClick,
                        enabled = true,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

