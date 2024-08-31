package presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atiurin.atp.farmcore.entity.DeviceStatus
import presentation.ui.theme.Colors


@Composable
fun StatusBadge(status: DeviceStatus) {
    val color = when (status) {
        DeviceStatus.BUSY -> Colors.Status.Busy
        DeviceStatus.FREE -> Colors.Status.Free
        DeviceStatus.BLOCKED -> Colors.Status.Blocked
    }
    Box(
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        BasicText(
            text = status.name,
            style = TextStyle(color = Color.White, fontSize = 14.sp)
        )
    }
}