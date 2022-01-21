[English](README.md) | **中文**
##  Jmqtt

![Jmqtt logo](jmqtt.png)

## 功能特性
* 完整支持mqtt3.1.1协议
* 支持基于mysql的数据持久化和集群
* 支持友好的二次开发，插件化开发：集群/存储/设备连接，发布订阅认证
* 支持tcp, websocket,ssl,wss

## 官方文档
[官方文档](http://www.mangdagou.com/)

## 快速开始
1. 下载 [release](https://github.com/Cicizz/jmqtt/releases)(3.x以上版本) 或`clone`本项目
2. 在broker模块下执行：`mvn -Ppackage-all -DskipTests clean install -U`
3. 配置配置文件并初始化db的sql:`/jmqtt-broker/resources/conf`目录下
4. 执行启动命令：`java -jar jmqtt-broker-3.0.0.jar -h ${conf文件目录}` -h后是配置文件目录，里面需要包含jmqtt.properties和log4j2.xml等配置文件

## 技术交流群
欢迎小伙伴们给个star并使用。

![jmqtt技术交流群](jmqtt_qq.png)

## 测试报告
https://www.yuque.com/tristan-ku8np/zze/xghq80

## 基于蚂蚁技术栈的消息服务
1. 提供iot端完整消息服务，接入设备管理&监控等
2. 结合蚂蚁智能能力(对话机器人，智能助理，实时音视频)等，提供端智能终端解决方案，提供IM+IoT消息解决方案
3. 完全免费，欢迎使用。感兴趣的同学欢迎联系我：钉钉号：zhanze2013 或 qq私聊我（jmqtt群群主）
![蚂蚁ccm_im_iot_消息服务](ccm_im_iot_msg_service.png)

