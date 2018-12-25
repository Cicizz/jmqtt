#### 说明
JMQTT是用java语言开发的支持mqtt协议的高可用，高性能，高可扩展性的broker，采用netty作为通信层组件，支持插件化开发。
#### 架构设计图
![架构图](jmqtt%20design.jpg)
#### 功能特性
1. 支持qos0,qos1,qos2消息。
2. 完整支持mqtt topic匹配过滤功能。
#### QuickStart
1. clone本项目
2. 输入`mvn -Ppackage-all -DskipTests clean install -U`打包
3.  进入jmqtt-distrubution/target/jmqtt/bin下，直接运行jmqttstart脚本即可
#### RoadMap
##### Version 2.x
1. 支持集群化，多主机横向扩展
2. 支持SSL/TLS
##### Version 1.x
1. 完整支持mqtt协议
2. 支持Websocket协议
3. 支持数据本地持久化
4. 支持redis存储