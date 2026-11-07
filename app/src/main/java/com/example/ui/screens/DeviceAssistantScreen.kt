package com.example.ui.screens

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.jarvis.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.util.DeviceController

@Composable
fun DeviceAssistantScreen(
    jarvisViewModel: JarvisViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val deviceStatus by jarvisViewModel.deviceStatus.collectAsState()
    val controller = remember { DeviceController(context) }
    val installedApps = remember { controller.getInstalledAppPackages() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        JarvisBackground,
                        Color(0xFF071426),
                        JarvisBackground
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = JarvisSurface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFF1B365D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10223D))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = JarvisCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "DEVICE HARDWARE BRIDGE",
                                color = JarvisCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Telemetry • System Controls • App Dispatcher",
                                color = JarvisNeonMint,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = { jarvisViewModel.refreshDeviceStatus() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10223D))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = JarvisCyan
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flashlight Control Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                        border = BorderStroke(1.dp, Color(0xFF1B365D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (deviceStatus.flashlightOn) JarvisCyan.copy(alpha = 0.2f) else Color(0xFF10223D)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (deviceStatus.flashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                        contentDescription = "Flashlight",
                                        tint = if (deviceStatus.flashlightOn) JarvisCyan else JarvisTextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "High-Intensity Flashlight",
                                        color = JarvisTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (deviceStatus.flashlightOn) "Illumination Active" else "Emitter Offline",
                                        color = if (deviceStatus.flashlightOn) JarvisNeonMint else JarvisTextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Switch(
                                checked = deviceStatus.flashlightOn,
                                onCheckedChange = { jarvisViewModel.executeFlashlightToggle() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JarvisCyan,
                                    checkedTrackColor = Color(0xFF003845),
                                    uncheckedThumbColor = JarvisTextSecondary,
                                    uncheckedTrackColor = Color(0xFF10223D)
                                )
                            )
                        }
                    }
                }

                // Volume Control Slider Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                        border = BorderStroke(1.dp, Color(0xFF1B365D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Media Acoustic Level",
                                        color = JarvisTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${deviceStatus.volumePercent}%",
                                    color = JarvisCyan,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Slider(
                                value = deviceStatus.volumePercent.toFloat(),
                                onValueChange = { jarvisViewModel.executeVolumeChange(it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = JarvisCyan,
                                    activeTrackColor = JarvisCyan,
                                    inactiveTrackColor = Color(0xFF10223D)
                                )
                            )
                        }
                    }
                }

                // Device Specifications & Telemetry Card
                item {
                    Text(
                        text = "SYSTEM TELEMETRY",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                        border = BorderStroke(1.dp, Color(0xFF1B365D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SpecRow("Device Model", deviceStatus.deviceModel)
                            SpecRow("Android OS", deviceStatus.androidVersion)
                            SpecRow(
                                "Power Reserve",
                                "${deviceStatus.batteryLevel}% ${if (deviceStatus.isCharging) "(Charging)" else ""}"
                            )
                            SpecRow("Dynamic RAM", "${deviceStatus.freeRamMb} MB Available / ${deviceStatus.totalRamMb} MB")
                            SpecRow("Storage Space", controller.getStorageSummary())
                        }
                    }
                }

                // Approved System Shortcuts (Alarms, Timers, Settings)
                item {
                    Text(
                        text = "APPROVED SYSTEM SHORTCUTS",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemShortcutButton(
                            title = "Set Alarm",
                            icon = Icons.Default.Alarm,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                try {
                                    val intent = controller.createAlarm("JARVIS Wake Call", 7, 0)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Alarm service unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        SystemShortcutButton(
                            title = "10m Timer",
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                try {
                                    val intent = controller.createTimer("JARVIS Timer", 600)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Timer service unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        SystemShortcutButton(
                            title = "Settings",
                            icon = Icons.Default.Settings,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                try {
                                    val intent = controller.openSettings(Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Settings unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // Installed Applications Dispatcher
                item {
                    Text(
                        text = "APP DISPATCHER (INSTALLED PACKAGES)",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                        border = BorderStroke(1.dp, Color(0xFF1B365D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (installedApps.isEmpty()) {
                                Text(
                                    text = "No launchable applications enumerated.",
                                    color = JarvisTextSecondary,
                                    fontSize = 12.sp
                                )
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(installedApps.take(15)) { (appName, pkg) ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF0C1F38),
                                            border = BorderStroke(1.dp, Color(0xFF1B4E82)),
                                            modifier = Modifier.clickable {
                                                val intent = controller.openApp(pkg)
                                                if (intent != null) {
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Unable to launch $appName", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Apps,
                                                    contentDescription = null,
                                                    tint = JarvisCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = appName,
                                                    color = JarvisTextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
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
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = JarvisTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = JarvisTextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SystemShortcutButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = JarvisCardBg,
        border = BorderStroke(1.dp, Color(0xFF1B365D)),
        modifier = modifier
            .height(70.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = JarvisCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = JarvisTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
