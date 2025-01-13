package presentation.devicedetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atiurin.atp.farmcore.entity.PoolDevice
import domain.command.Command
import domain.command.getPlatformCommands
import presentation.ui.components.badge.StateBadge
import presentation.ui.components.badge.StatusBadge

@Composable
fun DeviceDetailsScreen(
    viewModel: DeviceDetailsViewModel,
) {
    viewModel.state.value.device?.let {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.Start) {
            DeviceDetailsView(it)
            CommandsBar(viewModel, onButtonClick = { command ->
                when (command) {
                    Command.CONNECT -> viewModel.connectDevice(it.device)
                    Command.DISCONNECT -> viewModel.disconnectDevice(it.device)
                    Command.BLOCK -> viewModel.blockDevice(it.device.id)
                    Command.UNBLOCK -> viewModel.unblockDevice(it.device.id)
                }
            })
        }
    }
}

@Composable
fun CommandsBar(viewModel: DeviceDetailsViewModel, onButtonClick: (Command) -> Unit) {
    val commands = getPlatformCommands()
    Row {
        commands.forEach { command ->
            val enabled = viewModel.state.value.buttonEnabledMap[command.name] ?: false
            val buttonColor = if (enabled) Color.Yellow else Color.LightGray
            Button(
                enabled = enabled,
                onClick = {
                    onButtonClick(command)
                },
                border = BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.DarkGray,
                    containerColor = buttonColor
                ),
                modifier = Modifier.padding(top = 8.dp, end = 8.dp)
            ) {
                Text(command.name)
            }
        }
    }

}

@Composable
fun DeviceDetailsView(poolDevice: PoolDevice) {
    Column {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            StateBadge(state = poolDevice.device.state)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = poolDevice.status)
        }
        PropertyItemWithCopy("id", poolDevice.device.id)
        PropertyItemWithCopy("ip", "${poolDevice.device.ip}:${poolDevice.device.adbConnectPort}")
        PropertyItem("adb port", poolDevice.device.adbConnectPort.toString())
        PropertyItem("name", poolDevice.device.name)
        PropertyItem("group", poolDevice.device.groupId)
        PropertyItem("image", poolDevice.device.dockerImage)
        PropertyItem("desc", poolDevice.desc)
        PropertyItem("userAgent", poolDevice.userAgent)
        PropertyItem("busy time", poolDevice.busyTimestampSec.toString())
        PropertyItem("last ping", poolDevice.lastPingTimestampSec.toString())
    }
}

@Composable
fun PropertyItem(name: String, value: String? = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = "$name:", color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value ?: "", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PropertyItemWithCopy(name: String, value: String?) {
    val clipboardManager = LocalClipboardManager.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$name:",
            color = Color.Gray,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value ?: "",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
        )
        if (!value.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $name",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(value))
                    }
            )
        }
    }
}