# Build
```
docker build -t marathon .
```

# Run
create docker network
```
docker network create marathon_network
```

run
``` 
docker run -i -t  \
--name marathon \
--network marathon_network \
-v PATH_TO_MARATHON_FILE:/data \
-e DEVICE_HOSTS=EMULATOR_HOST1:PORT,EMULATOR_HOST2:PORT \
marathon:latest ADDITIONAL_PARAM_IF_NEED
```

PATH_TO_MARATHON_FILE must contain apk files and marathon file
