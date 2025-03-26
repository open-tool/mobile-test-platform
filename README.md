# Mobile Test Platform

It provides server to manage mobile devices. 

Currently supported only Android emulators in docker container.

Architecture:

![MobileTestPlatform](https://github.com/open-tool/mobile-test-platform/assets/12834123/268114d6-1f0d-4f82-9cb8-b9428dea2853)

## farm-server

to build server application launch

```shell
./gradlew clean :farm-server:distZip
```
The zip application archive is generated in `
`

To launch the server:
- unzip application archive 
- add `farm-server-folder/bin` folder to $PATH variable
- run server with command like following
```shell
farm-server --max_amount 5 -kad 30=2 --device_busy_timeout 1800 -i 30=us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2
```

## farm-cli-client

to build CLI application launch

```shell
./gradlew clean :farm-cli-client:distZip
```
The zip application archive is generated in `farm-cli-client/build/app/distributions`

To launch the server:
- unzip application archive
- add `farm-cli-client-folder/bin` folder to $PATH variable
- run cli client with command like following
```shell
farm-cli-client --device_amount 2 -g 30 --user_agent nameOfYourProject --url  http://localhost:8080 --marathon_config MarathonfileUltron
```

In case your farm-server is hosted not on `http://localhost:8080` add `--url farm_server_hostname` to `farm-cli-client` command.

## Desktop App

Project support Compose Multiplatform App to manage devices and explore servers. Run the app:

```shell
./gradlew :farm-app:desktopRun -DmainClass=MainKt
```

### Roadmap

- Desktop-App: device details support device removing 
- Device Ping from client to server
- K8s Support (?)

# Build docker runner image

```shell
./scripts/build-docker-runner.sh
```
The resulted image contains:
- farm-cli-client
- adb
- java
- marathon client
- allurectl

Try the runner image locally:
```shell
docker run -it --network host \
  --name farm-runner \
  -v "$(pwd)/artifacts-folder:/artifacts-folder" \
  android-runner:latest /bin/sh
```
`artifacts-folder` - folder that contains app.apk, test-apk, MarathonfileName

The container console should be opened:

```shell
farm-cli-client --device_amount 2 --group_id 30 \
  --user_agent ProjectName \
  --url  http://localhost:8080 \
  --marathon_config MarathonfileName
```

by default `marathon_config` uses value = `Marathonfile`