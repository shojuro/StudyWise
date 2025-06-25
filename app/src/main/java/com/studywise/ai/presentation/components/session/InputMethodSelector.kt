package com.studywise.ai.presentation.components.session

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studywise.ai.util.textextraction.InputMethod

@Composable
fun InputMethodSelector(
    selectedMethod: InputMethod,
    onMethodSelected: (InputMethod) -> Unit,
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InputMethodButton(
            method = InputMethod.TEXT,
            icon = Icons.Outlined.Keyboard,
            label = "Type",
            isSelected = selectedMethod == InputMethod.TEXT,
            isEnabled = !isProcessing,
            onClick = { onMethodSelected(InputMethod.TEXT) }
        )
        
        InputMethodButton(
            method = InputMethod.CAMERA,
            icon = Icons.Outlined.CameraAlt,
            label = "Camera",
            isSelected = selectedMethod == InputMethod.CAMERA,
            isEnabled = !isProcessing,
            onClick = { onMethodSelected(InputMethod.CAMERA) }
        )
        
        InputMethodButton(
            method = InputMethod.DOCUMENT,
            icon = Icons.Outlined.Description,
            label = "Upload",
            isSelected = selectedMethod == InputMethod.DOCUMENT,
            isEnabled = !isProcessing,
            onClick = { onMethodSelected(InputMethod.DOCUMENT) }
        )
        
        InputMethodButton(
            method = InputMethod.VOICE,
            icon = Icons.Outlined.Mic,
            label = "Voice",
            isSelected = selectedMethod == InputMethod.VOICE,
            isEnabled = !isProcessing,
            onClick = { onMethodSelected(InputMethod.VOICE) }
        )
    }
}

@Composable
private fun InputMethodButton(
    method: InputMethod,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        FilterChip(
            selected = isSelected,
            onClick = onClick,
            enabled = isEnabled,
            label = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp)
                )
            },
            modifier = Modifier.size(56.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}