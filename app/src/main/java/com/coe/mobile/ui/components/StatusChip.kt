package com.coe.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coe.mobile.ui.theme.CoeNeutralChip
import com.coe.mobile.ui.theme.CoeSuccess
import com.coe.mobile.ui.theme.CoeWarning

enum class StatusChipVariant {
    Success,
    Warning,
    Error,
    Neutral
}

@Composable
fun StatusChip(
    label: String,
    variant: StatusChipVariant,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, borderColor) = when (variant) {
        StatusChipVariant.Success -> Triple(
            CoeSuccess.copy(alpha = 0.16f),
            CoeSuccess,
            CoeSuccess.copy(alpha = 0.5f)
        )
        StatusChipVariant.Warning -> Triple(
            CoeWarning.copy(alpha = 0.16f),
            CoeWarning,
            CoeWarning.copy(alpha = 0.5f)
        )
        StatusChipVariant.Error -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error.copy(alpha = 0.52f)
        )
        StatusChipVariant.Neutral -> Triple(
            CoeNeutralChip.copy(alpha = 0.74f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
        )
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}
