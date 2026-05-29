package com.redrum.rootedfirmwarelab.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingWalkthrough(
    steps: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(steps[currentStep].first, style = MaterialTheme.typography.titleLarge)
                    Text(steps[currentStep].second, style = MaterialTheme.typography.bodyMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) { Text("Skip") }
                        Button(onClick = {
                            if (currentStep < steps.size - 1) currentStep++ else onDismiss()
                        }) {
                            Text(if (currentStep < steps.size - 1) "Next" else "Finish")
                        }
                    }
                }
            }
        }
    }
}
