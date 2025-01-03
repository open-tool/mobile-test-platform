package presentation.ui.components.badge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atiurin.atp.farmcore.entity.DeviceStatus
import presentation.ui.theme.Colors

@Composable
fun StatusBadge(status: DeviceStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        DeviceStatus.BUSY -> Colors.Status.Busy
        DeviceStatus.FREE -> Colors.Status.Free
        DeviceStatus.BLOCKED -> Colors.Status.Blocked
    }
    Badge(status.name, backgroundColor = color, modifier = modifier)
}