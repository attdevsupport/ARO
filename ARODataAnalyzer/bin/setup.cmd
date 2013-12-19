cls
:: Create the virtual WiFi
:: This cmd file is used to setup virtual wifi
netsh wlan set hostednetwork mode=allow ssid=AROROCKS key=arorocks
netsh wlan start hostednetwork & exit

::cd C:\emulatortrace\TEMP

:: Remove any existing PCAP
:: del *.pcap   & exit
