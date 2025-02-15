package presentation.ui.components.badge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import presentation.ui.theme.Colors

@Composable
fun ServerStatusBadge(isAlive: Boolean, modifier: Modifier = Modifier) {
    if (isAlive){
        Badge("Online", backgroundColor = Colors.ServerStatus.Online, modifier = modifier)
    } else {
        Badge("Offline", backgroundColor = Colors.ServerStatus.Offline, modifier = modifier)
    }
}