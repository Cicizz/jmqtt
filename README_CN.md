[English](README.md) | **中文**
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

## 快速开始

### 在线测试
在线测试环境：`120.24.69.113`，TCP端口：`1883`；Websocket端口：`1884`，欢迎使用！

### 直接本地运行
1. 下载 [release](https://github.com/Cicizz/jmqtt/releases) 或`clone`本项目
2. 在根目录执行：`mvn -Ppackage-all -DskipTests clean install -U`
3. （v1.1.0的执行脚本存在bug，因此需要手动添加一下）添加系统环境变量：key是`JMQTT_HOME`，value是`YOUR_PATH_TO_JMQTT\jmqtt-distribution\target\jmqtt`，配置的目的是指定jmqtt配置文件和日志配置文件所在的地址。
4. 在 `jmqtt-distrubution/target/jmqtt/bin` 目录下直接运行 `jmqttstart`脚本即可

### 用IDE调试
以IDEA为例：
1. 下载 [release](https://github.com/Cicizz/jmqtt/releases) 或`clone`本项目
2. 用IDEA打开，并选择“Add as a maven project”
3. 运行`jmqtt-broker`下面的`BrokerStartup`启动类，并添加运行环境变量：key是`JMQTT_HOME`，value是`YOUR_PATH_TO_JMQTT\jmqtt-distribution`,因为需要加载这个目录下的conf配置文件。

## 架构设计图

![架构图](jmqtt%20design.jpg)
## 模块简介及本地环境

* **broker**：mqtt协议层，逻辑处理，BrokerStartup为启动类，BrokerController为初始化类，初始化所有的必备环境，其中acl，store的插件配置也必须在这里初始化
* **common**：公共层，存放工具类，bean类等
* **remoting**：通信层，连接管理，协议解析，心跳等
* **distribution**：配置模块，主要是配置文件，启停命令等存放
* **example**：客户端示例，目前只有java以及websocket
* **group**：集群管理模块：消息传输，集群管理，以及相关运维功能实现
* **store**：存储模块，提供了mqtt协议数据的几个接口，支持基于内存的和Rocksdb的本地存储

## RoadMap

### Version 3.x

1. 支持简单运维功能
2. 支持RocketMQ Bridge
3. 支持Kafka Bridge
4. 支持$SYS Topic监控

### Version 2.x

1. 支持集群化，多主机横向扩展，实现高可用
2. 支持SSL/TLS
3. 支持安全认证

### Version 1.1.0

1. 添加connect，publish，subsribe权限认证接口，可插件化
2. 移除Redis存储
3. 优化Rocksdb本地存储，现在性能提高了很多，并且容易管理
4. 修复订阅的bug
5. 修复离线消息不能接收的bug
6. 修复retain消息偶尔接收不到的bug
7. 添加storeLog，remotingLog，messageTraceLog，clientTraceLog记录日志

### Version 1.0.0

1. 完整支持mqtt协议
2. 支持Websocket协议
3. 支持数据本地持久化

## 技术交流群

![jmqtt技术交流群](jmqtt_qq.png)