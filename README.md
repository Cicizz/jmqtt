**English** | [中文](README_CN.md)
#  Jmqtt

![Jmqtt logo](jmqtt.png)

## Features
* Full support of mqtt3.1.1 protocol
* Support data persistence and clustering based on MySQL
* Support friendly secondary development, plug-in development: cluster / storage / device connection, publish subscribe authentication
* Support tcp, websocket, SSL, WSS

## Official documents
[Official documents](https://arrogant95.github.io/jmqtt-docs/)

## Quick start
1. Download [release](https://github.com/Cicizz/jmqtt/releases) (Version above 3. X) Or `clone` this project
2. Execute in the broker directory:`mvn -Ppackage-all -DskipTests clean install -U`
3. Configuration file for configuration response:`/jmqtt-broker/resources/conf`
4. Execute the start command:`java -jar jmqtt-broker-3.0.0.jar -h ${conf文件目录}` -H is followed by the configuration file directory, which needs to contain jmqtt.properties And log4j2. XML

## Online trial
Server address: 81.69.46.38
TCP port: 1883
Websocket port: 8883
SSL port: 1884
WSS port: 8884

## QQ technology exchange group
![jmqtt技术交流群](jmqtt_qq2.png)

## test report
jmqtt-doc/docs/TEST_REPORT.md



