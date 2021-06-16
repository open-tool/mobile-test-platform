mkdir -p /tango/marathon && \
wget -nv -O marathon.zip https://github.com/MarathonLabs/marathon/releases/download/0.6.2/marathon-0.6.2.zip && \
unzip -q  marathon.zip -d /tango/ && \
rm -rf marathon.zip  && \
ln -s /tango/marathon-0.6.2/bin/marathon /usr/local/bin/
