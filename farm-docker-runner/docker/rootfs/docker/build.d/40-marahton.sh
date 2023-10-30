mkdir -p /atp/marathon && \
wget -nv -O marathon.zip https://github.com/MarathonLabs/marathon/releases/download/$MARATHON_VERSION/marathon-$MARATHON_VERSION.zip && \
unzip -q  marathon.zip -d /atp/ && \
rm -rf marathon.zip  && \
ln -s /atp/marathon-$MARATHON_VERSION/bin/marathon /usr/local/bin/
