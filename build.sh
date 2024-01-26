#!/bin/bash

set -e

MODE=$1

if [ "$MODE" == "all" ]; then
  echo "Cleaning full build"
  rm -rf ReactAndroid/build/generated
  rm -rf ReactAndroid/build/intermediates
  rm -rf ReactAndroid/build/outputs
  rm -rf ReactAndroid/build/libs
  rm -rf ReactAndroid/build/tmp
  rm -rf ReactAndroid/build/third-party-ndk
  rm -rf ReactAndroid/build/react-ndk
fi

if [ "$MODE" == "ndk" ]; then
  echo "Cleaning ndks build"
  rm -rf ReactAndroid/build/third-party-ndk
  rm -rf ReactAndroid/build/react-ndk
fi

if [ "$MODE" == "react" ]; then
  echo "Cleaning react-ndk build"
  rm -rf ReactAndroid/build/react-ndk
fi

echo "Start gradle build"
./gradlew ReactAndroid:installArchives
