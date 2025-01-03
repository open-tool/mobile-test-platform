package presentation.ui.components.badge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun GroupBadge(group: String) {
    Badge("Group: $group", backgroundColor = Color.Gray)
}