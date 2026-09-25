package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DiagnosisResult
import com.example.model.PaywallTriggerReason
import com.example.model.UserSession
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.components.GlassBackButton
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    userSession: UserSession,
    scanHistory: List<DiagnosisResult>,
    onBack: () -> Unit,
    onStartNewScan: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenScanDetail: (DiagnosisResult) -> Unit,
    onOpenBillingAssistant: () -> Unit,
    onOpenDiagnosticEngine: () -> Unit,
    onOpenMechanicsMap: () -> Unit,
    onOpenPaywall: (PaywallTriggerReason) -> Unit,
    onSignOut: () -> Unit,
    onSignIn: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Activity, 2: Settings
    val tabTitles = listOf("Overview", "Activity", "Settings")

    FrostedBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("user_dashboard_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (userSession.isLoggedIn) "Account Dashboard" else "Guest Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                        )
                    },
                    navigationIcon = {
                        GlassBackButton(onClick = onBack)
                    },
                    actions = {
                        if (userSession.isLoggedIn) {
                            IconButton(onClick = onSignOut) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = SafetyRed
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = NeutralDark
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
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // ── 1. Hero User Profile Card ──────────────────────────────
                item {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Avatar circle with initials or icon
                                Surface(
                                    shape = CircleShape,
                                    color = if (userSession.isLoggedIn) PrimaryBlue else SecondaryTeal.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .size(68.dp)
                                        .shadow(6.dp, CircleShape, ambientColor = PrimaryBlue, spotColor = PrimaryBlue)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (userSession.isLoggedIn) {
                                            val initials = userSession.userName
                                                .split(" ")
                                                .filter { it.isNotBlank() }
                                                .take(2)
                                                .map { it.first().uppercase() }
                                                .joinToString("")
                                                .ifBlank { "U" }
                                            Text(
                                                text = initials,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    fontSize = 24.sp
                                                )
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "Guest",
                                                tint = Color.White,
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (userSession.isLoggedIn) userSession.userName else "Guest Explorer",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NeutralDark,
                                                fontSize = 18.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (userSession.isLoggedIn) AccentGreen.copy(alpha = 0.15f) else AccentAmber.copy(alpha = 0.15f),
                                            border = BorderStroke(
                                                0.8.dp,
                                                if (userSession.isLoggedIn) AccentGreen else AccentAmber
                                            )
                                        ) {
                                            Text(
                                                text = if (userSession.isLoggedIn) "Verified" else "Guest",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (userSession.isLoggedIn) AccentGreen else AccentAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (userSession.isLoggedIn && userSession.userEmail.isNotBlank())
                                            userSession.userEmail
                                        else
                                            "Local session • Not synced with Cloud",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = NeutralMedium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = PrimaryBlue.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Free Tier",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = PrimaryBlue,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SecondaryTeal.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = null,
                                                    tint = SecondaryTeal,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${userSession.scansRemaining} Scans Left",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = SecondaryTeal,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (!userSession.isLoggedIn) {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Sync scans across all devices?",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = NeutralDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Button(
                                        onClick = onSignIn,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Sign In / Register",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 2. Segmented Navigation Tabs ───────────────────────────
                item {
                    FrostedGlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color.White.copy(alpha = 0.6f),
                        borderColor = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                val selected = selectedTab == index
                                Surface(
                                    onClick = { selectedTab = index },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) PrimaryBlue else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) Color.White else NeutralDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 3. Tab Content ─────────────────────────────────────────
                when (selectedTab) {
                    0 -> {
                        // ── Overview Tab ──
                        item {
                            DashboardStatsRow(
                                scanHistory = scanHistory,
                                userSession = userSession
                            )
                        }

                        item {
                            QuickActionsGrid(
                                onStartNewScan = onStartNewScan,
                                onOpenDiagnosticEngine = onOpenDiagnosticEngine,
                                onOpenMechanicsMap = onOpenMechanicsMap,
                                onOpenBillingAssistant = onOpenBillingAssistant
                            )
                        }

                        item {
                            PlanUpgradeBanner(
                                userSession = userSession,
                                onUpgrade = { onOpenPaywall(PaywallTriggerReason.BROWSING_PLANS) }
                            )
                        }

                        item {
                            RecentActivityHeader(onSeeAll = onOpenHistory)
                        }

                        if (scanHistory.isEmpty()) {
                            item {
                                EmptyActivityCard(onStartNewScan = onStartNewScan)
                            }
                        } else {
                            items(scanHistory.take(3)) { diag ->
                                RecentDiagnosisCard(
                                    diagnosis = diag,
                                    onClick = { onOpenScanDetail(diag) }
                                )
                            }
                        }
                    }

                    1 -> {
                        // ── Activity Tab ──
                        item {
                            Text(
                                text = "Full Diagnostic History (${scanHistory.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                ),
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        if (scanHistory.isEmpty()) {
                            item {
                                EmptyActivityCard(onStartNewScan = onStartNewScan)
                            }
                        } else {
                            items(scanHistory) { diag ->
                                RecentDiagnosisCard(
                                    diagnosis = diag,
                                    onClick = { onOpenScanDetail(diag) }
                                )
                            }
                        }
                    }

                    2 -> {
                        // ── Settings / Account Management Tab ──
                        item {
                            SettingsSection(
                                userSession = userSession,
                                onSignOut = onSignOut,
                                onSignIn = onSignIn,
                                onOpenBilling = onOpenBillingAssistant,
                                onUpgrade = { onOpenPaywall(PaywallTriggerReason.BROWSING_PLANS) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subcomponents
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardStatsRow(
    scanHistory: List<DiagnosisResult>,
    userSession: UserSession
) {
    val criticalCount = scanHistory.count { it.safetyWarning?.isCritical == true }
    val totalEstSavings = scanHistory.sumOf {
        val diff = (it.costEstimate.maxCost - it.costEstimate.minCost)
        if (diff > 0) diff else 15
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Scanner,
            iconTint = PrimaryBlue,
            value = scanHistory.size.toString(),
            label = "Total Scans",
            bgColor = PrimaryBlue.copy(alpha = 0.1f)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Shield,
            iconTint = if (criticalCount > 0) SafetyRed else AccentGreen,
            value = criticalCount.toString(),
            label = "Hazards Flagged",
            bgColor = if (criticalCount > 0) SafetyRed.copy(alpha = 0.12f) else AccentGreen.copy(alpha = 0.12f)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Savings,
            iconTint = SecondaryTeal,
            value = "$$totalEstSavings",
            label = "Est. Saved",
            bgColor = SecondaryTeal.copy(alpha = 0.12f)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    bgColor: Color
) {
    FrostedGlassCard(
        modifier = modifier,
        backgroundColor = Color.White.copy(alpha = 0.8f),
        borderColor = Color.White.copy(alpha = 0.9f),
        elevation = 3.dp,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = NeutralDark,
                    fontSize = 20.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NeutralMedium,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onStartNewScan: () -> Unit,
    onOpenDiagnosticEngine: () -> Unit,
    onOpenMechanicsMap: () -> Unit,
    onOpenBillingAssistant: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Quick Launchpad",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            ),
            modifier = Modifier.padding(start = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "AI Scan",
                subtitle = "Camera capture",
                icon = Icons.Default.CameraAlt,
                tint = PrimaryBlue,
                onClick = onStartNewScan
            )
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "Visual Engine",
                subtitle = "Multimodal breakdown",
                icon = Icons.Default.CenterFocusWeak,
                tint = SecondaryTeal,
                onClick = onOpenDiagnosticEngine
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "Local Mechanics",
                subtitle = "Maps & live quotes",
                icon = Icons.Default.Place,
                tint = AccentAmber,
                onClick = onOpenMechanicsMap
            )
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "Billing Assistant",
                subtitle = "Paywall & plans",
                icon = Icons.Default.AutoAwesome,
                tint = Color(0xFF7C5CD8),
                onClick = onOpenBillingAssistant
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    FrostedGlassCard(
        modifier = modifier,
        backgroundColor = Color.White.copy(alpha = 0.82f),
        elevation = 3.dp,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeutralMedium,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlanUpgradeBanner(
    userSession: UserSession,
    onUpgrade: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUpgrade),
        backgroundColor = Color(0xFF0F3A3D).copy(alpha = 0.92f),
        borderColor = SecondaryTeal.copy(alpha = 0.5f),
        elevation = 6.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = BrightCyan.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = BrightCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Upgrade to Pro Unlimited",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Unlimited AI diagnostics & priority mechanic dispatch",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrightCyan,
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = "Upgrade",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F3A3D)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentActivityHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Scans",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "View All →",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            )
        }
    }
}

@Composable
private fun EmptyActivityCard(onStartNewScan: () -> Unit) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.75f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = NeutralMedium,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No diagnostic scans yet",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
            )
            Text(
                text = "Snap a photo of any broken home appliance to see your first instant diagnosis.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NeutralMedium,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            FrostedGlassButton(
                onClick = onStartNewScan,
                containerColor = PrimaryBlue,
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Start First Scan",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun RecentDiagnosisCard(
    diagnosis: DiagnosisResult,
    onClick: () -> Unit
) {
    val dateString = remember(diagnosis.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(diagnosis.timestamp))
    }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.82f),
        elevation = 3.dp,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Appliance icon container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SecondaryTeal.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            diagnosis.applianceType.contains("Fridge", ignoreCase = true) || diagnosis.applianceType.contains("Refrigerator", ignoreCase = true) -> Icons.Default.Kitchen
                            diagnosis.applianceType.contains("Wash", ignoreCase = true) -> Icons.Default.LocalLaundryService
                            diagnosis.applianceType.contains("Micro", ignoreCase = true) || diagnosis.applianceType.contains("Oven", ignoreCase = true) -> Icons.Default.Microwave
                            else -> Icons.Default.Build
                        },
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(26.dp)
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
                        text = "${diagnosis.brand} ${diagnosis.applianceType}".trim(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "$${diagnosis.costEstimate.minCost}-$${diagnosis.costEstimate.maxCost}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = diagnosis.likelyFaults.firstOrNull()?.fault ?: diagnosis.symptoms.firstOrNull() ?: "Diagnostic scan completed",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NeutralMedium,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeutralMedium.copy(alpha = 0.8f),
                            fontSize = 10.5.sp
                        )
                    )

                    if (diagnosis.safetyWarning?.isCritical == true) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SafetyRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "⚠️ Hazard",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SafetyRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = NeutralMedium,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    userSession: UserSession,
    onSignOut: () -> Unit,
    onSignIn: () -> Unit,
    onOpenBilling: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Account & Preferences",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            ),
            modifier = Modifier.padding(start = 2.dp)
        )

        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.85f),
            elevation = 4.dp,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsItemRow(
                    icon = Icons.Default.Person,
                    title = "Profile Information",
                    subtitle = if (userSession.isLoggedIn) userSession.userName else "Guest User",
                    onClick = {}
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                SettingsItemRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Subscription & Usage",
                    subtitle = "${userSession.scansRemaining} free diagnostic scans remaining",
                    trailingText = "Manage",
                    onClick = onOpenBilling
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                SettingsItemRow(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Scan results & login hazard alerts",
                    onClick = {}
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                SettingsItemRow(
                    icon = Icons.Default.Security,
                    title = "Data & Privacy",
                    subtitle = "Encrypted Firebase Firestore storage",
                    onClick = {}
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                SettingsItemRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help & Support",
                    subtitle = "Appliance diagnostic guides & FAQs",
                    onClick = {}
                )
            }
        }

        // Sign In / Out Button
        if (userSession.isLoggedIn) {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyRed),
                border = BorderStroke(1.dp, SafetyRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = SafetyRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out from FixScan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SafetyRed
                    )
                )
            }
        } else {
            FrostedGlassButton(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign In / Create Account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Text(
            text = "FixScan Version 1.0 (Build 36) • Powered by Gemini AI & Firebase",
            style = MaterialTheme.typography.labelSmall.copy(
                color = NeutralMedium,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = NeutralDark
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecondaryTeal
                ),
                modifier = Modifier.padding(end = 6.dp)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = NeutralMedium.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}
