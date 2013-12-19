:: Close the Virtual WiFi interface
Taskkill /IM WinDump.exe /F
netsh wlan stop hostednetwork
netsh wlan set hostednetwork mode=disallow & exit



