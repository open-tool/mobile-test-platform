FROM centos:7
ENV ANDROID_HOME=/opt/android-sdk-linux
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-11.0.11.0.9-1.el7_9.x86_64
ENV PATH $PATH:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/skins:${ANDROID_HOME}/cmdline-tools/bin
ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${ANDROID_HOME}/emulator/lib64/qt/lib:${ANDROID_HOME}/emulator/lib64/libstdc++:${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/gles_swiftshader
COPY rootfs /
RUN docker/build
ENTRYPOINT ["bash", "/etc/adb-connect.sh"]
CMD ["--marathonfile", "/data/Marathonfile"]