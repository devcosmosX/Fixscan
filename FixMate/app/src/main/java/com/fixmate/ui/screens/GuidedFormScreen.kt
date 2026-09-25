package com.fixmate.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fixmate.DiagnosisViewModel
import com.fixmate.model.ApplianceType
import com.fixmate.model.Symptom

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GuidedFormScreen(
    viewModel: DiagnosisViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val ageOptions = listOf(
        "Less than 1 year", "1–3 years", "3–5 years", "5–10 years", "More than 10 years"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Describe the problem") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            DropdownField(
                label = "Appliance",
                selected = form.applianceType.displayName,
                options = ApplianceType.values().map { it.displayName },
                onSelect = { name ->
                    ApplianceType.values().firstOrNull { it.displayName == name }
                        ?.let(viewModel::setApplianceType)
                }
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = form.brand,
                onValueChange = viewModel::setBrand,
                label = { Text("Brand (optional)") },
                placeholder = { Text("e.g. Samsung, LG, Whirlpool") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            DropdownField(
                label = "Approximate age",
                selected = form.approxAge,
                options = ageOptions,
                onSelect = viewModel::setAge
            )
            Spacer(Modifier.height(24.dp))

            Text("What's happening?", style = MaterialTheme.typography.titleMedium)
            Text(
                "Select all that apply",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Symptom.ALL.forEach { symptom ->
                    val selected = symptom.id in form.selectedSymptomIds
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleSymptom(symptom.id) },
                        label = { Text(symptom.title) },
                        leadingIcon = if (symptom.safetyCritical) {
                            {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = form.note,
                onValueChange = viewModel::setNote,
                label = { Text("Anything else? (optional)") },
                placeholder = { Text("e.g. started after a power cut, error code E4…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onSubmit,
                enabled = form.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Diagnose", style = MaterialTheme.typography.labelLarge)
            }
            if (!form.canSubmit) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Select at least one symptom to continue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
