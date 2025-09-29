package com.takniatech.contec.presentation.profile

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.takniatech.contec.data.model.TemperatureReading
import com.takniatech.contec.data.model.User
import com.takniatech.contec.presentation.shared.utils.formatCompactDate
import com.takniatech.contec.presentation.shared.utils.formatTime
import com.takniatech.contec.presentation.shared.utils.formatTimestamp
import com.takniatech.contec.ui.theme.ContecColors
import com.takniatech.contec.ui.theme.Montserrat
import com.takniatech.contec.ui.theme.Roboto

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToScan: () -> Unit,
    onLogout: () -> Unit,
    userId: Int
) {
    val user by viewModel.user.collectAsState()
    val realtimeReadings by viewModel.realtimeReadings.collectAsState()
    val historyReadings by viewModel.historyReadings.collectAsState()
    val timeSynced by viewModel.timeSynced.collectAsState()
    val storageTotal by viewModel.storageTotal.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.refreshData(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("User Profile",style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold
                ),
                    color = ContecColors.AdaptiveText) },
                actions = {
                    IconButton(onClick = onNavigateToScan) {
                        Icon(Icons.Filled.Bluetooth, contentDescription = "Scan Devices",tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        val profileUser = user ?: run {
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { UserHeaderCard(profileUser, realtimeReadings.size + historyReadings.size, timeSynced, storageTotal) }
                item { MetricsSection(profileUser) }
                item { RealtimeSection(realtimeReadings) }
                item { HistorySection(historyReadings, storageTotal) }
//                item { RealtimeSection(realtimeDummy) }
//                item { HistorySection(historyDummy, 16) }
                item { Spacer(Modifier.height(10.dp)) }
            }
        }
}

@Composable
fun UserHeaderCard(user: User, totalReadings: Int, timeSynced: Boolean, storageTotal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp) // Larger size
                    .clip(CircleShape)
                    //.background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)) // Solid background
                    //.padding(6.dp),
                    .then(
                        if (user.profileImageUri.isNullOrEmpty()) {
                            Modifier
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                                .padding(6.dp)
                        } else {
                            Modifier // No extra background or padding
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {

                if (!user.profileImageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Default Profile Icon",
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    user.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = Roboto,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                )
                Spacer(Modifier.height(4.dp))
                // Use a dedicated Surface or Chip for total readings
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            "Total Readings: $totalReadings",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimary)
                        )
                    }

                    if (timeSynced) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "Time synchronized",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSecondary)
                            )
                        }
                    }

                    // show storage total if > 0
                    if (storageTotal > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "Stored: $storageTotal",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onTertiary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsSection(user: User) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Health Metrics", style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            color = ContecColors.AdaptiveText
        ))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricItem(Icons.Filled.WbSunny, "DOB", user.dateOfBirth,modifier = Modifier.weight(1f))
            MetricItem(Icons.Filled.Height, "Height", "${user.height} cm", modifier = Modifier.weight(1f))
            MetricItem(Icons.Filled.Scale, "Weight", "${user.weight} kg", modifier = Modifier.weight(1f))
            MetricItem(Icons.Filled.Favorite, "Gender", user.gender, modifier = Modifier.weight(1f))
        }
        Divider(Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun MetricItem(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    val displayValue = if (title == "DOB" && value.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
        formatCompactDate(value)
    } else {
        value
    }
    Card(
        modifier = modifier.height(85.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                displayValue,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ContecColors.AdaptiveText),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall.copy(color = ContecColors.AdaptiveText.copy(alpha = 0.7f))
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RealtimeSection(readings: List<TemperatureReading>) {

    var isExpanded by remember { mutableStateOf(false) }
    val displayLimit = 3
    val displayedReadings = if (isExpanded) readings else readings.take(displayLimit)
    val hasMore = readings.size > displayLimit

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Realtime Data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ContecColors.AdaptiveText,
                modifier = Modifier.weight(1f)
            )

            // Expand/Collapse Button
            if (hasMore) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    val icon = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                    val text = if (isExpanded) "Collapse" else "View All (${readings.size})"

                    Icon(icon, contentDescription = text)
                    Spacer(Modifier.width(4.dp))
                    Text(text)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (readings.isEmpty()) {
            Text("No realtime data available.",color = ContecColors.AdaptiveText,)
        } else {
            // Display the controlled list
            displayedReadings.forEach { ReadingListItem(it) }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(top = 8.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistorySection(readings: List<TemperatureReading>, storageTotal: Int) {

    // State to track if the list is expanded
    var isExpanded by remember { mutableStateOf(false) }

    val displayLimit = 5
    // Display the full list if expanded, otherwise display only the first 5
    val displayedReadings = if (isExpanded) readings else readings.take(displayLimit)
    val hasMore = readings.size > displayLimit

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "History Data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ContecColors.AdaptiveText,
                modifier = Modifier.weight(1f)
            )

            // Expand/Collapse Button
            if (hasMore) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    val icon =
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                    val text = if (isExpanded) "Collapse" else "View All (${readings.size})"

                    Icon(icon, contentDescription = text)
                    Spacer(Modifier.width(4.dp))
                    Text(text)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (readings.isEmpty()) {
            Text("No history data available. Storage Total: $storageTotal",color = ContecColors.AdaptiveText,)
        } else {
            // Display the controlled list
            displayedReadings.forEach { ReadingListItem(it) }
        }

        // Add a message if collapsed and there are more items
        if (!isExpanded && hasMore) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Showing $displayLimit of ${readings.size} entries.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(4.dp))
        }

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReadingListItem(reading: TemperatureReading) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Device Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reading.deviceName ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ContecColors.AdaptiveText,
                    modifier = Modifier.weight(1f)
                )

                // Status Indicator - Uses fixed colors for consistent appearance
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                reading.deviceError != null -> MaterialTheme.colorScheme.errorContainer
                                reading.isRealtime -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            reading.deviceError != null -> "Error"
                            reading.isRealtime -> "Live"
                            else -> "Stored"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        // Use fixed colors for consistent high contrast
                        color = when {
                            reading.deviceError != null -> MaterialTheme.colorScheme.onErrorContainer
                            reading.isRealtime -> ContecColors.AdaptiveText
                            else -> ContecColors.AdaptiveText
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Device Address
            Text(
                text = "Device: ${reading.deviceAddress}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = Roboto
                ),
                color = ContecColors.AdaptiveText,
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Temperature Display - Main Focus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format("%.1f", reading.temp),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.primary // Use theme primary
                    )
                    Text(
                        text = "°${reading.unit}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.primary, // Use theme primary
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                // Timestamp
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatTimestamp(reading.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = Roboto,
                            fontSize = 11.sp
                        ),
                        color = ContecColors.AdaptiveText,
                    )
                    Text(
                        text = formatTime(reading.timestamp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = ContecColors.AdaptiveText,
                    )
                }
            }

            // Error Message (if present)
            if (reading.deviceError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reading.deviceError,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = Roboto,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Raw State (if available and for debugging)
            if (reading.rawState != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Raw State: ${reading.rawState}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = Roboto,
                        fontSize = 10.sp
                    ),
                    color = ContecColors.AdaptiveText,
                )
            }
        }
    }
}
