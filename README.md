# Mobile Test Platform

The project provides a full infrastructure for Android UI tests. It has several parts:
- Server to manage Android emulators in Docker containers.
- CLI client to launch UI tests with server devices.
- Artifacts to build a Docker image with all required components: farm-cli-client, ADB, Marathon-runner, allurectl.
- Desktop UI application to monitor and manage devices.


## Architecture

*Note: Currently, only Android emulators in Docker containers are supported.*

![MobileTestPlatform](https://github.com/open-tool/mobile-test-platform/assets/12834123/268114d6-1f0d-4f82-9cb8-b9428dea2853)

## farm-server

Server Features
- Automatic Device Recreation: After each use, devices are automatically recreated to ensure a clean environment between test runs.
- Device Health Monitoring: The server continuously monitors device states and automatically recreates problematic devices.
- Idle Device Cleanup: The server tracks devices allocated to clients and releases them if they exceed the usage timeout. This behavior is controlled by the `device_busy_timeout` parameter.	
- Dynamic Reconfiguration: The server can be reconfigured on the fly without requiring a restart.
- Configurable Port Range: The server allows device connections within a specified port range. See the `start_port` and `end_port` parameters for configuration.
- Swagger: it could be found with URL [server_url]/swagger-ui/index.html
  
The server is a Kotlin Spring Boot application.

An example of building and launching the server application can be found in the `scripts/build-and-deploy-farm-server.sh` file.

To build the server application, run:

```shell
./gradlew clean :farm-server:distZip
```

To launch the server:
- Unzip the application archive.
- Add `farm-server-folder/bin` to the `$PATH` variable.
- Run the server with a command like the following:

```shell
farm-server --max_amount 5 -kad 30=2 --device_busy_timeout 1800 -i 30=us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2
```

### Server Parameters

When launching the farm-server, the following parameters can be used:

- `-m`, `--max_amount` (required): Maximum number of devices managed by the server.
- `-cbs`, `--max_device_creation_batch_size`: Maximum number of devices created in a batch (default: 10).
- `-kad`, `--keep_alive_devices`: A mapping of device groups to the number of devices that should always be kept alive. Example: `30=2` (Keep 2 devices alive for group 30).
- `-i`, `--img`: A mapping of device groups to emulator images. Example: `30=us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2`.
- `-dbt`, `--device_busy_timeout` (required): Time (in seconds) before a busy device is released.
- `-sp`, `--start_port`: Starting port number for device connections (default: 0).
- `-ep`, `--end_port`: Ending port number for device connections (default: 65534).
- `-adbp`, `--android_container_adb_path`: Path to the ADB binary inside the Android container.
- `-emp`, `--emulator_params`: Additional parameters for the emulator.
- `-emenv`, `--emulator_environment`: A mapping of environment variables for the emulator.
- `-md`, `--mock_device`: Flag that enables mock device mode. Only for developing and testing the farm server itself.

### Example command:

```shell
farm-server --max_amount 5 \
            --device_busy_timeout 1800 \
            --keep_alive_devices 30=5 \
            --keep_alive_devices 32=10 \
            -i 30=us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2 \
            -i 32=my_custom_image \
            --emulator_params "-no-window -gpu swiftshader_indirect" \
            --emulator_environment Variable1=value \
            --emulator_environment Variable2=value
```

## farm-cli-client

To build the CLI application, run:

```shell
./gradlew clean :farm-cli-client:distZip
```

The ZIP application archive is generated in `farm-cli-client/build/app/distributions`.

To launch the client:
- Unzip the application archive.
- Add `farm-cli-client-folder/bin` to the `$PATH` variable.
- Run the CLI client with a command like the following:

```shell
farm-cli-client --device_amount 2 -g 30 --user_agent nameOfYourProject --url http://localhost:8080 --marathon_config MarathonfileUltron
```

### Client Parameters

#### Main Parameters:
- `-u`, `--url`:(required) A list of farm server URLs the client can connect to. Default: `http://localhost:8080`. If your farm-server is not hosted on `http://localhost:8080`, add `--url [farm_server_hostname]` to the `farm-cli-client` command.
- `-c`, `--command`: The command to execute. Possible values: `RUN`, `ACQUIRE`, `RELEASE`. Default is `RUN`
- `-da`, `--device_amount` (required): The number of devices to acquire.
- `-g`, `--group_id` (required): The device group identifier.

#### Marathon Integration:
- `-mcmd`, `--marathon_command`: Command to run tests using Marathon. Default is `marathon `
- `-mc`, `--marathon_config`: Name/Path to the Marathon configuration file.
- `-mapv`, `--marathon_adb_port_variable`: Custom ADB server port. 

#### Allure Reporting:
- `-aw`, `--allure`, `--allure_watch`: Flag to enable Allure report monitoring.

#### Environment & Identification:
- `-e`, `--env`: A mapping of environment variables in the format `key=value`. Example: `-e VAR1=value1 -e VAR2=value2`.
- `-ua`, `--user_agent`: The identifier of the project or user initiating the command. If not specified, the GitLab project ID or `"test"` is used.

#### Timeouts:
- `-dct`, `--device_connection_timeout_sec` (default: `300`): The timeout for device connection in seconds.
- `-to`, `--timeout_sec` (default: `1800`): The overall timeout for command execution in seconds.

## Desktop App

The project supports a Compose Multiplatform App to manage devices and explore servers. Run the app with:

```shell
./gradlew :farm-app:desktopRun -DmainClass=MainKt
```

## Build Docker Runner Image

```shell
./scripts/build-docker-runner.sh
```

The resulting image contains:
- farm-cli-client
- ADB
- Java
- Marathon client
- allurectl

Try the runner image locally:

```shell
docker run -it --network host \
  --name farm-runner \
  -v "$(pwd)/artifacts-folder:/artifacts-folder" \
  android-runner:latest /bin/sh
```

`artifacts-folder` - folder that contains `app.apk`, `test-apk`, `MarathonfileName`.

The container console should be opened:

```shell
farm-cli-client --device_amount 2 --group_id 30 \
  --user_agent ProjectName \
  --url http://localhost:8080 \
  --marathon_config MarathonfileName
```

By default, `marathon_config` uses `Marathonfile`.

### Roadmap

- Desktop-App: Support for device details and device removal.
- Device Ping from client to server.
- K8s Support (?).

