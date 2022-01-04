## 快速上手

### 安装主题

1. 下载 [release](https://github.com/Cicizz/jmqtt/releases)(3.x以上版本) 或`clone`本项目：
2. 在jmqtt根目录执行：：
```bash
mvn -Ppackage-all -DskipTests clean install -U
```
3. 配置相应的配置文件,初始化db的sql文件:`/jmqtt-broker/resources/conf`目录下
4. 执行启动命令：`java -jar jmqtt-broker-3.0.0.jar -h ${conf文件目录}` -h后是配置文件目录，里面需要包含jmqtt.properties和log4j2.xml等配置文件

### 测试
下载客户端：[mqtt客户端](https://mqttx.app/cn/)
或 直接使用websocket测试：`/jmqtt/jmqtt-examples`




<p>&nbsp; </p>  

[我也想为贡献者之一？](https://github.com/Cicizz/jmqtt/pulls)

<p>&nbsp; </p> 


<Msg />
