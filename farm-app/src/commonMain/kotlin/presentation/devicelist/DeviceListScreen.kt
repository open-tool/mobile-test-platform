package presentation.devicelist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmcore.entity.PoolDevice
import presentation.ui.components.badge.StateBadge
import presentation.ui.components.badge.StatusBadge

@Composable
fun DeviceListScreen(
    state: DeviceListUiState,
    onItemClick: (String) -> Unit,
) {
    if (state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading...")
            CircularProgressIndicator()
        }
    } else {
        DeviceListTable(state.devices, onItemClick)
    }
}

@Composable
fun DeviceListTable(
    devices: List<PoolDevice>,
    onItemClick: (String) -> Unit,
) {
    val noFilterOption = "All"
    var selectedStatus by remember { mutableStateOf(noFilterOption) }
    var selectedState by remember { mutableStateOf(noFilterOption) }
    var selectedGroup by remember { mutableStateOf(noFilterOption) }
    var sortBy by remember { mutableStateOf(SortField.Name) }
    var sortAscending by remember { mutableStateOf(true) }

    val filterDeviceStatus = mutableListOf("All").apply { addAll(DeviceStatus.entries.map { it.name }) }
    val filterDeviceState = mutableListOf("All").apply { addAll(DeviceState.entries.map { it.name }) }
    val filterGroups = mutableListOf("All").apply { addAll( devices.map { it.device.groupId }.distinct().sorted()) }
    val filteredDevices = devices
        .filter { device ->
            (selectedStatus == noFilterOption || device.status.name == selectedStatus) &&
                    (selectedState == noFilterOption || device.device.state.name == selectedState) &&
                    (selectedGroup == noFilterOption || device.device.groupId == selectedGroup)
        }
        .sortedWith(
            when (sortBy) {
                SortField.Name -> if (sortAscending) compareBy { it.device.name } else compareByDescending { it.device.name }
                SortField.Ip -> if (sortAscending) compareBy { it.device.ip } else compareByDescending { it.device.ip }
                SortField.Status -> if (sortAscending) compareBy { it.status } else compareByDescending { it.status }
                SortField.State -> if (sortAscending) compareBy { it.device.state } else compareByDescending { it.device.state }
                SortField.Group -> if (sortAscending) compareBy { it.device.groupId } else compareByDescending { it.device.groupId }
            }
        )
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            FiltersRow(
                groupIds = filterGroups,
                statuses = filterDeviceStatus,
                states = filterDeviceState,
                selectedStatus = selectedStatus,
                selectedState = selectedState,
                selectedGroup = selectedGroup,
                onStatusChange = { selectedStatus = it },
                onStateChange = { selectedState = it },
                onGroupChange = { selectedGroup = it }
            )
        }

        item {
            TableHeader(
                sortBy = sortBy,
                sortAscending = sortAscending,
                onSortChange = { field ->
                    if (sortBy == field) sortAscending = !sortAscending
                    else {
                        sortBy = field
                        sortAscending = true
                    }
                }
            )
        }
        items(filteredDevices) { device ->
            DeviceTableRow(device = device, onClick = onItemClick)
        }
    }
}

@Composable
fun FiltersRow(
    statuses: List<String>,
    states: List<String>,
    groupIds: List<String>,
    selectedStatus: String,
    selectedState: String,
    selectedGroup: String?,
    onStatusChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DropdownMenuFilter(
            label = "Status",
            options = statuses,
            onOptionSelected = { onStatusChange(it) },
            modifier = Modifier.weight(1f)
        )
        DropdownMenuFilter(
            label = "State",
            options = states,
            onOptionSelected = { onStateChange(it) },
            modifier = Modifier.weight(1f)
        )
        DropdownMenuFilter(
            label = "Group",
            options = groupIds,
            onOptionSelected = { onGroupChange(it) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TableHeader(
    sortBy: SortField,
    sortAscending: Boolean,
    onSortChange: (SortField) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SortableColumnHeader("Name", SortField.Name, sortBy, sortAscending, onSortChange, Modifier.weight(1f))
        SortableColumnHeader("IP", SortField.Ip, sortBy, sortAscending, onSortChange, Modifier.weight(1f))
        SortableColumnHeader("Status", SortField.Status, sortBy, sortAscending, onSortChange, Modifier.weight(1f))
        SortableColumnHeader("State", SortField.State, sortBy, sortAscending, onSortChange, Modifier.weight(1f))
        SortableColumnHeader("Group", SortField.Group, sortBy, sortAscending, onSortChange, Modifier.weight(1f))
    }
}

@Composable
fun SortableColumnHeader(
    title: String,
    field: SortField,
    sortBy: SortField,
    sortAscending: Boolean,
    onSortChange: (SortField) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title + if (sortBy == field) if (sortAscending) " ↑" else " ↓" else "",
        modifier = modifier
            .clickable { onSortChange(field) }
            .padding(8.dp)
    )
}

@Composable
fun DeviceTableRow(device: PoolDevice, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(device.device.id) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(device.device.name, modifier = Modifier.weight(1f))
        Text("${device.device.ip}:${device.device.adbConnectPort}", modifier = Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f)) { StatusBadge(device.status) }
        Box(modifier = Modifier.weight(1f)) { StateBadge(device.device.state) }
        Text(device.device.groupId, modifier = Modifier.weight(1f))
    }
}

@Composable
fun <T> DropdownMenuFilter(
    label: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("All") }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .padding(start = 16.dp, top = 4.dp)
    )
    Box(
        modifier = modifier.clickable { expanded = !expanded }.padding(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedText.ifEmpty { label },
                    color = if (selectedText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.toString()) },
                    onClick = {
                        selectedText = option.toString()
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

enum class SortField {
    Name, Ip, Status, State, Group
}