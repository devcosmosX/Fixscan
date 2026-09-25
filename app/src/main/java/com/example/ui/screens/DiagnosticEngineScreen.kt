package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ApplianceDiagnosticAssessment
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.*
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticEngineScreen(
    assessment: ApplianceDiagnosticAssessment?,
    isAnalyzing: Boolean,
    onAnalyzePhotos: (String?, String?) -> Unit,
    onBack: () -> Unit
) {
    var appliancePhotoUri by remember { mutableStateOf<String?>(null) }
    var damagePhotoUri by remember { mutableStateOf<String?>(null) }

    val applianceImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        appliancePhotoUri = uri?.toString()
    }

    val damageImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        damagePhotoUri = uri?.toString()
    }

    FrostedBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Appliance Diagnostic Engine",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                            )
                            Text(
                                text = "Structured Visual & Damage Analyzer",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeutralDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.testTag("diagnostic_engine_screen")
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo Input Selectors Section
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Submit Appliance Photos",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Appliance Image Box (Required)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.08f))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!appliancePhotoUri.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = appliancePhotoUri,
                                            contentDescription = "Appliance Photo",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            IconButton(onClick = { applianceImagePicker.launch("image/*") }) {
                                                Icon(
                                                    imageVector = Icons.Default.AddAPhoto,
                                                    contentDescription = "Select Appliance Photo",
                                                    tint = PrimaryBlue
                                                )
                                            }
                                            Text(
                                                text = "Appliance Photo\n(Required)",
                                                fontSize = 11.sp,
                                                color = NeutralDark.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Damage Image Box (Optional)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryTeal.copy(alpha = 0.08f))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!damagePhotoUri.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = damagePhotoUri,
                                            contentDescription = "Damage Part Photo",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            IconButton(onClick = { damageImagePicker.launch("image/*") }) {
                                                Icon(
                                                    imageVector = Icons.Default.BrokenImage,
                                                    contentDescription = "Select Damaged Part Photo",
                                                    tint = SecondaryTeal
                                                )
                                            }
                                            Text(
                                                text = "Damaged Part\n(Optional)",
                                                fontSize = 11.sp,
                                                color = NeutralDark.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onAnalyzePhotos(appliancePhotoUri, damagePhotoUri) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing Images...")
                                } else {
                                    Icon(imageVector = Icons.Default.CenterFocusWeak, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Run AI Diagnostic Assessment")
                                }
                            }
                        }
                    }
                }

                // Visual Diagnostic Results Section
                if (assessment != null) {
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = assessment.appliance.type,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = NeutralDark
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (assessment.appliance.confidence.lowercase()) {
                                            "high" -> SecondaryTeal.copy(alpha = 0.2f)
                                            "medium" -> PrimaryBlue.copy(alpha = 0.2f)
                                            else -> SafetyRed.copy(alpha = 0.2f)
                                        }
                                    ) {
                                        Text(
                                            text = "Confidence: ${assessment.appliance.confidence}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Step 2 Damage Detection
                                Text(
                                    text = "Damage Detection Assessment",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Component: ${assessment.damage.component}", style = MaterialTheme.typography.bodyMedium.copy(color = NeutralDark))
                                Text(text = "Type: ${assessment.damage.damageType}", style = MaterialTheme.typography.bodyMedium.copy(color = NeutralDark))
                                Text(text = "Severity: ${assessment.damage.severity}", style = MaterialTheme.typography.bodyMedium.copy(color = NeutralDark), fontWeight = FontWeight.Bold)
                                Text(text = assessment.damage.description, style = MaterialTheme.typography.bodySmall, color = NeutralDark.copy(alpha = 0.8f))

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Step 3 Cost Estimate
                                Text(
                                    text = "Repair Cost Estimate (INR)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SecondaryTeal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${assessment.costEstimate.min} - ₹${assessment.costEstimate.max}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = SecondaryTeal)
                                )
                                Text(text = assessment.costEstimate.basis, style = MaterialTheme.typography.bodySmall, color = NeutralDark.copy(alpha = 0.8f))

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Notes: ${assessment.notes}", style = MaterialTheme.typography.labelMedium, color = NeutralMedium)
                            }
                        }
                    }

                    // Raw JSON Schema Output Display
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Raw Valid Output JSON Schema",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val jsonStr = remember(assessment) {
                                    val obj = JSONObject().apply {
                                        put("appliance", JSONObject().apply {
                                            put("type", assessment.appliance.type)
                                            put("confidence", assessment.appliance.confidence)
                                        })
                                        put("damage", JSONObject().apply {
                                            put("detected", assessment.damage.detected)
                                            put("component", assessment.damage.component)
                                            put("damage_type", assessment.damage.damageType)
                                            put("severity", assessment.damage.severity)
                                            put("description", assessment.damage.description)
                                        })
                                        put("cost_estimate", JSONObject().apply {
                                            put("currency", assessment.costEstimate.currency)
                                            put("min", assessment.costEstimate.min)
                                            put("max", assessment.costEstimate.max)
                                            put("basis", assessment.costEstimate.basis)
                                        })
                                        put("notes", assessment.notes)
                                    }
                                    obj.toString(2)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NeutralDark)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = jsonStr,
                                        color = Color(0xFF64FFDA),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
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
