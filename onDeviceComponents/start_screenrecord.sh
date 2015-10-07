#!/bin/bash
RECORD_PATH=$1/AtmosphereScreenRecords
STOP_FILE_NAME=log
INDEX=1

if [ -d "$RECORD_PATH" ]; then
    rm -rf "$RECORD_PATH"
fi
mkdir "$RECORD_PATH"

#Creates a log file
touch ${RECORD_PATH}/${STOP_FILE_NAME}

#Start a new screen record process every 3 minutes, while the log file exists
while [[ -f "${RECORD_PATH}/${STOP_FILE_NAME}" ]]
do
    /system/bin/screenrecord ${RECORD_PATH}/${INDEX}.mp4
    let INDEX++
done

#Creates a new log file
touch ${RECORD_PATH}/${STOP_FILE_NAME}

