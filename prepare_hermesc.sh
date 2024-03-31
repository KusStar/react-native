DOWNLOADS=./ReactAndroid/build/downloads

# get the latest hermes
HERMES_TAR=$(ls -l $DOWNLOADS | grep hermes | sort -k 5 -r | head -n 1 | awk '{print $9}')

if [ ! -d $DOWNLOADS/hermes ]; then
  mkdir $DOWNLOADS/hermes
fi
tar -xzf $DOWNLOADS/$HERMES_TAR -C $DOWNLOADS/hermes

# get the latest hermesc
OSX_HERMESC=$DOWNLOADS/hermes/package/osx-bin
LINUX_HERMESC=$DOWNLOADS/hermes/package/linux64-bin

# copy hermesc to ./

TARGET=./hermes-engine

if [ ! -d $TARGET ]; then
  mkdir $TARGET
fi

cp -r $OSX_HERMESC $TARGET
cp -r $LINUX_HERMESC $TARGET
