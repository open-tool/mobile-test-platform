cd ..
APP_VERSION=$(cat gradle.properties | grep 'appVersion' | cut -d'=' -f2)
echo "building farm-cli-client $APP_VERSION"
#./gradlew clean :farm-cli-client:distZip
cp farm-cli-client/build/app/distributions/farm-cli-client-$APP_VERSION.zip farm-docker-runner/docker/rootfs/app
cd farm-docker-runner/docker
docker build -t marathon .
