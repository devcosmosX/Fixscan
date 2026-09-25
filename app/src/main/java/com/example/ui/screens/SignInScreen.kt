package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserSession
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal

@Composable
fun SignInScreen(
    userSession: UserSession,
    onToggleAuth: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    FrostedBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sign_in_screen"),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // App Logo Badge in Frosted Glass Surface
                    FrostedGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "FixScan Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "FixScan",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    )

                    Text(
                        text = "AI Appliance Diagnostics & Repair Cost Estimator",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeutralMedium,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Benefits Section Card
                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.78f),
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureRow(
                            icon = Icons.Default.CheckCircle,
                            title = "Instant Photo Diagnostics",
                            subtitle = "Analyze motor noise, electrical faults & wear with Gemini AI"
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.8f))
                        FeatureRow(
                            icon = Icons.Default.Shield,
                            title = "Safety Hazard Alerts",
                            subtitle = "Detect fire risks, shock hazards & electrical short circuits early"
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.8f))
                        FeatureRow(
                            icon = Icons.Default.Build,
                            title = "Fair Price Estimates & Mechanics",
                            subtitle = "Know exact part costs before calling nearby verified mechanics"
                        )
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FrostedGlassButton(
                        onClick = onToggleAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_signin_button"),
                        containerColor = PrimaryBlue
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Sign in",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (userSession.isLoggedIn) "Signed in as ${userSession.userName} (Tap to Switch)" else "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_guest_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeutralDark)
                    ) {
                        Text(
                            text = "Continue to Home Screen",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "🔒 Secure Firebase Auth & Encrypted Diagnostic History",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeutralMedium,
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        FrostedGlassSurface(
            shape = RoundedCornerShape(10.dp),
            backgroundColor = SecondaryTeal.copy(alpha = 0.15f),
            borderColor = SecondaryTeal.copy(alpha = 0.3f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryTeal,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium)
            )
        }
    }
}
