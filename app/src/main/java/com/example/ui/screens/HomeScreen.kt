package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ApplianceType
import com.example.model.DiagnosisResult
import com.example.model.UserSession
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.*

import com.example.model.PaywallTriggerReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userSession: UserSession,
    scanHistory: List<DiagnosisResult>,
    usageStatusCopy: String = "",
    accountWelcomeCopy: String = "",
    onStartNewScan: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenScanDetail: (DiagnosisResult) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenSignIn: () -> Unit,
    onOpenBillingAssistant: () -> Unit = {},
    onOpenDiagnosticEngine: () -> Unit = {},
    onOpenPaywall: (PaywallTriggerReason) -> Unit = {},
    onOpenMechanicsMap: () -> Unit = {}
) {
    var isAutoMode by remember { mutableStateOf(true) }

    FrostedBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "FixScan Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FixScan",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeutralDark)
                                )
                                Text(
                                    text = "Frosted Glass Diagnostics AI",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onOpenMechanicsMap,
                            modifier = Modifier.testTag("map_top_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Nearby Mechanics Map",
                                tint = PrimaryBlue
                            )
                        }

                        IconButton(
                            onClick = onOpenDiagnosticEngine,
                            modifier = Modifier.testTag("diagnostic_engine_top_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusWeak,
                                contentDescription = "Visual Diagnostic Engine",
                                tint = PrimaryBlue
                            )
                        }

                        IconButton(
                            onClick = onOpenBillingAssistant,
                            modifier = Modifier.testTag("billing_top_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Billing Assistant Playground",
                                tint = SecondaryTeal
                            )
                        }

                        IconButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.testTag("history_top_icon")
                        ) {
                            BadgedBox(badge = {
                                if (scanHistory.isNotEmpty()) {
                                    Badge(containerColor = PrimaryBlue) { Text(scanHistory.size.toString()) }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History"
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (userSession.isLoggedIn) {
                                    onOpenDashboard()
                                } else {
                                    onOpenDashboard()
                                }
                            },
                            modifier = Modifier.testTag("account_top_icon")
                        ) {
                            if (userSession.isLoggedIn) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initial = userSession.userName.firstOrNull()?.uppercase() ?: "U"
                                        Text(
                                            text = initial,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Account Dashboard",
                                    tint = NeutralDark
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = NeutralDark,
                        actionIconContentColor = NeutralDark
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Unified Billing Assistant Status Banner (Scenarios 1 & 2)
                item {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPaywall(PaywallTriggerReason.BROWSING_PLANS) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SecondaryTeal.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = SecondaryTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (usageStatusCopy.isNotBlank()) usageStatusCopy else "5 scans remaining on Free Plan.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeutralDark
                                    )
                                )
                                if (accountWelcomeCopy.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = accountWelcomeCopy,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeutralDark.copy(alpha = 0.7f)
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryBlue,
                                modifier = Modifier.clickable { onOpenPaywall(PaywallTriggerReason.BROWSING_PLANS) }
                            ) {
                                Text(
                                    text = "Upgrade",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // New Hero Section based on image design
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Diagnose Broken.\nRepair Smarter.",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = NeutralDark,
                                lineHeight = 44.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Intelligent appliance diagnosis with real-time feedback.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = NeutralDark.copy(alpha = 0.8f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // Floating animated pills Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedFloatingPill(
                                text = "Current Status: Ready",
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(x = (-20).dp),
                                delayMillis = 0,
                                durationMillis = 3000
                            )
                            
                            AnimatedFloatingPill(
                                text = "AI Accuracy: 95%",
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(y = 20.dp),
                                delayMillis = 1000,
                                durationMillis = 3500
                            )
                            
                            AnimatedFloatingPill(
                                text = "Cost Est: INSTANT",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(y = (-10).dp),
                                delayMillis = 500,
                                durationMillis = 2800
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        // Polished MVP Segmented Control for Mode Selection (⚡ Auto vs 🖐️ Manual)
                        FrostedGlassCard(
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color.White.copy(alpha = 0.85f),
                            borderColor = Color.White,
                            elevation = 5.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DIAGNOSTIC MODE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeutralDark,
                                                letterSpacing = 1.1.sp
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isAutoMode) BrightCyan.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (isAutoMode) "⚡ AI Vision Active" else "🖐️ Step-by-Step",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAutoMode) Color(0xFF0D5E63) else Color(0xFF9E6500),
                                                fontSize = 10.5.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Interactive Segmented Pill Bar
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = NeutralMedium.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Manual Option
                                        Surface(
                                            onClick = { isAutoMode = false },
                                            shape = RoundedCornerShape(11.dp),
                                            color = if (!isAutoMode) Color.White else Color.Transparent,
                                            shadowElevation = if (!isAutoMode) 3.dp else 0.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🖐️  Manual",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = if (!isAutoMode) FontWeight.ExtraBold else FontWeight.Medium,
                                                        color = if (!isAutoMode) NeutralDark else NeutralMedium,
                                                        fontSize = 13.5.sp
                                                    )
                                                )
                                            }
                                        }

                                        // Auto Option
                                        Surface(
                                            onClick = { isAutoMode = true },
                                            shape = RoundedCornerShape(11.dp),
                                            color = if (isAutoMode) PrimaryBlue else Color.Transparent,
                                            shadowElevation = if (isAutoMode) 4.dp else 0.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⚡  Auto",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = if (isAutoMode) FontWeight.ExtraBold else FontWeight.Medium,
                                                        color = if (isAutoMode) Color.White else NeutralMedium,
                                                        fontSize = 13.5.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Descriptive Subtitle for active mode
                                Text(
                                    text = if (isAutoMode)
                                        "⚡ Auto Mode: Point camera at any appliance for instant AI object detection, fault inference & cost breakdown."
                                    else
                                        "🖐️ Manual Mode: Pick your appliance model, brand, age, and symptoms manually with guided diagnostic forms.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NeutralDark.copy(alpha = 0.75f),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        // Get Started / Action Launch Button
                        FrostedGlassButton(
                            onClick = {
                                if (isAutoMode) {
                                    onStartNewScan()
                                } else {
                                    onSelectCategory("Refrigerator")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("scan_broken_button"),
                            containerColor = if (isAutoMode) Color(0xFF0F3A3D) else PrimaryBlue
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isAutoMode) Icons.Default.CameraAlt else Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isAutoMode) "⚡ Start AI Auto Scan" else "🖐️ Start Manual Diagnosis",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                                Text(">>>", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = BrightCyan))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nearby Mechanics Map Button
                        OutlinedButton(
                            onClick = onOpenMechanicsMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Find Nearby Mechanics & Repair (Live Map)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                
                // Keep the Categories Section but update text styling slightly
                item {
                    Column {
                        Text(
                            text = "Supported Appliances",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeutralDark),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(ApplianceType.values().toList()) { category ->
                                FrostedGlassSurface(
                                    shape = RoundedCornerShape(14.dp),
                                    backgroundColor = Color.White.copy(alpha = 0.70f),
                                    borderColor = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier
                                        .clickable { onSelectCategory(category.displayName) }
                                        .testTag("category_chip_${category.name}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = getApplianceIcon(category),
                                            contentDescription = category.displayName,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = category.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = NeutralDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Past Scans / Recent Diagnostic History
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Diagnostic History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeutralDark)
                        )

                        if (scanHistory.isNotEmpty()) {
                            TextButton(
                                onClick = onOpenHistory,
                                modifier = Modifier.testTag("view_all_history_button")
                            ) {
                                Text(text = "View All (${scanHistory.size})", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (scanHistory.isEmpty()) {
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.60f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = PrimaryBlue.copy(alpha = 0.7f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No past diagnostic scans yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tap 'Scan Appliance Now' above to diagnose your fridge, washing machine, AC, or microwave.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(scanHistory.take(3)) { scan ->
                        RecentScanCard(
                            scan = scan,
                            onClick = { onOpenScanDetail(scan) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentScanCard(scan: DiagnosisResult, onClick: () -> Unit) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_scan_card_${scan.id}"),
        onClick = onClick,
        backgroundColor = Color.White.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
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
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NeutralDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (scan.safetyWarning != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SafetyRedLight
                        ) {
                            Text(
                                text = "⚠️ Safety Alert",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SafetyRed,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Top Fault: ${scan.likelyFaults.firstOrNull()?.fault ?: "Diagnostic Scan"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Est. Repair: ${scan.costEstimate.currency}${scan.costEstimate.minCost} - ${scan.costEstimate.currency}${scan.costEstimate.maxCost}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SecondaryTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = NeutralMedium
            )
        }
    }
}

fun getApplianceIcon(type: ApplianceType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        ApplianceType.FRIDGE -> Icons.Default.Kitchen
        ApplianceType.WASHING_MACHINE -> Icons.Default.LocalLaundryService
        ApplianceType.AIR_CONDITIONER -> Icons.Default.AcUnit
        ApplianceType.MICROWAVE -> Icons.Default.Microwave
        ApplianceType.MIXER_GRINDER -> Icons.Default.Blender
        ApplianceType.WATER_PURIFIER -> Icons.Default.WaterDrop
        ApplianceType.TV -> Icons.Default.Tv
        ApplianceType.OTHER -> Icons.Default.Devices
    }
}

@Composable
fun AnimatedFloatingPill(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 3000
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    FrostedGlassSurface(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color.White.copy(alpha = 0.5f),
        borderColor = Color.White.copy(alpha = 0.7f),
        modifier = modifier.offset(y = offsetY.dp).shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = SecondaryTeal, spotColor = SecondaryTeal)
    ) {
        Text(
            text = text,
            color = NeutralDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}
