#!/bin/bash

set -e

USE_JAVA_11=false
if [ -z "$JAVA_HOME" ]; then
  USE_JAVA_11=true
elif [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_MAJOR=$("$JAVA_HOME/bin/java" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)
  if [ "$JAVA_MAJOR" = "1" ]; then
    JAVA_MAJOR=8
  fi
  if [ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -ge 17 ]; then
    USE_JAVA_11=true
  fi
fi

if [ "$USE_JAVA_11" = true ] && [ -x /usr/libexec/java_home ]; then
  JAVA_11_HOME=$(/usr/libexec/java_home -v 11 2>/dev/null || true)
  if [ -n "$JAVA_11_HOME" ]; then
    export JAVA_HOME="$JAVA_11_HOME"
  fi
fi

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
