echo "Install packages"
apk update && apk add --no-cache \
    openjdk${JAVA_VERSION} \
    python3 \
    wget \
    unzip \
    libc6-compat
echo "Packages installed"
