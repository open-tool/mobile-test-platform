mkdir -p /atp/marathon && \
wget -nv -O marathon.zip https://github.com/MarathonLabs/marathon/releases/download/0.6.2/marathon-0.6.2.zip && \
unzip -q  marathon.zip -d /atp/ && \
rm -rf marathon.zip  && \
ln -s /atp/marathon-0.6.2/bin/marathon /usr/local/bin/
