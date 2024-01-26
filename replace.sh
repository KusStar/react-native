#!/bin/bash

REWIND_DIR=$(grep -E '^rewind\.dir=' local.properties | awk -F '=' '{print $2}')

echo "rewind dir: $REWIND_DIR"

function replace_android {
  REWIND_MODULES_RN_ANDROID=$REWIND_DIR/node_modules/react-native/android

  echo "Removing $REWIND_MODULES_RN_ANDROID"

  rm -rf $REWIND_MODULES_RN_ANDROID

  echo "Copying ./android to $REWIND_MODULES_RN_ANDROID"

  cp -r ./android $REWIND_MODULES_RN_ANDROID

  cat $REWIND_MODULES_RN_ANDROID/com/facebook/react/react-native/maven-metadata.xml.md5
}

function replace_libraries() {
  REWIND_MODULES_RN_LIBRARIES=$REWIND_DIR/node_modules/react-native/Libraries

  echo "Removing $REWIND_MODULES_RN_LIBRARIES"

  rm -rf $REWIND_MODULES_RN_LIBRARIES

  echo "Copying ./Libraries to $REWIND_MODULES_RN_LIBRARIES"

  cp -r ./Libraries $REWIND_MODULES_RN_LIBRARIES
}

if [ "$1" == "android" ]; then
  replace_android
elif [ "$1" == "libraris" ]; then
  replace_libraries
else
  replace_android
  echo "\n"
  replace_libraries
fi
