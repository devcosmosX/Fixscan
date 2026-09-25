package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApplianceType
import com.example.model.Symptom
import com.example.model.VisionDetectionResult
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.*

@Composable
fun StepHeader(stepNumber: Int, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = PrimaryBlue,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stepNumber.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GuidedFormScreen(
    applianceType: String,
    brand: String,
    approxAge: String,
    selectedSymptoms: Set<String>,
    note: String,
    photoUri: String?,
    visionResult: VisionDetectionResult? = null,
    isAnalyzing: Boolean,
    errorMessage: String? = null,
    onApplianceTypeChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onApproxAgeChanged: (String) -> Unit,
    onSymptomToggled: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onChangePhoto: () -> Unit,
    onSubmitScan: () -> Unit,
    onBack: () -> Unit
) {
    val ageOptions = listOf("<1yr", "1-3yr", "3-7yr", "7+yr")

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFE6F5F4),
            Color(0xFFC5EBE6)
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("guided_form_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Appliance Symptom Checklist", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        com.example.ui.components.GlassBackButton(onClick = onBack)
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
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)) {
                            Text(
                                text = "Help AI Diagnose",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "The more details you provide, the more accurate the diagnosis and repair estimate.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = NeutralDark)
                            )
                        }
                    }

                    // Photo indicator header
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.75f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = if (photoUri != null) SecondaryTeal.copy(alpha = 0.15f) else NeutralMedium.copy(alpha = 0.1f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (photoUri != null) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                                                contentDescription = null,
                                                tint = if (photoUri != null) SecondaryTeal else NeutralMedium,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (photoUri != null) "Photo Attached" else "No Photo",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NeutralDark
                                            )
                                        )
                                        Text(
                                            text = if (photoUri != null) "AI will evaluate visual symptoms" else "AI relies on checklist",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium)
                                        )

                                        if (visionResult != null && visionResult.objects.isNotEmpty()) {
                                            val primary = visionResult.primaryObject
                                            if (primary != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = PrimaryLight,
                                                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                                                    modifier = Modifier.clickable {
                                                        onApplianceTypeChanged(primary.name)
                                                    }
                                                ) {
                                                    Text(
                                                        text = "🔍 Detected: ${primary.name} (${primary.confidencePercentage}%) • Tap to apply",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = PrimaryBlue,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = onChangePhoto,
                                    modifier = Modifier.testTag("change_photo_form_button")
                                ) {
                                    Text(if (photoUri != null) "Change" else "Add Photo", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 1. Appliance Type Selector
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.75f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                StepHeader(stepNumber = 1, title = "Select Appliance Category")
                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ApplianceType.values().forEach { type ->
                                        val isSelected = applianceType.equals(type.displayName, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onApplianceTypeChanged(type.displayName) },
                                            label = { Text(type.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null) }
                                            } else null,
                                            modifier = Modifier.testTag("form_appliance_chip_${type.name}"),
                                            colors = FilterChipDefaults.filterChipColors(
                                                labelColor = NeutralDark,
                                                selectedContainerColor = PrimaryLight,
                                                selectedLabelColor = PrimaryBlue,
                                                selectedLeadingIconColor = PrimaryBlue
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = NeutralMedium.copy(alpha = 0.2f),
                                                selectedBorderColor = PrimaryBlue,
                                                enabled = true,
                                                selected = isSelected
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Brand & Age Inputs
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.75f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StepHeader(stepNumber = 2, title = "Brand & Approximate Age")

                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = onBrandChanged,
                                    label = { Text("Brand Name (e.g. LG, Samsung, Whirlpool)") },
                                    placeholder = { Text("Enter brand name") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("brand_text_field"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.6f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )

                                Text(
                                    text = "Approximate Appliance Age:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = NeutralDark
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ageOptions.forEach { ageStr ->
                                        val isSelected = approxAge == ageStr
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onApproxAgeChanged(ageStr) },
                                            label = { Text(ageStr) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("age_chip_$ageStr"),
                                            colors = FilterChipDefaults.filterChipColors(
                                                labelColor = NeutralDark,
                                                selectedContainerColor = PrimaryLight,
                                                selectedLabelColor = PrimaryBlue,
                                                selectedLeadingIconColor = PrimaryBlue
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = NeutralMedium.copy(alpha = 0.2f),
                                                selectedBorderColor = PrimaryBlue,
                                                enabled = true,
                                                selected = isSelected
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Symptoms Multi-select
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.75f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    StepHeader(stepNumber = 3, title = "Select Reported Symptoms")
                                    if (selectedSymptoms.isNotEmpty()) {
                                        Text(
                                            text = "${selectedSymptoms.size} selected",
                                            style = MaterialTheme.typography.labelSmall.copy(color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Symptom.ALL_SYMPTOMS.forEach { symptom ->
                                        val isSelected = selectedSymptoms.contains(symptom.title)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onSymptomToggled(symptom.title) },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (symptom.isSafetyCritical) {
                                                        Text(text = "⚠️ ", fontSize = 12.sp)
                                                    }
                                                    Text(symptom.title)
                                                }
                                            },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null) }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                labelColor = NeutralDark,
                                                selectedContainerColor = if (symptom.isSafetyCritical) SafetyRed.copy(alpha = 0.2f) else PrimaryLight,
                                                selectedLabelColor = if (symptom.isSafetyCritical) SafetyRed else PrimaryBlue,
                                                selectedLeadingIconColor = if (symptom.isSafetyCritical) SafetyRed else PrimaryBlue
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = NeutralMedium.copy(alpha = 0.2f),
                                                selectedBorderColor = if (symptom.isSafetyCritical) SafetyRed else PrimaryBlue,
                                                enabled = true,
                                                selected = isSelected
                                            ),
                                            modifier = Modifier.testTag("symptom_chip_${symptom.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Additional Free-text Note
                    item {
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.75f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                StepHeader(stepNumber = 4, title = "Additional Notes (Optional)")
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = note,
                                    onValueChange = onNoteChanged,
                                    label = { Text("Describe specific sounds, error codes, or power cuts") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("additional_note_field"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.6f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Fixed Bottom Bar CTA in Frosted Surface
                FrostedGlassSurface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    backgroundColor = Color.White.copy(alpha = 0.85f),
                    borderColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Column {
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            FrostedGlassButton(
                            onClick = onSubmitScan,
                            enabled = !isAnalyzing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("submit_scan_button"),
                            containerColor = PrimaryBlue
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SecondaryLight)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "RUN AI DIAGNOSIS",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            )
                        }
                        }
                    }
                }
            }
            if (isAnalyzing) {
                com.example.ui.components.AnimatedScannerLoader(text = "Analyzing with Gemini AI...")
            }
        }
    }
}
