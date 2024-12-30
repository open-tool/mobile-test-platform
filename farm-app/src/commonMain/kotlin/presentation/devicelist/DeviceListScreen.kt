package presentation.devicelist


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atiurin.atp.farmcore.entity.PoolDevice
import presentation.ui.components.StateBadge
import presentation.ui.components.StatusBadge


@Composable
fun DeviceListScreen(
    state: DeviceListScreenState,
    onItemClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (state.isLoading){
            Text("Loading...")
            CircularProgressIndicator()
        } else {
            DeviceList(state.devices, onItemClick)
        }
    }
}

@Composable
fun DeviceList(items: List<PoolDevice>, onItemClick: (String) -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEach {
            Row {
                DeviceItemView(deviceItem = it, onItemClick)
            }
        }
    }
}

@Composable
fun DeviceItemView(deviceItem: PoolDevice, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .clickable {
                onClick(deviceItem.device.id)
            }
    ) {
        Column {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${deviceItem.device.ip}:${deviceItem.device.adbConnectPort}")
                Spacer(modifier = Modifier.width(8.dp))
                StateBadge(state = deviceItem.device.state)
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = deviceItem.status)
            }
            HorizontalDivider(modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp))
        }
    }
}
