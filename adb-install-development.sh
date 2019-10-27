#!/bin/bash

deviceName="$1"

if [ -z "$deviceName" ]; then
    deviceName="84B5T15A10002892"
fi

adb -s ${deviceName} install -r ./mobile/build/outputs/apk/development/debug/mobile-development-debug.apk
