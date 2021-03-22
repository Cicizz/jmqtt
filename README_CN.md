[English](README.md) | **中文**
##  Jmqtt

![Jmqtt logo](jmqtt.png)

## 功能特性
* 完整支持mqtt3.1.1协议
* 支持基于mysql的数据持久化和集群
* 支持友好的二次开发，插件化开发：集群/存储/设备连接，发布订阅认证
* 支持tcp, websocket,ssl,wss
* 支持基于redis的集群，性能更高
* 支持基于内存的启动，不支持数据持久化和集群，适合嵌入式启动
* 支持Spring boot jmqtt starter,可快速结合spring boot启动
* 支持完整的测试用例

## 官方文档
[官方文档](http://www.mangdagou.com/)

## 快速开始
1. 下载 [release](https://github.com/Cicizz/jmqtt/releases)(3.x以上版本) 或`clone`本项目
2. 在根目录执行：`mvn -Ppackage-all -DskipTests clean install -U`
3. 配置配置文件并初始化db的sql:`/jmqtt-broker/resources/conf`目录下
4. 执行启动命令：`java -jar jmqtt-broker-3.0.0.jar -h ${conf文件目录}` -h后是配置文件目录，里面需要包含jmqtt.properties和log4j2.xml等配置文件

## 线上试用
服务器地址：81.69.46.38

tcp端口：1883

websocket端口：8883

ssl端口：1884

wss端口：8884

## 技术交流群
欢迎小伙伴们给个star并使用。

![jmqtt技术交流群](jmqtt_qq.png)
## 微信公众号
关注我，一起学习成长

![vx_mangdagou](vx_mangdagou.jpg)
