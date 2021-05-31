package com.atiurin.atp.farmserver

import com.atiurin.atp.farmserver.node.NodeRepository
import com.github.ajalt.clikt.core.CliktCommand
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.util.*

@SpringBootApplication
class FarmApplication : CliktCommand(){


	val initialDevices = listOf<DeviceInfo>(
		DeviceInfo(name = "android-api-27", api = 27, dockerImage = "android-api-27:latest")
	)
	@Bean
	fun createRepo(deviceRepository: NodeRepository) = CommandLineRunner{
		initialDevices.forEach { device ->
			DevicePool.join(createDevice(device))
		}
	}

	override fun run() {
		runApplication<FarmApplication>()
	}
}

fun main(args: Array<String>) = FarmApplication().main(args)

fun createDevice(info: DeviceInfo): FarmDevice {
	val containerOptions = mapOf(
		"ANDROID_ARCH" to "x86"
	)
	val container = GenericContainer<Nothing>(DockerImageName.parse(info.dockerImage)).apply {
		withLabels(containerOptions)
	}
	startContainer(container)
	val vncPort = container.getMappedPort(5900)
	val adbPort = container.getMappedPort(5555)
	val telnetPort = container.getMappedPort(5554)
	val socketPort = container.getMappedPort(5037)
	println("ip: ${container.host}, vncPort: $vncPort, adbPort: $adbPort, adbTelnet: $telnetPort, socketPort: $socketPort")
	return FarmDevice(
		UUID.randomUUID().toString(), info,
		ContainerInfo(vncPort = vncPort, adbPort = adbPort, telnetPort = telnetPort, adbServerSocketPort = socketPort, ip = container.host),
		container)
}

fun startContainer(container: GenericContainer<Nothing>){
	container.apply {
		println("Start container")
		withPrivilegedMode(true)
		withExposedPorts(5900, 5554, 5555, 5037)

//		withLogConsumer(Slf4jLogConsumer(App.logger))
//        withEnv(mapOf("ENABLE_VNC" to "true"))
		start()
		waitingFor(Wait.forHealthcheck())
		var emulStarted = false
		while (!emulStarted){
			val outLines = container.execInContainer("adb", "devices").stdout.reader().readLines()
			outLines.forEach { line ->
				if (line == "emulator-5554\tdevice") emulStarted = true
			}
		}
		println("Emulator started")
		var packageServiceStarted = false
		while (!packageServiceStarted){
			val outLines = container.execInContainer("adb",  "shell", "service", "check", "package").stdout.reader().readLines()
			outLines.forEach { line ->
				if (line == "Service package: found") packageServiceStarted = true
			}
		}
		println("Service package: found")
	}
}

fun getDeviceImageForApi(api: Int): String {
	return "android-api-$api:latest"
}