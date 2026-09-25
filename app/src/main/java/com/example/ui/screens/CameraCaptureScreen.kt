package com.example.ui.screens

import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SecondaryTeal

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.model.VisionDetectionResult
import com.example.model.LocalizedObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    currentPhotoUri: String?,
    visionResult: VisionDetectionResult? = null,
    isDetectingObjects: Boolean = false,
    onPhotoCaptured: (Uri?) -> Unit,
    onDetectObjects: (Uri) -> Unit = {},
    onSelectDetectedObject: (String) -> Unit = {},
    onOpenMechanicsMap: () -> Unit = {},
    onProceedToForm: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var previewUri by remember { mutableStateOf<Uri?>(currentPhotoUri?.let { Uri.parse(it) }) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            previewUri = uri
            onPhotoCaptured(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            previewUri = tempCameraUri
            onPhotoCaptured(tempCameraUri)
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", java.io.File.createTempFile("capture_", ".jpg", context.cacheDir))
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    FrostedBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_capture_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Appliance Photo Scan", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        com.example.ui.components.GlassBackButton(onClick = onBack)
                    },
                    actions = {
                        TextButton(
                            onClick = onProceedToForm,
                            modifier = Modifier.testTag("skip_photo_button")
                        ) {
                            Text("Skip Photo", color = PrimaryBlue, fontWeight = FontWeight.Bold)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Instruction header in Frosted Glass Card
                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.75f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (previewUri == null) "Point Camera at Appliance or Model Label" else "Photo Captured!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (previewUri == null)
                                "Include brand name, model tag, control panel, or visible damage"
                            else "Ready to analyze this image with Gemini AI",
                            style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Viewfinder / Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewUri != null) {
                        AsyncImage(
                            model = previewUri,
                            contentDescription = "Captured Appliance Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top-left Cloud Vision Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeutralDark.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDetectingObjects) "Cloud Vision: Analyzing..." else "Google Cloud Vision",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Bottom detected tags overlay
                        if (visionResult != null && visionResult.objects.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(visionResult.objects) { obj ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PrimaryBlue.copy(alpha = 0.85f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                        modifier = Modifier.clickable {
                                            onSelectDetectedObject(obj.name)
                                        }
                                    ) {
                                        Text(
                                            text = "${obj.name} (${obj.confidencePercentage}%)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback camera viewfinder graphic representation
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "Viewfinder Camera Graphic",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Scanning Reticle Overlay
                        Box(
                            modifier = Modifier
                                .size(220.dp, 160.dp)
                                .border(2.dp, SecondaryTeal, RoundedCornerShape(12.dp))
                                .background(SecondaryTeal.copy(alpha = 0.15f))
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeutralDark.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "[ ALIGN MODEL TAG OR DAMAGE HERE ]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Action Bar Controls & Object Localization Panel
                if (previewUri == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery Button
                            FrostedGlassSurface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .testTag("gallery_button"),
                                backgroundColor = Color.White.copy(alpha = 0.7f),
                                borderColor = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Upload from Gallery",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Capture Shutter Button
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clickable {
                                        if (hasCameraPermission) {
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", java.io.File.createTempFile("capture_", ".jpg", context.cacheDir))
                                            tempCameraUri = uri
                                            cameraLauncher.launch(uri)
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                    .testTag("capture_shutter_button"),
                                shadowElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        modifier = Modifier.size(62.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Capture Photo",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Flash Button (Toggle placeholder)
                            FrostedGlassSurface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable { /* Toggle flash */ }
                                    .testTag("flash_button"),
                                backgroundColor = Color.White.copy(alpha = 0.7f),
                                borderColor = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "Toggle Flash",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Tap circle to capture, or choose from gallery",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeutralMedium,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    // Preview Confirmation & Object Localization Details
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Google Cloud Vision Card
                        FrostedGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CenterFocusWeak,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Google Cloud Vision Localization",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NeutralDark
                                            )
                                        )
                                    }

                                    if (isDetectingObjects) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                when {
                                    isDetectingObjects -> {
                                        Text(
                                            text = "Analyzing image and detecting objects via Vision API...",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium)
                                        )
                                    }
                                    visionResult != null && visionResult.objects.isNotEmpty() -> {
                                        if (visionResult.detectedModelNumber != null) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = SecondaryTeal.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, SecondaryTeal.copy(alpha = 0.4f)),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            ) {
                                                Text(
                                                    text = "🏷️ Model: ${visionResult.detectedModelNumber}${if (visionResult.detectedBrand != null) " (${visionResult.detectedBrand})" else ""}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = SecondaryTeal,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Detected ${visionResult.objects.size} objects. Tap to select appliance type:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(visionResult.objects) { obj ->
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = PrimaryLight,
                                                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                                                    modifier = Modifier.clickable {
                                                        onSelectDetectedObject(obj.name)
                                                    }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "${obj.name} • ${obj.confidencePercentage}%",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = PrimaryBlue,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    visionResult != null && !visionResult.isSuccess -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = visionResult.errorMessage ?: "Detection unavailable",
                                                style = MaterialTheme.typography.bodySmall.copy(color = SafetyRed),
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            TextButton(onClick = { previewUri?.let { onDetectObjects(it) } }) {
                                                Text("Retry", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    else -> {
                                        Text(
                                            text = "Ready to detect appliance objects and model label tags.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium)
                                        )
                                    }
                                }
                            }
                        }

                        // Direct Navigation Button to Nearby Mechanics
                        OutlinedButton(
                            onClick = onOpenMechanicsMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Find Nearby Mechanics on Map",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Bottom Controls: Retake & Continue
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    previewUri = null
                                    onPhotoCaptured(null)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .testTag("retake_photo_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retake")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retake", fontWeight = FontWeight.Bold)
                            }

                            FrostedGlassButton(
                                onClick = onProceedToForm,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .testTag("use_photo_button")
                            ) {
                                Text(
                                    "Use Photo & Continue",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}
