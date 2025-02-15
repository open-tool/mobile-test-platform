package presentation.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atiurin.atp.farmcore.entity.ServerInfo
import presentation.ui.components.badge.ServerStatusBadge
import utils.formatTimestampToDateString

@Composable
fun ServersScreen(
    viewModel: ServersListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
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
        ServersLisTable(
            uiState = uiState
        )
    }

}

@Composable
fun ServersLisTable(
    uiState: ServersListUiState
){
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ServersListTableHeader()
        }
        items(uiState.servers){ serverInfo: ServerInfo ->
            ServersListTableItem(serverInfo)
        }
    }
}

@Composable
fun ServersListTableHeader(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
//        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Text("IP", modifier = Modifier.weight(3f).padding(8.dp))
        Text("Port", modifier = Modifier.weight(1f).padding(8.dp))
        Text("Status", modifier = Modifier.weight(1.5f).padding(8.dp))
        Text("Alive time", modifier = Modifier.weight(2f).padding(8.dp))
    }
}

@Composable
fun ServersListTableItem(serverInfo: ServerInfo){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Text(serverInfo.ip, modifier = Modifier.weight(3f).padding(8.dp))
        Text(serverInfo.port.toString(), modifier = Modifier.weight(1f).padding(8.dp))
        Box(modifier = Modifier.weight(1.5f).padding(8.dp)){
            ServerStatusBadge(serverInfo.isAlive)
        }
        Text(formatTimestampToDateString(serverInfo.aliveTimestamp), modifier = Modifier.weight(2f).padding(8.dp))
    }
}