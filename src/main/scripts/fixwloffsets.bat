@echo off
set BASEDIR=%~d0%~p0
java -jar "%BASEDIR%\lib\wlandsuite.jar" -- fixwloffsets %1 %2 %3 %4 %5 %6 %7 %8 %9
