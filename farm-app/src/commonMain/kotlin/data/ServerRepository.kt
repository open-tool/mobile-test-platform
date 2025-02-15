package data

import com.atiurin.atp.farmcore.entity.ServerInfo
import com.atiurin.atp.kmpclient.FarmClient

class ServerRepository (private val farmClient: FarmClient) {
    suspend fun getList(): List<ServerInfo> = farmClient.serverList()
}