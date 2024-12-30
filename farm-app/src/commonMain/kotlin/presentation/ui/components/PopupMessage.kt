package presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PopupMessage(
    message: String = "Popup",
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0x80000000))
                .clickable(onClick = { onDismiss(); isVisible = false }),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { /* Do nothing to prevent dismiss on click inside */ }
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Close",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            onDismiss()
                            isVisible = false
                        }
                    )
                }
            }
        }
    }
}