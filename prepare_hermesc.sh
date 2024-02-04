DOWNLOADS=./ReactAndroid/build/downloads

# get the latest hermes
HERMES_TAR=$(ls -l $DOWNLOADS | grep hermes | sort -k 5 -r | head -n 1 | awk '{print $9}')

if [ ! -d $DOWNLOADS/hermes ]; then
  mkdir $DOWNLOADS/hermes
fi
tar -xzf $DOWNLOADS/$HERMES_TAR -C $DOWNLOADS/hermes

# get the latest hermesc
HERMESC=$DOWNLOADS/hermes/package/osx-bin/hermesc

echo $HERMESC

# copy hermesc to ./

cp $HERMESC ./
