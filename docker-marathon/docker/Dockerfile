FROM centos:7
ENV ANDROID_HOME=/opt/android-sdk-linux
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-11.0.11.0.9-1.el7_9.x86_64
COPY rootfs /
RUN docker/build
