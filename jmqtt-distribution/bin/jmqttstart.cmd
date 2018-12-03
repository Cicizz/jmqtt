@echo off

if not exist "%JMQTT_HOME%\bin\runbroker.cmd" echo Please set the JMQTT_HOME variable in your environment! & EXIT /B 1

call "%JMQTT_HOME%\bin\runbroker.cmd" org.jmqtt.broker.BrokerStartup %*

IF %ERRORLEVEL% EQU 0 (
   ECHO "Broker starts OK"
)