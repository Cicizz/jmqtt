##  Jmqtt

**注意**：`master` 分支在开发时可能不稳定，导致打包的文件不能运行甚至不能打包，请使用 [releases](https://github.com/Cicizz/jmqtt/releases) 版本

![Jmqtt logo](jmqtt.png)

## 功能特性

* 基于Java及Netty开发，插件化模式，高性能，高扩展性
* 支持mqtt协议qos0，qos1，qos2消息质量服务
* 支持mqtt协议cleansession，retain，will等消息服务
* 完整支持mqtt Topic匹配过滤
* 支持websocket协议
* 支持RocksDB进行数据本地存储，数据高可靠



#### 架构设计图
![架构图](jmqtt%20design.jpg)
#### 功能特性
1. 支持qos0,qos1,qos2消息特性
2. 支持Rocksdb消息持久化
3. 支持多级Topic过滤匹配
4. 支持Websocket
5. 支持redis存储

#### QuickStart
1. clone本项目
2. 输入`mvn -Ppackage-all -DskipTests clean install -U`打包
3.  进入jmqtt-distrubution/target/jmqtt/bin下，直接运行jmqttstart脚本即可
#### RoadMap

##### Version 3.x

1. 支持简单运维功能
2. 支持RocketMQ Bridge
3. 支持Kafka Bridge
4. 支持$SYS Topic监控

##### Version 2.x（开发中）
1. 支持集群化，多主机横向扩展，实现高可用
2. 支持SSL/TLS
3. 支持安全认证
##### Version 1.0.0
1. 完整支持mqtt协议
2. 支持Websocket协议
3. 支持数据本地持久化
4. 支持redis存储

#### 欢迎关注公众号进行交流
![开发大小事](zze.jpg)