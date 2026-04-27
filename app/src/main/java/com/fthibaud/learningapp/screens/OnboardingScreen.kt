package com.fthibaud.learningapp.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fthibaud.learningapp.permissions.isAccessibilityEnabled
import com.fthibaud.learningapp.permissions.isBatteryOptimizationDisabled
import com.fthibaud.learningapp.permissions.isOverlayEnabled
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val buttonLabel: String,
    val isGranted: (Context) -> Boolean,
    val openSettings: (Context) -> Unit,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Accessibility,
        title = "Service d'accessibilité",
        description = "Kakomon a besoin du service d'accessibilité pour détecter quand vous ouvrez une app surveillée.",
        buttonLabel = "Ouvrir les paramètres d'accessibilité",
        isGranted = ::isAccessibilityEnabled,
        openSettings = { context ->
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        },
    ),
    OnboardingPage(
        icon = Icons.Default.Layers,
        title = "Affichage par-dessus d'autres apps",
        description = "Cette permission permet d'afficher le quiz par-dessus l'app que vous tentez d'ouvrir.",
        buttonLabel = "Autoriser l'affichage",
        isGranted = ::isOverlayEnabled,
        openSettings = { context ->
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            )
        },
    ),
    OnboardingPage(
        icon = Icons.Default.BatterySaver,
        title = "Optimisation de la batterie",
        description = "Désactivez l'optimisation de batterie pour que le service de détection ne soit pas arrêté par le système.",
        buttonLabel = "Désactiver l'optimisation",
        isGranted = ::isBatteryOptimizationDisabled,
        openSettings = { context ->
            context.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                )
            )
        },
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToAppSelection: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // Re-check permissions when returning from settings
    val permissionStates = remember {
        pages.map { mutableStateOf(it.isGranted(context)) }
    }
    LifecycleResumeEffect(Unit) {
        permissionStates.forEachIndexed { index, state ->
            state.value = pages[index].isGranted(context)
        }
        onPauseOrDispose {}
    }

    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == pages.size - 1

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configuration") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val page = pages[pageIndex]
                val granted = permissionStates[pageIndex].value

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = if (granted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (granted) {
                        Text(
                            text = "Permission accordée",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        OutlinedButton(onClick = { page.openSettings(context) }) {
                            Text(page.buttonLabel)
                        }
                    }
                }
            }

            // Bottom section: page indicator + navigation buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        val color by animateColorAsState(
                            targetValue = if (index == currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            label = "dot_color"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentPage - 1)
                                }
                            }
                        ) {
                            Text("Précédent")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (isLastPage) {
                        Button(
                            onClick = onNavigateToAppSelection,
                        ) {
                            Text("Commencer")
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentPage + 1)
                                }
                            }
                        ) {
                            Text("Suivant")
                        }
                    }
                }
            }
        }
    }
}
