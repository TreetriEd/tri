package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewModel.NetworkOptimizerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NetworkOptimizerHomeScreen(
    viewModel: NetworkOptimizerViewModel,
    modifier: Modifier = Modifier
) {
    // Collect model values
    val isSimMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()
    val isAutoSwitch by viewModel.autoSwitchEnabled.collectAsStateWithLifecycle()
    val isNotifications by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val wifiThreshold by viewModel.wifiThresholdSetting.collectAsStateWithLifecycle()
    val cellularThreshold by viewModel.cellularThresholdSetting.collectAsStateWithLifecycle()

    val homeNetworks by viewModel.homeNetworks.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val recommendation by viewModel.currentRecommendation.collectAsStateWithLifecycle()

    // Simulated States
    val simWifiEnabled by viewModel.simulatedWifiEnabled.collectAsStateWithLifecycle()
    val simWifiSsid by viewModel.simulatedWifiSsid.collectAsStateWithLifecycle()
    val simWifiStrength by viewModel.simulatedWifiStrength.collectAsStateWithLifecycle()
    val simCellEnabled by viewModel.simulatedCellularEnabled.collectAsStateWithLifecycle()
    val simCellStrength by viewModel.simulatedCellularStrength.collectAsStateWithLifecycle()
    val simCellCarrier by viewModel.simulatedCellularCarrier.collectAsStateWithLifecycle()
    val simActiveNet by viewModel.simulatedActiveNetwork.collectAsStateWithLifecycle()

    // Real Hardware States
    val realWifiEnabled by viewModel.realWifiEnabled.collectAsStateWithLifecycle()
    val realWifiSsid by viewModel.realWifiSsid.collectAsStateWithLifecycle()
    val realWifiStrength by viewModel.realWifiStrength.collectAsStateWithLifecycle()
    val realCellEnabled by viewModel.realCellularEnabled.collectAsStateWithLifecycle()
    val realCellCarrier by viewModel.realCellularCarrier.collectAsStateWithLifecycle()
    val realCellStrength by viewModel.realCellularStrength.collectAsStateWithLifecycle()
    val realActiveNet by viewModel.realActiveNetwork.collectAsStateWithLifecycle()

    // Local UI states
    var newHomeSsid by remember { mutableStateOf("") }
    var newHomeLabel by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Set colors for signals
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "NetSwitch",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color(0xFF43493E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "INTELLIGENT HUB",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFF74796D)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color(0xFFFBFDF8),
                    titleContentColor = Color(0xFF1A1C19)
                ),
                actions = {
                    // Custom Organic Target-Logo from Natural Tones Theme XML inline mockup
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDE5D6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .border(2.dp, Color(0xFF43493E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF43493E))
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearLogHistory() },
                        modifier = Modifier.testTag("clear_logs_button").padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "ล้างประวัติการนำส่งข้อมูล",
                            tint = Color(0xFF4F6630)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFFBFDF8),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Recommendation Alert Card (Natural Tones High-Fidelity Hero Status Card)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recommendation_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAutoSwitch) Color(0xFFE1EAD3) else Color(0xFFF3F4E9)
                    ),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp)
                    ) {
                        // Abstract Organic Shape (Graphic Element)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 30.dp, y = (-30).dp)
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC8D3B6).copy(alpha = 0.5f))
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "การเชื่อมต่อขณะนี้ / ปรับแต่งสัญญาณ",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        color = Color(0xFF43493E).copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val currentConnLabel = if (isSimMode) {
                                        if (simActiveNet == "Wi-Fi" && simWifiEnabled) simWifiSsid else if (simActiveNet == "Cellular" && simCellEnabled) simCellCarrier else "No Active Link"
                                    } else {
                                        if (realActiveNet == "Wi-Fi" && realWifiEnabled) realWifiSsid else if (realActiveNet == "Cellular" && realCellEnabled) realCellCarrier else "No Active Link"
                                    }
                                    Text(
                                        text = currentConnLabel,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1C19)
                                    )
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFDF8)),
                                    border = BorderStroke(1.dp, Color(0xFF4F6630).copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(100.dp)
                                ) {
                                    Text(
                                        text = if (isAutoSwitch) "OPTIMIZER ON" else "OFFLINE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color(0xFF4F6630)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1C19)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    // Custom active mini-signal bars
                                    Box(modifier = Modifier.size(width = 5.dp, height = 8.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF4F6630)))
                                    Box(modifier = Modifier.size(width = 5.dp, height = 12.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF4F6630)))
                                    Box(modifier = Modifier.size(width = 5.dp, height = 16.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF4F6630)))
                                    Box(modifier = Modifier.size(width = 5.dp, height = 22.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF4F6630)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSimMode) "Sandbox Active" else "Connected Mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF43493E)
                                    )
                                }

                                Text(
                                    text = "สลับล่าสุดเมื่อสักครู่",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF5D6257)
                                )
                            }
                        }
                    }
                }
            }

            // Mode Toggle: REAL vs SIMULATED (Natural Tones Polished Layout)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE1E4D9)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "โหมดการทำงาน",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43493E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "แอนดรอยด์จำกัดการเปิดปิดระดับระบบแบบปิด เพื่อความปลอดภัย คุณสามารถจำลองเหตุการณ์ผ่านกล่อง Sandbox หรือเชื่อมโยงอุปกรณ์จริงในการตรวจผลลัพธ์ได้",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF74796D),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4E9))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val activeTabColor = Color(0xFF4F6630)
                            val activeTextColor = Color.White
                            val inactiveTextColor = Color(0xFF5D6257)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSimMode) activeTabColor else Color.Transparent)
                                    .clickable { viewModel.isSimulationMode.value = true }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "กล่องจำลอง Sandbox",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSimMode) activeTextColor else inactiveTextColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isSimMode) activeTabColor else Color.Transparent)
                                    .clickable { viewModel.isSimulationMode.value = false }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "เชื่อมฮาร์ดแวร์จริง",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSimMode) activeTextColor else inactiveTextColor
                                )
                            }
                        }
                    }
                }
            }

            // Connection Status Panel
            item {
                val activeSsid = if (isSimMode) simWifiSsid else realWifiSsid
                val activeWifiStrength = if (isSimMode) simWifiStrength else realWifiStrength
                val activeWifiEnabled = if (isSimMode) simWifiEnabled else realWifiEnabled

                val activeCarrier = if (isSimMode) simCellCarrier else realCellCarrier
                val activeCellStrength = if (isSimMode) simCellStrength else realCellStrength
                val activeCellEnabled = if (isSimMode) simCellEnabled else realCellEnabled

                val activeNet = if (isSimMode) simActiveNet else realActiveNet

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "สถานะสตรีมเน็ตเวิร์ก",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF43493E)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Wi-Fi Station
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wifi_status_card"),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                1.dp,
                                if (activeNet == "Wi-Fi" && activeWifiEnabled) Color(0xFF4F6630).copy(alpha = 0.5f) else Color(0xFFE1E4D9)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeNet == "Wi-Fi" && activeWifiEnabled) {
                                    Color(0xFFE1EAD3).copy(alpha = 0.4f)
                                } else {
                                    Color.White
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF3F4E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = "Wi-Fi",
                                            tint = if (activeWifiEnabled) Color(0xFF4F6630) else Color(0xFF74796D),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (activeWifiEnabled) Color(0xFF4F6630) else Color(0xFFD16A6A)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "ไวไฟ (Wi-Fi)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF74796D)
                                )
                                Text(
                                    text = if (activeWifiEnabled) activeSsid else "ปิดการใช้งาน",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C19),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "สัญญาณ: $activeWifiStrength%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F6630)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { activeWifiStrength / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (activeWifiStrength < wifiThreshold) Color(0xFFD16A6A) else Color(0xFF4F6630),
                                    trackColor = Color(0xFFF3F4E9)
                                )
                            }
                        }

                        // Cellular Station
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cellular_status_card"),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                1.dp,
                                if (activeNet == "Cellular" && activeCellEnabled) Color(0xFF4F6630).copy(alpha = 0.5f) else Color(0xFFE1E4D9)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeNet == "Cellular" && activeCellEnabled) {
                                    Color(0xFFE1EAD3).copy(alpha = 0.4f)
                                } else {
                                    Color.White
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF3F4E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SignalCellularAlt,
                                            contentDescription = "Cellular",
                                            tint = if (activeCellEnabled) Color(0xFF4F6630) else Color(0xFF74796D),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (activeCellEnabled) Color(0xFF4F6630) else Color(0xFFD16A6A)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "มือถือ (Cellular)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF74796D)
                                )
                                Text(
                                    text = if (activeCellEnabled) activeCarrier else "ปิดการใช้งาน",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C19),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "สัญญาณ: $activeCellStrength%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F6630)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { activeCellStrength / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (activeCellStrength < cellularThreshold) Color(0xFFD16A6A) else Color(0xFF4F6630),
                                    trackColor = Color(0xFFF3F4E9)
                                )
                            }
                        }
                    }
                }
            }

            // Sandbox Interactive Regulators (Simulated signals & Trigger buttons)
            if (isSimMode) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE1E4D9)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3F4E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Sandbox Controls",
                                        tint = Color(0xFF4F6630),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "ตู้ทดสอบสัญญาณ Sandbox",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF43493E)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            // Simulated Active Selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "เครือข่ายเชื่อมต่อหลักปัจจุบัน:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF74796D)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Wi-Fi", "Cellular", "None").forEach { net ->
                                        val isSelected = simActiveNet == net
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFFE1EAD3) else Color(0xFFF3F4E9))
                                                .clickable { viewModel.simulatedActiveNetwork.value = net }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = net,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF4F6630) else Color(0xFF5D6257)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE1E4D9))

                            // Radio State Switches
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = simWifiEnabled,
                                        onCheckedChange = { viewModel.simulatedWifiEnabled.value = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4F6630))
                                    )
                                    Text("จำลอง Wi-Fi เปิด", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1A1C19))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = simCellEnabled,
                                        onCheckedChange = { viewModel.simulatedCellularEnabled.value = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4F6630))
                                    )
                                    Text("จำลอง Mobile Data เปิด", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1A1C19))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // WiFi SSID Simulation Input
                            OutlinedTextField(
                                value = simWifiSsid,
                                onValueChange = { viewModel.simulatedWifiSsid.value = it },
                                label = { Text("ชื่อสัญญาณโมเดลที่จะตรวจ (SSID)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4F6630),
                                    unfocusedBorderColor = Color(0xFFE1E4D9)
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Slider: Wi-Fi Strength
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ระดับทราฟฟิคสัญญาณจำลอง Wi-Fi ($simWifiStrength%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF74796D)
                                    )
                                    if (simWifiStrength < wifiThreshold) {
                                        Text(
                                            text = "สัญญาณขัดข้อง (<$wifiThreshold%臨界)",
                                            color = Color(0xFFD16A6A),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = simWifiStrength.toFloat(),
                                    onValueChange = { viewModel.simulatedWifiStrength.value = it.toInt() },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF4F6630),
                                        activeTrackColor = Color(0xFF4F6630),
                                        inactiveTrackColor = Color(0xFFF3F4E9)
                                    )
                                )
                            }

                            // Slider: Cellular Strength
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ระดับทราฟฟิคสัญญาณจำลอง Cellular ($simCellStrength%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF74796D)
                                    )
                                    if (simCellStrength < cellularThreshold) {
                                        Text(
                                            text = "สัญญาณอับหมดท่า (<$cellularThreshold%)",
                                            color = Color(0xFFD16A6A),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = simCellStrength.toFloat(),
                                    onValueChange = { viewModel.simulatedCellularStrength.value = it.toInt() },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF4F6630),
                                        activeTrackColor = Color(0xFF4F6630),
                                        inactiveTrackColor = Color(0xFFF3F4E9)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Fault Trigger Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.triggerSimulatedWifiDrop() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD16A6A)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("simulate_wifi_drop"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Wifi,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("เน็ตไวไฟหลุด", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.triggerSimulatedCellularDrop() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD16A6A)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("simulate_cell_drop"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SignalCellularAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("เน็ตมือถือดับ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Controller Configuration panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE1E4D9)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Optimizer Config",
                                    tint = Color(0xFF4F6630),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "กำหนดเงื่อนไขและแบบจำลอง",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF43493E)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch: Turn Auto Optimizer On/Off
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "สลับสัญญาณอัตโนมัติ (Auto-Switch)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1A1C19)
                                )
                                Text(
                                    text = "สลับโครงข่ายที่เสถียรที่สุดเมื่อขาดการติดต่อ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF74796D)
                                )
                            }
                            Switch(
                                checked = isAutoSwitch,
                                onCheckedChange = { viewModel.toggleAutoSwitch(it) },
                                modifier = Modifier.testTag("auto_switch_toggle"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4F6630)
                                )
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE1E4D9))

                        // Switch: Notifications Enabled
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "แจ้งเตือนการปรับโครงข่าย",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1A1C19)
                                )
                                Text(
                                    text = "แจ้งหน้าจอเมื่อสวิตซ์ช่องสัญญาณเสร็จสิ้น",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF74796D)
                                )
                            }
                            Switch(
                                checked = isNotifications,
                                onCheckedChange = { viewModel.toggleNotifications(it) },
                                modifier = Modifier.testTag("notifications_toggle"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4F6630)
                                )
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE1E4D9))

                        // Config Slider: WiFi Signal Switching Threshold
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "เกณฑ์ Wi-Fi ขั้นต่ำในการย้าย ($wifiThreshold%)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1A1C19)
                                )
                                Text(
                                    text = "หากต่ำกว่าระบบจะสลับใช้ cellular",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F6630)
                                )
                            }
                            Slider(
                                value = wifiThreshold.toFloat(),
                                onValueChange = { viewModel.updateWifiThreshold(it.toInt()) },
                                valueRange = 10f..80f,
                                modifier = Modifier.testTag("wifi_threshold_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4F6630),
                                    activeTrackColor = Color(0xFF4F6630),
                                    inactiveTrackColor = Color(0xFFF3F4E9)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Config Slider: Cellular Signal Switching Threshold
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "เกณฑ์ Cellular ขั้นต่ำในการย้าย ($cellularThreshold%)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1A1C19)
                                )
                                Text(
                                    text = "หากต่ำกว่าระบบจะสลับใช้ Wi-Fi",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F6630)
                                )
                            }
                            Slider(
                                value = cellularThreshold.toFloat(),
                                onValueChange = { viewModel.updateCellularThreshold(it.toInt()) },
                                valueRange = 10f..80f,
                                modifier = Modifier.testTag("cellular_threshold_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4F6630),
                                    activeTrackColor = Color(0xFF4F6630),
                                    inactiveTrackColor = Color(0xFFF3F4E9)
                                )
                            )
                        }
                    }
                }
            }

            // Home Area WiFi Config Zone
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE1E4D9)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home Zones",
                                    tint = Color(0xFF4F6630),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "กำหนดพื้นที่บ้าน (Home Wi-Fi)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF43493E)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "เพิ่มสัญลักษณ์ชื่อ SSID สัญญาณอินเทอร์เน็ตบ้าน เมื่อตรวจพบชื่อคลื่นนี้หรือเมื่อโมบายสูญเสียคลื่น สเตชันรับเข้าจะเปลี่ยนกลับเข้าไวไฟอัตโนมัติ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF74796D),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Add fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newHomeSsid,
                                onValueChange = { newHomeSsid = it },
                                label = { Text("ชื่อ SSID (อย่างละเอียด)") },
                                placeholder = { Text("Home_WiFi_2.4G") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("home_ssid_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4F6630),
                                    unfocusedBorderColor = Color(0xFFE1E4D9)
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            OutlinedTextField(
                                value = newHomeLabel,
                                onValueChange = { newHomeLabel = it },
                                label = { Text("ป้ายชื่อ") },
                                placeholder = { Text("บ้าน") },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("home_label_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4F6630),
                                    unfocusedBorderColor = Color(0xFFE1E4D9)
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newHomeSsid.isNotBlank()) {
                                            viewModel.addHomeWifi(newHomeSsid, newHomeLabel.ifBlank { "บ้าน" })
                                            newHomeSsid = ""
                                            newHomeLabel = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Quick Add Connected SSID Auto Action
                            val currentSsid = if (isSimMode) simWifiSsid else realWifiSsid
                            val currentEnabled = if (isSimMode) simWifiEnabled else realWifiEnabled

                            Button(
                                onClick = {
                                    if (currentEnabled && currentSsid != "Disconnected" && currentSsid != "None") {
                                        viewModel.addHomeWifi(currentSsid, "เครือข่ายปัจจุบัน")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE1EAD3),
                                    contentColor = Color(0xFF4F6630)
                                ),
                                enabled = currentEnabled && currentSsid != "Disconnected" && currentSsid != "None" && currentSsid  != "Connected (Needs Permission)",
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("บันทึก SSID ปัจจุบัน", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (newHomeSsid.isNotBlank()) {
                                        viewModel.addHomeWifi(newHomeSsid, newHomeLabel.ifBlank { "บ้าน" })
                                        newHomeSsid = ""
                                        newHomeLabel = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F6630),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .testTag("add_home_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("เพิ่ม", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Home network lists
                        Spacer(modifier = Modifier.height(16.dp))
                        if (homeNetworks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4E9), shape = RoundedCornerShape(12.dp))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ยังไม่มีพื้นที่สัญญาณ Wi-Fi บ้านที่กำหนดไว้",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D6257)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                homeNetworks.forEach { network ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFE1E4D9), RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFBFDF8), shape = RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE1EAD3)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Home,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4F6630),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = network.ssid,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF1A1C19)
                                                )
                                                Text(
                                                    text = "ป้ายระบุ: ${network.label}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF74796D)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeHomeWifi(network.ssid) },
                                            modifier = Modifier.testTag("delete_home_${network.ssid}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "ลบ",
                                                tint = Color(0xFFD16A6A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Connection Switch Activity Log Terminal (Stored in SQLite)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE1E4D9)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3F4E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Logs indicator",
                                        tint = Color(0xFF4F6630),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "ประวัติการสลับสัญญาณอัตโนมัติ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF43493E)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE1EAD3))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${logs.size} บันทึก",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4F6630),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (logs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD1E4D9).copy(alpha = 0.8f),
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ยังคงไม่พบประวัติการสลับช่องสัญญาณเครือข่าย",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF74796D),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                logs.take(15).forEach { log ->
                                    val timeString = dateFormat.format(Date(log.timestamp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (log.isSimulation) Color(0xFFF3F4E9).copy(alpha = 0.6f) else Color(0xFFE1EAD3).copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, Color(0xFFE1E4D9).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "[$timeString]",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(end = 8.dp),
                                            color = Color(0xFF5D6257),
                                            fontWeight = FontWeight.Bold
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = when (log.eventType) {
                                                        "AUTO_SWITCH" -> "🔄 Dynamic Switch"
                                                        "SYSTEM" -> "⚙️ System Logic"
                                                        "WIFI" -> "📡 Wifi Config"
                                                        "CELLULAR" -> "📶 Mobile Network"
                                                        else -> "ℹ️ Update"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = when (log.eventType) {
                                                        "AUTO_SWITCH" -> Color(0xFF4F6630)
                                                        "SYSTEM" -> Color(0xFF5D6257)
                                                        else -> Color(0xFF43493E)
                                                    }
                                                )

                                                if (log.isSimulation) {
                                                    Text(
                                                        text = "SANDBOX",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF9E4E4E),
                                                        modifier = Modifier
                                                            .background(Color(0xFFFCE8E6), CircleShape)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = log.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF1A1C19),
                                                lineHeight = 15.sp
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
