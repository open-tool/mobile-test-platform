import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()
    
    fun setDeviceItem(uid: String){
        _uiState.update {
            it.copy(deviceItemId = uid)
        }
    }
    
    fun resetState(){
        _uiState.value = AppUiState()
    }
}

data class AppUiState(
    val deviceItemId: String = "",
    val userAgent: String = "Desktop App"
)