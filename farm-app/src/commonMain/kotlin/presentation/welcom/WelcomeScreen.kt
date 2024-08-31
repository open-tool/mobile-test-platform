package presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atiurin.atp.kmpclient.FarmUrl
import com.atiurin.atp.kmpclient.getFarmUrlFromString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onContinueButtonClicked: (FarmUrl, String) -> Unit,
) {
    val enteredUrl = remember { mutableStateOf("") }
    val isUrlValid = remember { mutableStateOf(false) }
    val farmUrl = remember { mutableStateOf<FarmUrl?>(null) }
    val userName = remember { mutableStateOf("") }
    val canContinue = remember {
        derivedStateOf {
            isUrlValid.value && userName.value.isNotBlank()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7DD)), // Светлый фон
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Specify user name and farm URL in format protocol://host:port",
                fontSize = 16.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = userName.value,
                label = { Text("User Name") },
                onValueChange = { newValue ->
                    userName.value = newValue
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color(0xFF6200EE),
                    unfocusedIndicatorColor = Color(0xFFCCCCCC)
                )
            )
            TextField(
                value = enteredUrl.value,
                label = { Text("http://localhost:8080") },
                onValueChange = { newValue ->
                    enteredUrl.value = newValue
                    isUrlValid.value = try {
                        farmUrl.value = getFarmUrlFromString(newValue)
                        true
                    } catch (_: Exception) {
                        false
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color(0xFF6200EE),
                    unfocusedIndicatorColor = Color(0xFFCCCCCC)
                )
            )
            Button(
                onClick = { farmUrl.value?.let { onContinueButtonClicked(it, userName.value) } },
                enabled = canContinue.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUrlValid.value) Color(0xFF6200EE) else Color(0xFFBBBBBB),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}