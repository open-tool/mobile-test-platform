package presentation.ui.components.badge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atiurin.atp.farmcore.entity.DeviceState
import presentation.ui.theme.Colors

@Composable
fun StateBadge(state: DeviceState, modifier: Modifier = Modifier) {
    val color = when (state) {
        DeviceState.READY -> Colors.State.Ready
        else -> Colors.State.NotReady
    }
    Badge(state.name, backgroundColor = color, modifier = modifier)
}