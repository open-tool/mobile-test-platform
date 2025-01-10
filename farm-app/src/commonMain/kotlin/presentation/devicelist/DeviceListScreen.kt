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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.collectAsState
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
    viewModel: DeviceListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onItemClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
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
        DeviceListTable(
            uiState = uiState,
            onItemClick = onItemClick,
            onStatusChange = { viewModel.updateSelectedStatus(it) },
            onStateChange = { viewModel.updateSelectedState(it) },
            onGroupChange = { viewModel.updateSelectedGroup(it) },
            onSortChange = { viewModel.updateSortField(it) }
        )
    }
}

@Composable
fun DeviceListTable(
    uiState: DeviceListUiState,
    onItemClick: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onSortChange: (SortField) -> Unit,
) {
    val noFilterOption = "All"
    val filterDeviceStatus = mutableListOf(noFilterOption).apply { addAll(DeviceStatus.entries.map { it.name }) }
    val filterDeviceState = mutableListOf(noFilterOption).apply { addAll(DeviceState.entries.map { it.name }) }
    val filterGroups =
        mutableListOf(noFilterOption).apply { addAll(uiState.devices.map { it.device.groupId }.distinct().sorted()) }

    val filteredDevices = uiState.devices
        .filter { device ->
            (uiState.selectedStatus == noFilterOption || device.status.name == uiState.selectedStatus) &&
                    (uiState.selectedState == noFilterOption || device.device.state.name == uiState.selectedState) &&
                    (uiState.selectedGroup == noFilterOption || device.device.groupId == uiState.selectedGroup)
        }
        .sortedWith(
            when (uiState.sortBy) {
                SortField.Name -> if (uiState.sortAscending) compareBy { it.device.name } else compareByDescending { it.device.name }
                SortField.Ip -> if (uiState.sortAscending) compareBy { it.device.ip } else compareByDescending { it.device.ip }
                SortField.Status -> if (uiState.sortAscending) compareBy { it.status } else compareByDescending { it.status }
                SortField.State -> if (uiState.sortAscending) compareBy { it.device.state } else compareByDescending { it.device.state }
                SortField.Group -> if (uiState.sortAscending) compareBy { it.device.groupId } else compareByDescending { it.device.groupId }
            }
        )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            FiltersRow(
                uiState = uiState,
                groupIds = filterGroups,
                statuses = filterDeviceStatus,
                states = filterDeviceState,
                onStatusChange = onStatusChange,
                onStateChange = onStateChange,
                onGroupChange = onGroupChange
            )
        }

        item {
            TableHeader(
                sortBy = uiState.sortBy,
                sortAscending = uiState.sortAscending,
                onSortChange = onSortChange
            )
        }

        itemsIndexed(filteredDevices) { index, device ->
            DeviceTableRow(index = index+1, device = device, onClick = onItemClick)
        }
    }
}

@Composable
fun FiltersRow(
    uiState: DeviceListUiState,
    statuses: List<String>,
    states: List<String>,
    groupIds: List<String>,
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
            modifier = Modifier.weight(1f),
            selectedText = uiState.selectedStatus
        )
        DropdownMenuFilter(
            label = "State",
            options = states,
            onOptionSelected = { onStateChange(it) },
            modifier = Modifier.weight(1f),
            selectedText = uiState.selectedState
        )
        DropdownMenuFilter(
            label = "Group",
            options = groupIds,
            onOptionSelected = { onGroupChange(it) },
            modifier = Modifier.weight(1f),
            selectedText = uiState.selectedGroup
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
        Text("#", modifier = Modifier.weight(0.3f).padding(start = 8.dp))
        SortableColumnHeader("Name", SortField.Name, sortBy, sortAscending, onSortChange, Modifier.weight(2f))
        SortableColumnHeader("IP", SortField.Ip, sortBy, sortAscending, onSortChange, Modifier.weight(2f))
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
fun DeviceTableRow(index: Int, device: PoolDevice, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(device.device.id) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(index.toString(), modifier = Modifier.weight(0.3f).padding(8.dp))
        Text(device.device.name, modifier = Modifier.weight(2f).padding(8.dp))
        Text("${device.device.ip}:${device.device.adbConnectPort}", modifier = Modifier.weight(2f).padding(8.dp))
        Box(modifier = Modifier.weight(1f).padding(8.dp)) { StatusBadge(device.status) }
        Box(modifier = Modifier.weight(1f).padding(8.dp)) { StateBadge(device.device.state) }
        Text(device.device.groupId, modifier = Modifier.weight(1f).padding(8.dp))
    }
}

@Composable
fun <T> DropdownMenuFilter(
    label: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier,
    selectedText: String,
) {
    var expanded by remember { mutableStateOf(false) }

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
                    text = selectedText,
                    color = MaterialTheme.colorScheme.onSurface,
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