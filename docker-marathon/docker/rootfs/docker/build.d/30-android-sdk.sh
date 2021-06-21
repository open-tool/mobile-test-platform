mkdir -p ${ANDROID_HOME}/platforms && \
mkdir -p ${ANDROID_HOME}/platform-tools && \
mkdir -p ${ANDROID_HOME}/cmdline-tools && \
mkdir -p ${ANDROID_HOME}/system-images/android && \
mkdir -p ${ANDROID_HOME}/system-images/android-30/google_apis/x86_64/ && \

wget -nv -O platform-tools.zip https://dl.google.com/android/repository/platform-tools-latest-linux.zip && \
unzip -q platform-tools.zip -d ${ANDROID_HOME} && \
rm -rf platform-tools.zip

wget -nv -O sys-img.zip https://dl.google.com/android/repository/sys-img/google_apis/x86-30_r09.zip && \
unzip -q sys-img.zip -d ${ANDROID_HOME}/system-images/android-30/google_apis/ && \
rm -rf sys-img.zip

wget -nv -O emulator.zip  https://dl.google.com/android/repository/emulator-linux_x64-7324830.zip && \
unzip -q emulator.zip -d ${ANDROID_HOME} && \
rm -rf emulator.zip

wget -nv -O tools.zip https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip && \
unzip -q tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
rm -rf tools.zip

