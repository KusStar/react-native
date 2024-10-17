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

function replace_gradle() {
  REWIND_REACT_GRADLE=$REWIND_DIR/node_modules/react-native/react.gradle

  echo "Removing $REWIND_REACT_GRADLE"

  rm $REWIND_REACT_GRADLE

  echo "Copying ./react.gradle to $REWIND_REACT_GRADLE"

  cp ./react.gradle $REWIND_REACT_GRADLE
}

function replace_hermesc() {
  REWIND_HERMES=$REWIND_DIR/node_modules/react-native/hermes-engine

  echo "Removing $REWIND_HERMES"

  rm -rf $REWIND_HERMES

  echo "Copying ./hermes-engine to $REWIND_HERMES"

  cp -r ./hermes-engine $REWIND_HERMES
}

if [ "$1" == "android" ]; then
  replace_android
elif [ "$1" == "libraries" ]; then
  replace_libraries
elif [ "$1" == "gradle" ]; then
  replace_gradle
else
  replace_android
  echo "\n"
  replace_libraries
  echo "\n"
  replace_gradle
  echo "\n"
  replace_hermesc
fi
