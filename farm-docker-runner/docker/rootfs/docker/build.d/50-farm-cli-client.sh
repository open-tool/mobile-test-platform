#wget -nv -O marathon.zip https://github.com/MarathonLabs/marathon/releases/download/$MARATHON_VERSION/marathon-$MARATHON_VERSION.zip && \
unzip -q  farm-cli-client.zip -d $APPS_DIR/ && \
rm -rf farm-cli-client.zip  && \
ln -s $APPS_DIR/farm-cli-client-$APP_VERSION/bin/farm-cli-client /usr/local/bin/
