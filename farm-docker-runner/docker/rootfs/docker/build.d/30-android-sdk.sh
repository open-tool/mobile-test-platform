mkdir -p ${ANDROID_HOME}/platforms && \
mkdir -p ${ANDROID_HOME}/platform-tools && \
mkdir -p ${ANDROID_HOME}/cmdline-tools && \

wget -nv -O platform-tools.zip https://dl.google.com/android/repository/platform-tools-latest-linux.zip && \
unzip -q platform-tools.zip -d ${ANDROID_HOME} && \
rm -rf platform-tools.zip

wget -nv -O tools.zip https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip && \
unzip -q tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
rm -rf tools.zip