#!/bin/bash

deviceName="$1"

if [ -z "$deviceName" ]; then
    deviceName="84B5T15A10002892"
fi

adb -s ${deviceName} install -r ./mobile/production/debug/mobile-production-debug.apk
