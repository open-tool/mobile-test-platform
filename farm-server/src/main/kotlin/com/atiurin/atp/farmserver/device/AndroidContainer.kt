package com.atiurin.atp.farmserver.device

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class AndroidContainer<SELF : GenericContainer<SELF>>(image: DockerImageName) : GenericContainer<SELF>(image) {
    companion object {
        const val ADB_PORT = 5555
        const val GRPC_PORT = 8554
    }

    public override fun addFixedExposedPort(hostPort: Int, containerPort: Int) {
        super.addFixedExposedPort(hostPort, containerPort)
    }

    fun exposeAdbPort(hostPort: Int) = addFixedExposedPort(hostPort, ADB_PORT)
    fun exposeGrpcPort(hostPort: Int) = addFixedExposedPort(hostPort, GRPC_PORT)

    fun getHostAdbPort() = getMappedPort(ADB_PORT)
    fun getHostGrpcPort() = getMappedPort(GRPC_PORT)
}