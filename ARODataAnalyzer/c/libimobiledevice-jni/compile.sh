javaclass=../../classes/com/att/aro/libimobiledevice/ScreencaptureImpl.class
libdir=../../bin/libimobiledevice

if [ -d $libdir ]
then
    echo "Found dir: $libdir"
else
    echo "Missing libimobiledevice dir: $libdir"
    exit
fi

if [ ! -f "$javaclass" ]
then
    echo "Java class not found, compile class first: $javaclass"
    exit
fi

echo 'Everything is good to go, start compiling...'
if [ -f ScreencaptureBridge.h ]
then
    rm ScreencaptureBridge.h
fi

javah -o ScreencaptureBridge.h -classpath .:../../classes com.att.aro.libimobiledevice.ScreencaptureImpl

gcc -dynamiclib -I/usr/local/include -I/opt/local/include -I/System/Library/Frameworks/JavaVM.framework/Headers -limobiledevice ScreencaptureBridge.c -o libScreencaptureBridge.jnilib

if [ ! -f libScreencaptureBridge.jnilib ]
then
    echo "Failed to compile jni lib"
    exit
fi

install_name_tool -change /usr/local/lib/libimobiledevice.4.dylib @loader_path/libimobiledevice.4.dylib ./libScreencaptureBridge.jnilib

echo "Copying jnilib to bin dir: $libdir"

cp libScreencaptureBridge.jnilib "$libdir/libScreencaptureBridge.jnilib"

echo 'Done'
