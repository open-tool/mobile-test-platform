APP_VERSION=$(cat gradle.properties | grep 'appVersion' | cut -d'=' -f2)
echo "building farm-cli-client $APP_VERSION"
./gradlew clean :farm-cli-client:distZip
cp farm-cli-client/build/app/distributions/farm-cli-client-$APP_VERSION.zip farm-docker-runner/docker/rootfs/$APPS_DIR/farm-cli-client.zip
cd farm-docker-runner/docker
if [ -z "$1" ]; then
  IMAGE_NAME="android-runner"
else
  IMAGE_NAME="$1"
fi
echo "Build docker image"
docker build --build-arg APP_VERSION=$APP_VERSION -t IMAGE_NAME .
