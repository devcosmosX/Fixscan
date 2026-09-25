package com.example.ui.screens

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.MechanicsUiState
import com.example.model.NearbyMechanic
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.*
import com.example.util.NavigationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicsMapScreen(
    uiState: MechanicsUiState,
    onRefresh: () -> Unit,
    onSelectMechanic: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    val markerMap = remember { mutableMapOf<String, Marker>() }

    // Synchronize Map Camera when a mechanic is selected
    LaunchedEffect(uiState) {
        if (uiState is MechanicsUiState.Success) {
            val selected = uiState.mechanics.find { it.id == uiState.selectedMechanicId }
            if (selected != null && googleMapInstance != null) {
                googleMapInstance?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(selected.latitude, selected.longitude),
                        15f
                    )
                )
                // Scroll card into view
                val idx = uiState.mechanics.indexOf(selected)
                if (idx >= 0) {
                    listState.animateScrollToItem(idx)
                }
            }
        }
    }

    FrostedBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Nearby Mechanics & Repair",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                            )
                            Text(
                                text = "Google Places & Maps Navigation",
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
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = PrimaryBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.testTag("mechanics_map_screen")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState) {
                    is MechanicsUiState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Locating nearby repair workshops with Google Places...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeutralDark
                                )
                            )
                        }
                    }

                    is MechanicsUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = SafetyRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to load nearby mechanics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FrostedGlassButton(
                                onClick = onRefresh,
                                modifier = Modifier.width(160.dp),
                                containerColor = PrimaryBlue
                            ) {
                                Text("Retry Search", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is MechanicsUiState.Empty -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = NeutralMedium,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FrostedGlassButton(
                                onClick = onRefresh,
                                modifier = Modifier.width(160.dp),
                                containerColor = PrimaryBlue
                            ) {
                                Text("Refresh", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is MechanicsUiState.Success -> {
                        val mechanics = uiState.mechanics
                        val userLat = uiState.userLatitude
                        val userLng = uiState.userLongitude

                        // 1. Google Map View
                        GoogleMapView(
                            userLatitude = userLat,
                            userLongitude = userLng,
                            mechanics = mechanics,
                            selectedMechanicId = uiState.selectedMechanicId,
                            onMapReady = { map ->
                                googleMapInstance = map
                            },
                            onMarkerClicked = { mechanicId ->
                                onSelectMechanic(mechanicId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 2. Bottom Floating Card Carousel for Nearby Mechanics
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            // Counter Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FrostedGlassSurface(
                                    shape = RoundedCornerShape(20.dp),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = Color.White
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${mechanics.size} Repair Shops Found",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NeutralDark
                                            )
                                        )
                                    }
                                }

                                FrostedGlassSurface(
                                    shape = RoundedCornerShape(20.dp),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = Color.White,
                                    modifier = Modifier.clickable {
                                        googleMapInstance?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(userLat, userLng),
                                                14f
                                            )
                                        )
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "My Location",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryTeal
                                            )
                                        )
                                    }
                                }
                            }

                            // Horizontal Mechanic Cards
                            LazyRow(
                                state = listState,
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(mechanics, key = { it.id }) { mechanic ->
                                    val isSelected = mechanic.id == uiState.selectedMechanicId
                                    MechanicMapCard(
                                        mechanic = mechanic,
                                        isSelected = isSelected,
                                        onClick = {
                                            onSelectMechanic(mechanic.id)
                                        },
                                        onNavigate = {
                                            NavigationHelper.startNavigation(
                                                context = context,
                                                latitude = mechanic.latitude,
                                                longitude = mechanic.longitude,
                                                destinationName = mechanic.name
                                            )
                                        },
                                        onCall = {
                                            NavigationHelper.callMechanic(
                                                context = context,
                                                phoneNumber = mechanic.phoneNumber ?: "+91 98765 43210"
                                            )
                                        },
                                        modifier = Modifier.width(300.dp)
                                    )
                                }
                            }
                        }
                    }

                    is MechanicsUiState.Idle -> {
                        // Handled automatically by ViewModel trigger
                    }
                }
            }
        }
    }
}

@Composable
fun MechanicMapCard(
    mechanic: NearbyMechanic,
    isSelected: Boolean,
    onClick: () -> Unit,
    onNavigate: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier
            .clickable { onClick() }
            .testTag("mechanic_map_card_${mechanic.id}"),
        backgroundColor = if (isSelected) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.85f),
        borderColor = if (isSelected) PrimaryBlue else Color.White.copy(alpha = 0.9f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mechanic.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mechanic.address,
                        style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryLight,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "★ ${mechanic.formattedRating}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mechanic.formattedDistance} away",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (mechanic.isOpenNow != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (mechanic.isOpenNow) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
                    ) {
                        Text(
                            text = if (mechanic.isOpenNow) "Open Now" else "Closed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (mechanic.isOpenNow) Color(0xFF137333) else SafetyRed,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrostedGlassButton(
                    onClick = onNavigate,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("navigate_button_${mechanic.id}"),
                    containerColor = PrimaryBlue
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Navigate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .testTag("call_button_${mechanic.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Call",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(
    userLatitude: Double,
    userLongitude: Double,
    mechanics: List<NearbyMechanic>,
    selectedMechanicId: String?,
    onMapReady: (GoogleMap) -> Unit,
    onMarkerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            try {
                mapView.onDestroy()
            } catch (e: Exception) {}
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { mapV ->
        mapV.getMapAsync { googleMap ->
            onMapReady(googleMap)

            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false

            googleMap.clear()

            // 1. Add User Location Marker (Cyan/Azure)
            val userLatLng = LatLng(userLatitude, userLongitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(userLatLng)
                    .title("Your Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            // 2. Add Mechanic Markers (Red / Selected Orange)
            mechanics.forEach { mechanic ->
                val mLatLng = LatLng(mechanic.latitude, mechanic.longitude)
                val isSelected = mechanic.id == selectedMechanicId
                val hue = if (isSelected) BitmapDescriptorFactory.HUE_ORANGE else BitmapDescriptorFactory.HUE_RED

                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(mLatLng)
                        .title(mechanic.name)
                        .snippet("${mechanic.formattedDistance} • ★ ${mechanic.formattedRating}")
                        .icon(BitmapDescriptorFactory.defaultMarker(hue))
                )
                marker?.tag = mechanic.id
            }

            // 3. Set marker click handler
            googleMap.setOnMarkerClickListener { marker ->
                val id = marker.tag as? String
                if (id != null) {
                    onMarkerClicked(id)
                    marker.showInfoWindow()
                    true
                } else {
                    false
                }
            }

            // Initial Camera Positioning
            val targetLocation = mechanics.find { it.id == selectedMechanicId }?.let {
                LatLng(it.latitude, it.longitude)
            } ?: userLatLng

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 13.5f))
        }
    }
}
