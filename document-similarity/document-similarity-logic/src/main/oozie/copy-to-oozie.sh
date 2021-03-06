#!/bin/bash

TASK=$1
USER=$2

WORKFLOW_HDFS_DIR="/user/${USER}/workflows/${TASK}"
WORKFLOW_LOCAL_LIB_DIR=${TASK}/workflow/lib
WORKFLOW_LOCAL_PIG_DIR=${TASK}/workflow/pig

if [ ! -d "$WORKFLOW_LOCAL_LIB_DIR" ]; then
    mkdir ${WORKFLOW_LOCAL_LIB_DIR}
fi

if [ ! -d "$WORKFLOW_LOCAL_PIG_DIR" ]; then
    mkdir ${WORKFLOW_LOCAL_PIG_DIR}
fi

echo "Copying required libaries to ${TASK}/lib"
cp /usr/lib/pig/pig-0.9.2-cdh4.0.1.jar ${WORKFLOW_LOCAL_LIB_DIR}
cp ../../../../commons/target/commons-1.0-SNAPSHOT.jar  ${WORKFLOW_LOCAL_LIB_DIR}
cp ../../../../importers/target/importers-1.0-SNAPSHOT.jar ${WORKFLOW_LOCAL_LIB_DIR}
cp ../../../../disambiguation/target/disambiguation-1.0-SNAPSHOT.jar ${WORKFLOW_LOCAL_LIB_DIR}
cp ../../../../document-similarity/target/document-similarity-1.0-SNAPSHOT.jar ${WORKFLOW_LOCAL_LIB_DIR}

echo "Copying required pig scripts to ${TASK}"
cp ../pig/*.pig ${WORKFLOW_LOCAL_PIG_DIR}

echo "Recreating workflow data in HDFS"
hadoop fs -rm -r ${WORKFLOW_HDFS_DIR}
hadoop fs -mkdir ${WORKFLOW_HDFS_DIR}
echo "Putting current workflow data to HDFS"
hadoop fs -put ${TASK}/* ${WORKFLOW_HDFS_DIR}
