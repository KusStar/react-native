set -e

rm -rf ReactAndroid/build/generated
rm -rf ReactAndroid/build/intermediates
rm -rf ReactAndroid/build/libs
rm -rf ReactAndroid/build/outputs
rm -rf ReactAndroid/build/react-ndk
rm -rf ReactAndroid/build/third-party-ndk
rm -rf ReactAndroid/build/tmp

./gradlew ReactAndroid:installArchives
