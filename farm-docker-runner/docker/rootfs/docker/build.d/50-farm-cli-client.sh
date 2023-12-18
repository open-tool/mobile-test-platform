unzip -q  farm-cli-client.zip -d $APPS_DIR/ && \
rm -rf farm-cli-client.zip  && \
ln -s $APPS_DIR/farm-cli-client-$APP_VERSION/bin/farm-cli-client /usr/local/bin/
