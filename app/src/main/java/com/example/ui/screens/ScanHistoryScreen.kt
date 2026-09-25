package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.example.model.DiagnosisResult
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.*
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SecondaryTeal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    scanHistory: List<DiagnosisResult>,
    onSelectScan: (DiagnosisResult) -> Unit,
    onDeleteScan: (String) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Scan History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all past diagnostic reports stored on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = SafetyRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("scan_history_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Diagnostic Scan History", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        com.example.ui.components.GlassBackButton(onClick = onBack)
                    },
                    actions = {
                        if (scanHistory.isNotEmpty()) {
                            IconButton(onClick = { showClearDialog = true }) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = SafetyRed)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = NeutralDark,
                        navigationIconContentColor = NeutralDark,
                        actionIconContentColor = NeutralDark
                    )
                )
            }
        ) { innerPadding ->
            if (scanHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.70f)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryToggleOff,
                                contentDescription = null,
                                tint = PrimaryBlue.copy(alpha = 0.7f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Diagnostic History Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Completed scans will automatically save locally to Room DB.",
                                style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(scanHistory, key = { it.id }) { scan ->
                        HistoryItemCard(
                            scan = scan,
                            onClick = { onSelectScan(scan) },
                            onDelete = { onDeleteScan(scan.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    scan: DiagnosisResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(scan.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(scan.timestamp))
    }

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${scan.id}"),
        onClick = onClick,
        backgroundColor = Color.White.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!scan.photoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = scan.photoUri,
                    contentDescription = scan.applianceType,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${scan.brand} ${scan.applianceType}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scenario 7 Single-Line AI History Summary Format
                val summaryText = remember(scan) {
                    val fault = scan.likelyFaults.firstOrNull()?.fault ?: "Operational fault"
                    "${scan.applianceType}: $fault (Est. ₹${scan.costEstimate.minCost} - ₹${scan.costEstimate.maxCost})"
                }

                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NeutralDark.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "${scan.costEstimate.currency}${scan.costEstimate.minCost} - ${scan.costEstimate.currency}${scan.costEstimate.maxCost}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    if (scan.safetyWarning != null) {
                        Text(
                            text = "⚠️ Safety Hazard",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SafetyRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete scan",
                    tint = NeutralMedium
                )
            }
        }
    }
}
