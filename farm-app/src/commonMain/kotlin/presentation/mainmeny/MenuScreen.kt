package presentation.mainmeny

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import presentation.app.AppScreen

@Composable
fun MenuScreen(
    onNextButtonClicked: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
){
    val menuItems = listOf(
        AppScreen.DeviceList,
        AppScreen.Servers,
    )

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        menuItems.forEach {
            MenuItem(it) {
                onNextButtonClicked(it)    
            }
        }
    }
}

@Composable
fun MenuItem(screen: AppScreen, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = screen.title,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}