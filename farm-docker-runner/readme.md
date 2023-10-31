# Build
```
mobile-test-platform/scripts/build-docker-runner.sh
```

# Run

run
``` 
docker run -i -t  \
--name android-runner \
-v PATH_TO_MARATHON_FILE:/data \
android-runner:latest ADDITIONAL_PARAM_IF_NEED
```

PATH_TO_MARATHON_FILE must contain apk files and marathon file
