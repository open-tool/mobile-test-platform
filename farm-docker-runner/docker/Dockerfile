FROM centos:7
ENV ANDROID_HOME=/opt/android-sdk
ENV MARATHON_VERSION=0.8.4
ENV FARM_CLI_CLIENT_VErSION=0.1.4
ENV JAVA_HOME=/usr/lib/jvm/jre-11-openjdk
ENV PATH=$PATH:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/skins:${ANDROID_HOME}/cmdline-tools/bin
ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${ANDROID_HOME}/emulator/lib64/qt/lib:${ANDROID_HOME}/emulator/lib64/libstdc++:${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/gles_swiftshader
COPY rootfs /
RUN chmod +x /app/entrypoint.sh
RUN docker/build
ENTRYPOINT ["/app/entrypoint.sh"]
# CMD ["--marathonfile", "/data/Marathonfile"]