这里是存放iot mq所有的功能测试，性能测试脚本及测试结果
## 功能测试计划(基于集群)
### 1.连接功能项
1. 连接
2. 连接 with cleasession = false（mqtt）
3. 连接 with will 消息（mqtt）
4. 使用ws连接
5. 连接使用ssl
6. 重新发起连接
### 2.发布消息
1. 发布qos0消息（mqtt）
2. 发布qos1消息（mqtt）
3. 发布qos2消息（mqtt）
4. 发布retain消息（mqtt）
### 3. 订阅
1. 订阅普通topic
2. 订阅多级topic
3. 订阅收到retain消息（mqtt）
4. 发布订阅拉通测试发送消息
### 4. 关闭连接


## 性能测试计划
## 1. 同时在线连接数压测
1. 连接数分级：100连接数/单机，500连接数/单机，1000连接数/单机，5000连接数/单机，1W连接数/单机
2. 同时请求连接：10/s，50/s，100/s，200/s，500/s  -- 单机
3. 集群测试（2+服务器）：1W连接数/集群；5W连接数/集群；10W连接数/集群
4. 集群测试（2+服务器）：1000/s，2000/s
5. 集群测试（2+服务器）：同时在线10W设备，1000设备发送消息
## 2.发送消息TPS
消息字节大小：100byte[]
1. 单机：500/s，1000/s，5000/s，10000/s
2. 集群(2+台服务器)：10000/s，15000/s，20000/s
## 3.订阅
1. 单设备订阅topic：10topic/单设备，50topic/单设备，100topic/单设备
2. 单机订阅树：10topic/单设备，50topic/单设备，100topic/单设备：分别连接1000设备，5000设备，10000设备
## 4.消费消息
1. 集群测试：同时在线10W设备，1000设备消费消息




