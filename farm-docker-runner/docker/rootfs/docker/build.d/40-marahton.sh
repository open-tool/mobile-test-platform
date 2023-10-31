wget -nv -O marathon.zip https://github.com/MarathonLabs/marathon/releases/download/$MARATHON_VERSION/marathon-$MARATHON_VERSION.zip && \
unzip -q  marathon.zip -d $APPS_DIR/ && \
rm -rf marathon.zip  && \
ln -s $APPS_DIR/marathon-$MARATHON_VERSION/bin/marathon /usr/local/bin/