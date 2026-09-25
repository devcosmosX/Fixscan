package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.*
import com.example.util.NavigationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultsScreen(
    diagnosis: DiagnosisResult,
    onOpenChat: () -> Unit,
    onNewScan: () -> Unit,
    onOpenMechanicsMap: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var sourcesExpanded by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    FrostedBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("scan_results_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "${diagnosis.brand} ${diagnosis.applianceType}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Diagnostic Report",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    },
                    navigationIcon = {
                        com.example.ui.components.GlassBackButton(onClick = onBack)
                    },
                    actions = {
                        IconButton(onClick = onNewScan) {
                            Icon(Icons.Default.Refresh, contentDescription = "New Scan")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = NeutralDark,
                        navigationIconContentColor = NeutralDark,
                        actionIconContentColor = NeutralDark
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onOpenChat,
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    text = { Text("Ask FixScan AI Chat", fontWeight = FontWeight.Bold) },
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .testTag("open_chat_fab")
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
            ) {
                // 3D Hero Render (Swappable based on appliance type)
                item {
                    val heroResource = when {
                        diagnosis.applianceType.contains("Fridge", ignoreCase = true) || diagnosis.applianceType.contains("Refrigerator", ignoreCase = true) -> R.drawable.appliance_fridge_1785410044523
                        diagnosis.applianceType.contains("Washing", ignoreCase = true) -> R.drawable.appliance_washing_machine_1785410057629
                        diagnosis.applianceType.contains("AC", ignoreCase = true) || diagnosis.applianceType.contains("Air", ignoreCase = true) -> R.drawable.appliance_ac_1785410072336
                        diagnosis.applianceType.contains("Microwave", ignoreCase = true) -> R.drawable.appliance_microwave_1785410082877
                        else -> R.drawable.appliance_fridge_1785410044523 // fallback
                    }

                    val infiniteTransition = rememberInfiniteTransition()
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = -12f,
                        targetValue = 12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3500, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = heroResource),
                            contentDescription = "3D Render of ${diagnosis.applianceType}",
                            modifier = Modifier
                                .size(240.dp)
                                .offset(y = offsetY.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // PROMINENT SAFETY WARNING BANNER (Frosted Red)
                if (diagnosis.safetyWarning != null) {
                    item {
                        val safety = diagnosis.safetyWarning
                        FrostedGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("safety_warning_banner"),
                            backgroundColor = Color(0x33EF4444),
                            borderColor = SafetyRed,
                            elevation = 6.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SafetyRed,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Safety Hazard",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (safety.isCritical) "CRITICAL SAFETY HAZARD" else "SAFETY PRECAUTION",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SafetyRed
                                            )
                                        )
                                        Text(
                                            text = "Risk detected by AI diagnostic engine",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SafetyRed.copy(alpha = 0.9f))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = safety.warningText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeutralDark
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                FrostedGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = SafetyRed.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ACTION REQUIRED: ${safety.recommendedAction}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SafetyRed
                                        ),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD 1: DIAGNOSIS CARD (Likely Faults & Confidence)
                item {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diagnosis_card"),
                        backgroundColor = Color.White.copy(alpha = 0.75f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Diagnosed Likely Faults",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NeutralDark
                                        )
                                    )
                                }

                                FrostedGlassSurface(
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = PrimaryBlue.copy(alpha = 0.12f),
                                    borderColor = PrimaryBlue.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "${diagnosis.likelyFaults.size} Faults Found",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            diagnosis.likelyFaults.forEachIndexed { index, fault ->
                                if (index > 0) HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${fault.fault}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = NeutralDark
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        ConfidenceChip(confidence = fault.confidence)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = fault.explanation,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = NeutralMedium,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD 2: COST ESTIMATE CARD
                item {
                    val cost = diagnosis.costEstimate
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cost_estimate_card"),
                        backgroundColor = Color.White.copy(alpha = 0.75f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Estimated Repair Cost",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeutralDark
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FrostedGlassSurface(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = SecondaryTeal.copy(alpha = 0.12f),
                                borderColor = SecondaryTeal.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Estimated Price Range",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SecondaryTeal, fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${cost.currency}${cost.minCost} - ${cost.currency}${cost.maxCost}",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SecondaryTeal
                                            )
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SecondaryTeal
                                        ) {
                                            Text(
                                                text = "Search Grounded",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = cost.basisNote,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeutralMedium
                                )
                            )

                            if (cost.sources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { sourcesExpanded = !sourcesExpanded }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "View Search Sources (${cost.sources.size})",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Icon(
                                        imageVector = if (sourcesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand sources",
                                        tint = PrimaryBlue
                                    )
                                }

                                AnimatedVisibility(visible = sourcesExpanded) {
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        cost.sources.forEach { src ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(src.url))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        // ignore fallback
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = src.title,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = PrimaryBlue,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // CARD 3: NEARBY MECHANICS CARD
                item {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nearby_mechanics_card"),
                        backgroundColor = Color.White.copy(alpha = 0.75f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Nearby Repair Mechanics",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NeutralDark
                                        )
                                    )
                                }

                                Text(
                                    text = "Maps Places API",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action to open full interactive Google Map with navigation
                            FrostedGlassButton(
                                onClick = onOpenMechanicsMap,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                containerColor = SecondaryTeal
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Open Live Google Map & Navigation",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            diagnosis.mechanics.forEachIndexed { idx, mechanic ->
                                if (idx > 0) HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )

                                MechanicItemRow(
                                    mechanic = mechanic,
                                    onCall = {
                                        NavigationHelper.callMechanic(context, mechanic.phone)
                                    },
                                    onGetDirections = {
                                        NavigationHelper.startNavigation(
                                            context = context,
                                            latitude = mechanic.latitude,
                                            longitude = mechanic.longitude,
                                            destinationName = mechanic.name
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // ADMOB BANNER RESERVED SPACE
                item {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("admob_banner_reserved_space"),
                        backgroundColor = Color.White.copy(alpha = 0.5f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📢 Ad Space Reserved (AdMob Banner)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeutralMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfidenceChip(confidence: Confidence) {
    val (bgColor, textColor) = when (confidence) {
        Confidence.MOST_LIKELY -> SafetyRed.copy(alpha = 0.15f) to SafetyRed
        Confidence.POSSIBLE -> AccentAmber.copy(alpha = 0.15f) to AccentAmber
        Confidence.LESS_LIKELY -> PrimaryBlue.copy(alpha = 0.15f) to PrimaryBlue
    }

    FrostedGlassSurface(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = bgColor,
        borderColor = textColor.copy(alpha = 0.3f)
    ) {
        Text(
            text = confidence.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MechanicItemRow(
    mechanic: Mechanic,
    onCall: () -> Unit,
    onGetDirections: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mechanic.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    )
                    if (mechanic.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = mechanic.address,
                    style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " ${mechanic.rating} (${mechanic.reviewCount})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    )
                }
                Text(
                    text = "📍 ${mechanic.distanceKm} km away",
                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryTeal, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrostedGlassButton(
                onClick = onCall,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("call_mechanic_${mechanic.id}"),
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call Mechanic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onGetDirections,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .testTag("directions_mechanic_${mechanic.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
