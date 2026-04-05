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
        StatusChipVariant.Success -> Triple(CoeSuccess.copy(alpha = 0.14f), CoeSuccess, CoeSuccess.copy(alpha = 0.44f))
        StatusChipVariant.Warning -> Triple(CoeWarning.copy(alpha = 0.14f), CoeWarning, CoeWarning.copy(alpha = 0.46f))
        StatusChipVariant.Error -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error.copy(alpha = 0.48f)
        )
        StatusChipVariant.Neutral -> Triple(
            CoeNeutralChip.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
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
