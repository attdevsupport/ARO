#!/bin/sh
echo $$
while :
do
	curTime=$(date +"%s")
	outFile=$1"/"$curTime
	top -n 1 > $outFile
	sleep 2
done
