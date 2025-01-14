package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.util.NetUtil
import com.atiurin.atp.farmserver.util.nowSec
import java.util.UUID

abstract class AbstractDevicePool: DevicePool {

    fun initDevice(groupId: String, ip: String = NetUtil.localhostName) = initDevice(DeviceInfo("AutoLaunched group '$groupId'", groupId), ip)

    fun initDevice(deviceInfo: DeviceInfo, ip: String = NetUtil.localhostName) = FarmPoolDevice(
        device = FarmDevice(
            id = UUID.randomUUID().toString(),
            deviceInfo = deviceInfo,
            state = DeviceState.CREATING,
            stateTimestampSec = nowSec(),
            containerInfo = ContainerInfo(ip, 0, 0, "")
        ),
        status = DeviceStatus.FREE,
        statusTimestampSec = nowSec(),
        desc = "Creating new device"
    )
}