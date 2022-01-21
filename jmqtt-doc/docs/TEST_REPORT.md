# Jmqtt 最新版功能及性能测试报告
线上版连接：https://www.yuque.com/tristan-ku8np/zze/xghq80
最新版链接：[https://github.com/Cicizz/jmqtt](https://github.com/Cicizz/jmqtt)

# 一、目标

1. 检测Jmqtt功能及性能运行情况
1. 为使用者提供参考

说明：以下测试为Jmqtt作者闲暇之时进行的测试，仅供参考，再线上使用Jmqtt时，请自行测试，感谢 朱霄 同学提供的服务器等测试资源

# 二、测试机配置

|                 | 数量        | 操作系统        | 配置                          |
| --------------- | ----------- | --------------- | ----------------------------- |
| Jmqtt运行服务器 | 2台（集群） | linux centos 7  | 8C16G                         |
| Jmqtt压测机     | 2台         | linux centos 7  | 8C16G                         |
| Mysql           | 单库单表    | mysql5.7        | 阿里云rds基础版：ESSD OL1云盘 |
| SLB             | 1           | 支持4层负载均衡 | 支持20万长连接                |

测试脚本：

1. jmeter
1. emqx-bench

# 三、功能测试报告

| 功能项                      | 是否满足 | 备注                                                      |
| --------------------------- | -------- | --------------------------------------------------------- |
| 集群连接                    | ✅        |                                                           |
| 集群cleansession为false连接 | ✅        |                                                           |
| 设备在集群互发消息          | ✅        |                                                           |
| retain消息                  | ✅        |                                                           |
| will消息                    | ✅        |                                                           |
| qos0                        | ✅        |                                                           |
| qos1                        | ✅        |                                                           |
| qos2                        | ✅        |                                                           |
| 离线消息                    | ✅        | 不限离线消息数量                                          |
| 设备信息持久化              | ✅        | 见sql表：jmqtt_session                                    |
| 订阅关系持久化              | ✅        | 见sql表：jmqtt_subscription                               |
| 消息持久化                  | ✅        | 见sql表：jmqtt_message                                    |
| 集群事件消息                | ✅        | 包含集群间转发消息，连接事件，见sql表：jmqtt_cluser_event |
| 设备消息接收状态            | ✅        | 见sql表：jmqtt_client_inbox                               |
| 监控topic                   | ❎        | 不支持，需自行实现                                        |
| 各个消息桥接                | ❎        | 不支持，需自行实现                                        |
| 规则引擎                    | ❎        | 不支持，需自行实现                                        |

# 四、单机性能测试报告

## 4.1 连接数性能报告

连接数单机测试达到10W级连接，连接tps1000，未出现报错，
注意：因测试机资源问题，未压测到上限。详细截图报告见下图

### 4.1.1 连接数200，连接持续3min

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746010873-3806f744-eebd-4a06-b638-a5c9030a913a.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=111&id=u3f625a5c&margin=%5Bobject%20Object%5D&name=image.png&originHeight=222&originWidth=1704&originalType=binary&ratio=1&rotation=0&showTitle=false&size=129250&status=done&style=none&taskId=u1e953a0b-157d-45d3-9bd0-886c7d1982c&title=&width=852)

### 4.1.2 连接数1000，连接持续3min

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746046914-ac14a7be-c248-4dfd-9244-3b3b6bf50bfd.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=114&id=ude67f41c&margin=%5Bobject%20Object%5D&name=image.png&originHeight=228&originWidth=1754&originalType=binary&ratio=1&rotation=0&showTitle=false&size=113132&status=done&style=none&taskId=u96d8e2f0-5487-4386-a565-467f58186d3&title=&width=877)

### 4.1.3 连接数2000，连接持续3min

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746065295-8a158413-36ac-4e88-b67c-1c6f6f565bc7.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=313&id=u67a9620e&margin=%5Bobject%20Object%5D&name=image.png&originHeight=626&originWidth=2236&originalType=binary&ratio=1&rotation=0&showTitle=false&size=390748&status=done&style=none&taskId=u44a649b4-aa01-4ad7-906e-e55f1b7f21e&title=&width=1118)

### 4.1.4 连接数5000，连接持续3min

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746088650-4e2c8fab-4185-41db-b406-89571492fb79.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=279&id=u8a9a70e7&margin=%5Bobject%20Object%5D&name=image.png&originHeight=558&originWidth=2192&originalType=binary&ratio=1&rotation=0&showTitle=false&size=257112&status=done&style=none&taskId=u68f785bb-6e9b-4668-9fa0-23ce4914e1a&title=&width=1096)

### 4.1.5 连接数1W，连接持续3min

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746108655-757645fb-6951-4dc4-ac6e-97946f35e980.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=42&id=u4b3542f2&margin=%5Bobject%20Object%5D&name=image.png&originHeight=84&originWidth=945&originalType=binary&ratio=1&rotation=0&showTitle=false&size=49950&status=done&style=none&taskId=ue2cf34cc-2cef-4ef0-88db-3826ecda806&title=&width=472.5)

### 4.1.6 连接数2W，连接持续3min

超过2W采用开源emqx-bench进行性能压测
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746146826-bc37501b-4b08-4566-a461-02ced5d72cfe.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=23&id=ue6ccf234&margin=%5Bobject%20Object%5D&name=image.png&originHeight=46&originWidth=634&originalType=binary&ratio=1&rotation=0&showTitle=false&size=22600&status=done&style=none&taskId=u33aaf8db-a098-40a5-8036-e6f2065e970&title=&width=317)

### 4.1.7 连接数5W，连接持续10min

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746200835-34532619-4370-4a54-9f57-607b6eb20916.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=20&id=ub9254472&margin=%5Bobject%20Object%5D&name=image.png&originHeight=39&originWidth=738&originalType=binary&ratio=1&rotation=0&showTitle=false&size=18207&status=done&style=none&taskId=ud0a3ce29-ca11-482e-83f5-71b4c49f78c&title=&width=369)

### 4.1.8 连接数10W，连接持续10min

压到10W后，未持续压测，尚未压到连接数上限，两台测试机截图如下：
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746254255-83829c1c-0ebc-46cb-a252-68febb726b2f.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=24&id=u55c0540d&margin=%5Bobject%20Object%5D&name=image.png&originHeight=48&originWidth=1491&originalType=binary&ratio=1&rotation=0&showTitle=false&size=43616&status=done&style=none&taskId=ue7fdb9c3-50d5-4e16-9f5e-c70b6b430b0&title=&width=745.5)
服务器load截图：
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642746280704-1784ae08-ffeb-4fdd-94f1-98206c444c54.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=70&id=udcb79ca0&margin=%5Bobject%20Object%5D&name=image.png&originHeight=140&originWidth=716&originalType=binary&ratio=1&rotation=0&showTitle=false&size=71157&status=done&style=none&taskId=u6a9c56d6-706e-4f0e-9813-5c3bf2a1adb&title=&width=358)

## 4.2 发送消息性能报告

### 4.2.1 设备连接2W，再启动1000连接持续发送消息

消息大小：256byte
qos：0
每隔10ms发送1条消息
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642747253961-4180a124-1185-4aac-8442-781b044fb609.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=251&id=ua5ae4796&margin=%5Bobject%20Object%5D&name=image.png&originHeight=302&originWidth=438&originalType=binary&ratio=1&rotation=0&showTitle=false&size=104022&status=done&style=none&taskId=u06b96fe2-eac6-404c-a96b-5f353408b1f&title=&width=364)
对比emq服务（没有2W长连接设备保持）：broker.emqx.io
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642747299016-50f125b1-fe67-4379-8858-37ac0cdec74b.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=203&id=u73d17b7e&margin=%5Bobject%20Object%5D&name=image.png&originHeight=255&originWidth=459&originalType=binary&ratio=1&rotation=0&showTitle=false&size=85760&status=done&style=none&taskId=u61141867-fb4f-4047-ae97-9400e851490&title=&width=364.5)

### 4.2.2 设备连接2W，再启动200连接持续发送消息

消息大小：256byte
qos：1
每隔10ms发送1条消息
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642747478891-b7ec2f4a-012d-4c9d-9dbc-f2bf7112cae8.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=203&id=ue0cc314a&margin=%5Bobject%20Object%5D&name=image.png&originHeight=284&originWidth=403&originalType=binary&ratio=1&rotation=0&showTitle=false&size=91808&status=done&style=none&taskId=u236295c7-4c40-4247-b975-e31b2558b3b&title=&width=287.5)

### 4.2.3 设备连接2W，再启动200连接持续发送消息

消息大小：100byte
qos：1
每隔10ms发送1条消息
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642747586254-5d5b40f9-4cef-4633-9a3d-94ab46837bd6.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=176&id=uf4feec24&margin=%5Bobject%20Object%5D&name=image.png&originHeight=301&originWidth=561&originalType=binary&ratio=1&rotation=0&showTitle=false&size=99190&status=done&style=none&taskId=u7428afd4-7a26-4449-9433-47fc1af1230&title=&width=327.5)

## 4.3 订阅性能报告

### 4.3.1 启动2W个设备，订阅2W个topic

### ![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642747707443-c70722d5-e4f6-4674-a9ab-3ba8490899d9.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=255&id=u22f47aba&margin=%5Bobject%20Object%5D&name=image.png&originHeight=333&originWidth=285&originalType=binary&ratio=1&rotation=0&showTitle=false&size=80501&status=done&style=none&taskId=u39a72cd5-3b32-4b86-9490-28e3815563d&title=&width=218.5) 

# 五、集群性能测试报告

## 5.1 连接数性能报告

Jmqtt服务器两台，设备连接数10W，未压测到上限
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642748056231-f1d72eeb-0dac-4e33-8ba2-0111e9f11387.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=314&id=u09e9c4d3&margin=%5Bobject%20Object%5D&name=image.png&originHeight=628&originWidth=1928&originalType=binary&ratio=1&rotation=0&showTitle=false&size=589189&status=done&style=none&taskId=ue4369d89-e4b7-4dac-979b-4514e76acdb&title=&width=964)

## 5.2 发送消息性能报告

发送消息强依赖db进行保存，性能瓶颈在db侧，故tps上不去

### 5.2.1 设备连接2W，启动200连接持续发送消息

消息大小：256byte
qos：1
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642748182652-85094ef1-740e-4067-85ac-1d089961994b.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=271&id=u5a016123&margin=%5Bobject%20Object%5D&name=image.png&originHeight=542&originWidth=658&originalType=binary&ratio=1&rotation=0&showTitle=false&size=239858&status=done&style=none&taskId=udb8c34ff-f596-4ef8-bedc-5343205e3ab&title=&width=329)

### 5.2.2 设备连接2W，启动200连接持续发送消息

消息大小：100byte
qos：1
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642748243207-6bd4b28d-818c-4f71-9759-11edfe7ffbac.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=251&id=ud1655849&margin=%5Bobject%20Object%5D&name=image.png&originHeight=502&originWidth=694&originalType=binary&ratio=1&rotation=0&showTitle=false&size=230671&status=done&style=none&taskId=u9e328faa-fd3f-48f8-884c-7104ba74757&title=&width=347)

## 5.3 订阅性能报告

### 5.3.1 启动2W个设备，订阅2W个topic

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642748307769-aba52973-ef70-4c53-b1d1-b0e8b81ab18f.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=203&id=ua71378ae&margin=%5Bobject%20Object%5D&name=image.png&originHeight=406&originWidth=920&originalType=binary&ratio=1&rotation=0&showTitle=false&size=143298&status=done&style=none&taskId=u0c19253a-4c2b-4ecc-adef-b7ea8893f21&title=&width=460)

### 5.3.2 启动5W个设备，订阅5W个topic

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642748324754-a92522f8-395d-4f70-a2f1-ff143bb3bb37.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=275&id=u048d5dd1&margin=%5Bobject%20Object%5D&name=image.png&originHeight=550&originWidth=718&originalType=binary&ratio=1&rotation=0&showTitle=false&size=183115&status=done&style=none&taskId=ue28001b0-b3a9-407d-9c6d-1fc15a813d3&title=&width=359)

# 六、性能测试说明

## 6.1 关于连接数

1. 单机和集群都未压测到上线，受限于时间和测试机问题
1. 实际使用时，单机连接数不要超过5w，方式服务器重启时，大量重连请求导致不可预知的问题

## 6.2 关于消息发送tps

1. 整体看消息tps与emq的测试服务器消息tps差不多
1. 为什么集群tps上不去？
   1. 因为消息保存强依赖mysql进行存储，mysql存储tps已达上限，这也是消息发送的可优化项

## 6.3 关于订阅

1. 目前订阅强依赖db存储，不存在订阅关系丢失的问题
1. 本地采用tri树进行订阅关系的管理

## 6.3 Jmqtt性能可优化项指南

1. 升级mysql
1. 集群事件转发器 用 mq替代（kafka或其他的mq都可以），减少集群服务器从db long pull的模式
1. 消息存储采用其他存储中间件，例如时序数据库，甚至kafka都行

# 七、测试常见问题

## 7.1 测试机需要修改端口限制，否则无法启动5W长连接

linux centos7默认限制了端口可用范围，需要修改一下，不然连接数无法达到5w
查看端口范围：cat /proc/sys/net/ipv4/ip_local_port_range

## 7.2 jmqtt服务器需要修改文件句柄数

linux 万物皆文件，需要修改文件句柄数，否则无法支持那么大的长连接
linux默认为65535个文件句柄数，需要修改两个地方：
ulimit -n 和vim /etc/security/limits.conf

## 7.3 jmqtt集群的负载均衡需要升级

mqtt协议的复杂均衡需要4层的负载均衡代理，
默认购买的SLB一般只支持5W长连接，故需要升级

# 八、附：Jmqtt启动问题

1. 目前jmqtt尽量减少各种依赖，代码简单，很容易进行二次开发和开箱即用
1. 请使用最新发布版本或master 分支代码
1. 建议从源码构建
1. 本地启动，直接在BrokerStartup执行main方法

## 8.1 结构介绍

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749307002-ed2ad593-44d3-4dac-a62e-f9db215e5fe5.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=312&id=u7cac4a06&margin=%5Bobject%20Object%5D&name=image.png&originHeight=1290&originWidth=1886&originalType=binary&ratio=1&rotation=0&showTitle=false&size=1369802&status=done&style=none&taskId=ua523a209-b0b0-4450-965a-f60ff3ca631&title=&width=456)

## 8.2 在db库中初始化好脚本
t默认使用的是mysql驱动，依赖其他db需要自行修改

1. 在自己的库中，执行jmqtt.sql
1. 执行后如截图所示：

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749458236-93fdeb96-9e87-40d7-9152-cdfd8dfbe1bb.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=309&id=ud2bd07e1&margin=%5Bobject%20Object%5D&name=image.png&originHeight=662&originWidth=594&originalType=binary&ratio=1&rotation=0&showTitle=false&size=150608&status=done&style=none&taskId=u74608728-bee1-4b58-acc8-e64fb9d1b8d&title=&width=277)

## 8.3 打包

在broker模块下，执行 ：mvn -Ppackage-all -DskipTests clean install -U  
打包后：
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749594691-b2ac7265-1947-4694-b834-8285f5acc1da.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=360&id=u9e9f6e8c&margin=%5Bobject%20Object%5D&name=image.png&originHeight=720&originWidth=1374&originalType=binary&ratio=1&rotation=0&showTitle=false&size=640584&status=done&style=none&taskId=ua743bfc9-2d04-4761-8c85-d8a5b18b6f5&title=&width=687)

## 8.4 修改配置文件

如截图：这里修改为自己的db连接串
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749722171-23d6363a-9bcd-425a-a682-c36d878cdca2.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=278&id=u60f8b443&margin=%5Bobject%20Object%5D&name=image.png&originHeight=1242&originWidth=2758&originalType=binary&ratio=1&rotation=0&showTitle=false&size=1430587&status=done&style=none&taskId=u54a006e0-9619-468f-9deb-2a45f16b9de&title=&width=617)

## 8.5 上传资源到服务器

1. 将jar，conf下的资源，bin下的脚本都上传到服务器：

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749874476-b29816f0-2c04-4ab5-bac2-2f771e8c4130.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=80&id=u809bdae2&margin=%5Bobject%20Object%5D&name=image.png&originHeight=160&originWidth=1314&originalType=binary&ratio=1&rotation=0&showTitle=false&size=131296&status=done&style=none&taskId=u3491c4a5-645f-4ae6-bc72-10edd16469b&title=&width=657)
其中 config为conf下的配置文件

2. 执行启动命令：./runbroker.sh jmqtt-broker-3.0.0.jar -h config/

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749926168-c1ff93fb-523f-4569-b6e6-231db8d3c50c.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=62&id=u440600e3&margin=%5Bobject%20Object%5D&name=image.png&originHeight=124&originWidth=1226&originalType=binary&ratio=1&rotation=0&showTitle=false&size=91843&status=done&style=none&taskId=ue93dc841-1df2-4ae5-bbf9-3cf8117dfc0&title=&width=613)

3. 查看启动日志：
   1. cd jmqttlogs
   1. tailf -200 brokerLog.log : 显示如下截图说明启动成功

![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642749978459-d4df9171-0c11-4e05-a877-dc3eeeed2fa8.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=168&id=ucb54e4fc&margin=%5Bobject%20Object%5D&name=image.png&originHeight=336&originWidth=2268&originalType=binary&ratio=1&rotation=0&showTitle=false&size=259176&status=done&style=none&taskId=uafca868b-afe8-45a8-b2bb-42fa7003c9e&title=&width=1134)
![image.png](https://cdn.nlark.com/yuque/0/2022/png/2726385/1642750011254-87d99be8-40f4-40a8-bbaf-8a3cb67f8f14.png#clientId=u63915b68-034c-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=67&id=u9268067d&margin=%5Bobject%20Object%5D&name=image.png&originHeight=134&originWidth=1308&originalType=binary&ratio=1&rotation=0&showTitle=false&size=61787&status=done&style=none&taskId=ub9ebb91e-a64c-4e63-8601-48e82931c6e&title=&width=654)

jmqt
