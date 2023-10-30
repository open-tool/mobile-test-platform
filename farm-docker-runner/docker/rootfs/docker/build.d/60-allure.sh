wget -q https://github.com/allure-framework/allurectl/releases/download/$ALLURE_VERSION/allurectl_linux_amd64 -O ./allurectl && \
mkdir $APPS_DIR/allure
mv allurectl $APPS_DIR/allure/
chmod +x $APPS_DIR/allure/allurectl
ln -s $APPS_DIR/allure/allurectl /usr/local/bin/
