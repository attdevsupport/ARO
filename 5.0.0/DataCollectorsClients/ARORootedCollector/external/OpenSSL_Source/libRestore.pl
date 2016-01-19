# Use this script to restore the original libraries.
# Assumption: original libraries had been backed up in /sdcard/ARO/BACKUP_LIBS
#
use strict;
use warnings;

system("adb shell \"su -c 'mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system'\"");
system("adb shell \"su -c 'cat /sdcard/ARO/BACKUP_LIBS/libssl.so > /system/lib/libssl.so'\"");
system("adb shell \"su -c 'cat /sdcard/ARO/BACKUP_LIBS/libcrypto.so > /system/lib/libcrypto.so'\"");
system("adb shell \"su -c 'mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system'\"");

