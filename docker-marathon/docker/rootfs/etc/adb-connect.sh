#!/bin/sh

if [[ -z "${DEVICE_HOSTS}" ]]; then
  echo "no devices is set"
else
  IFS=","
  for host in $DEVICE_HOSTS
  do
    echo "connect to $host"
    adb connect $host
  done
fi
IFS=" "
args="$@"
marathon $args