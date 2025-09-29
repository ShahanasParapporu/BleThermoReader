package com.takniatech.contec.presentation.shared.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale


// Helper functions for timestamp formatting
@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(timestamp: Long): String {
    val date = java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    val today = java.time.LocalDate.now()
    val yesterday = today.minusDays(1)

    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(timestamp: Long): String {
    val time = java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalTime()

    return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
}

fun formatCompactDate(dateString: String): String {
    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    val date = try {
        inputFormat.parse(dateString)
    } catch (e: Exception) {
        return dateString
    }
    val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return date?.let { outputFormat.format(it) } ?: dateString
}

fun Context.saveScaledImage(uri: Uri, maxWidth: Int = 512, maxHeight: Int = 512): String? {
    try {
        val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val ratio = minOf(maxWidth.toFloat() / originalBitmap.width, maxHeight.toFloat() / originalBitmap.height)
        val width = (originalBitmap.width * ratio).toInt()
        val height = (originalBitmap.height * ratio).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        val file = File(cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun isBluetoothOn(context: Context): Boolean {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    return bluetoothAdapter?.isEnabled == true
}