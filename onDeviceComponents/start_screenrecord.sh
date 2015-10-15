#!/bin/bash
RECORD_PATH=$1/AtmosphereScreenRecords
TIME_LIMIT=$2
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
    if [ $TIME_LIMIT -le 180 ]; then
        /system/bin/screenrecord --time-limit $TIME_LIMIT ${RECORD_PATH}/${INDEX}.mp4
        rm "${RECORD_PATH}/${STOP_FILE_NAME}"
    else
        /system/bin/screenrecord ${RECORD_PATH}/${INDEX}.mp4
        TIME_LIMIT=$((TIME_LIMIT - 180))
    fi
    let INDEX++
done

#Creates a new log file
touch ${RECORD_PATH}/${STOP_FILE_NAME}

