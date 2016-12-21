#!/bin/bash
RECORD_PATH=$1/AtmosphereScreenRecords
PROCESS_NAME_PREFIX=$2
STOP_FILE_NAME=log

if [ -n "$(ps ${PROCESS_NAME_PREFIX}screenrecord | grep -Eo [0-9]+ | grep -m 1 -Eo [0-9]+)" ]
then
    #Removes the log file created by the start script
    rm ${RECORD_PATH}/${STOP_FILE_NAME}

    kill -SIGINT $(ps ${PROCESS_NAME_PREFIX}screenrecord | grep -Eo [0-9]+ | grep -m 1 -Eo [0-9]+)

    #Waits the start script to create a log file after the screen record process is done
    while [[ ! -f "$RECORD_PATH/$STOP_FILE_NAME" ]]
    do
        sleep 1
    done

    #Removes the log file
    rm ${RECORD_PATH}/${STOP_FILE_NAME}

    #Lists all screen records that were made
    ls $RECORD_PATH
fi 