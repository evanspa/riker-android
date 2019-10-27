#!/bin/bash

PACKAGE="com.rikerapp.riker.dev"
DB_FILE="riker.sqlite"
DB_FILE_ABS_PATH="/data/data/${PACKAGE}/databases/${DB_FILE}"

# https://stackoverflow.com/a/25566528/1034895
adb shell "run-as ${PACKAGE} chmod 666 ${DB_FILE_ABS_PATH}"
adb exec-out run-as ${PACKAGE} cat ${DB_FILE_ABS_PATH} > ${DB_FILE}
adb exec-out run-as ${PACKAGE} cat ${DB_FILE_ABS_PATH}-shm > ${DB_FILE}-shm
adb exec-out run-as ${PACKAGE} cat ${DB_FILE_ABS_PATH}-wal > ${DB_FILE}-wal
