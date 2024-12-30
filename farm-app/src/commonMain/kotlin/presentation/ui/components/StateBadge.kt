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
import com.atiurin.atp.farmcore.entity.DeviceState
import presentation.ui.theme.Colors

@Composable
fun StateBadge(state: DeviceState) {
    val color = when (state) {
        DeviceState.READY -> Colors.State.Ready
        else -> Colors.State.NotReady
    }
    Box(
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        BasicText(
            text = state.name,
            style = TextStyle(color = Color.White, fontSize = 14.sp)
        )
    }
}