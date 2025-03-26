FROM alpine:latest
# versions
ARG APP_VERSION=0.0.1
ENV MARATHON_VERSION=0.10.3
ENV ALLURE_VERSION=2.15.1
ENV JAVA_VERSION=17
ENV APPS_DIR=/apps
RUN echo $APP_VERSION
# paths
ENV ANDROID_HOME=/opt/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk
ENV PATH=$PATH:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/skins:${ANDROID_HOME}/cmdline-tools/bin
ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/lib:/usr/lib:/usr/local/lib
COPY rootfs /
RUN /docker/build
CMD ["/bin/sh"]
