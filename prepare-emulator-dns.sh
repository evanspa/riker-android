#!/bin/bash
# Steps derived from: https://medium.com/@crysfel/updating-the-hosts-file-on-android-emulator-d61a533a76cf
adb -s emulator-5554 pull /system/etc/hosts hosts
echo "10.0.2.2 dev.rikerapp.com" >> hosts
adb -s emulator-5554 root
adb -s emulator-5554 remount
adb -s emulator-5554 push hosts /system/etc/hosts
