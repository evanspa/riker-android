#!/bin/bash

emulatorNum="$1"

if [ -z "$emulatorNum" ]; then
    emulatorNum="5554"
fi

adb -s emulator-${emulatorNum} install -r ./mobile/development/debug/mobile-development-debug.apk
