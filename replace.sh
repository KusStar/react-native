#!/bin/bash

REWIND_DIR=$(grep -E '^rewind\.dir=' local.properties | awk -F '=' '{print $2}')

echo $REWIND_DIR

REWIND_MODULES_RN_ANDROID=$REWIND_DIR/node_modules/react-native/android

echo "Removing $REWIND_MODULES_RN_ANDROID"

rm -rf $REWIND_MODULES_RN_ANDROID

echo "Copying ./android to $REWIND_MODULES_RN_ANDROID"

cp -r ./android $REWIND_MODULES_RN_ANDROID

cat $REWIND_MODULES_RN_ANDROID/com/facebook/react/react-native/maven-metadata.xml.md5
