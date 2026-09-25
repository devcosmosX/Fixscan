package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.ui.theme.*

/**
 * Renders an ambient luminous gradient backdrop ideal for Frosted Glass translucency.
 */
@Composable
fun FrostedBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val ambientGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE6F5F4), // Very light teal/white at top
            Color(0xFFC5EBE6), // Mid cyan/teal
            Color(0xFF45A29E), // Deeper teal
            Color(0xFF0D3B3E)  // Dark deep teal at bottom
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ambientGradient)
            .drawBehind {
                // Glowing radial background blobs for glass refraction feel
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * 0.1f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color(0xFFE6BD63).copy(alpha = 0.15f), // subtle warm glow like in image
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.8f, size.height * 0.4f)
                )
            }
    ) {
        content()
    }
}

/**
 * Custom Frosted Glass Card container with translucent surface background,
 * crisp white hairline border, and gentle drop shadow.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.72f),
    borderColor: Color = Color.White.copy(alpha = 0.8f),
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            borderColor,
            Color.White.copy(alpha = 0.3f),
            PrimaryBlue.copy(alpha = 0.2f)
        )
    )

    Surface(
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, glassBorderBrush),
        shadowElevation = elevation,
        tonalElevation = 2.dp,
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        Column {
            content()
        }
    }
}

/**
 * Lightweight Frosted Glass Pill or Chip Surface container.
 */
@Composable
fun FrostedGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.65f),
    borderColor: Color = Color.White.copy(alpha = 0.7f),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * Frosted Glass Button with vibrant gradient background fill and glassy outline.
 */
@Composable
fun FrostedGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    containerColor: Color = Color(0xFF0F3A3D),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .shadow(12.dp, shape, ambientColor = containerColor, spotColor = containerColor),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        elevation = null, // using shadow modifier instead for softer look
        content = content
    )
}

@Composable
fun AnimatedScannerLoader(modifier: Modifier = Modifier, text: String = "Analyzing...") {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F3A3D).copy(alpha = 0.85f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, SecondaryTeal.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.size(64.dp)
                )

                // Scan line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = scanLineY.dp)
                        .background(SecondaryTeal)
                        .shadow(8.dp, spotColor = SecondaryTeal)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun GlassBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = NeutralDark,
    backgroundColor: Color = Color.White.copy(alpha = 0.6f)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = backgroundColor, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
