<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>JMQTT</title>
    <link rel="stylesheet" type="text/css" href="index/css/style.css"/>
</head>
<body>
<div class="auto-box">
    <div class="main-box">
        <div class="index-tit">
            <h1>JMQTT</h1>
            <p>基于Java及Netty开发，插件化模式，高性能，高扩展性</p>
        </div>
        <ul class="index-tserver">
            <li class="tserver-list1">
                足够轻
                <p class="animated zoomin">
                    <a href="#">使用Netty开发,高性能，高扩展,以redis为中央存储的集群(可用其他中央存储替换)</a>
                </p>
            </li>
            <li class="tserver-list2">
                足够快
                <p class="animated zoomin">
                    <a href="#">Nio或者Epoll模型+Netty天生快</a>
                </p>
            </li>
            <li class="tserver-list3">
                数据高可靠
                <p class="animated zoomin">
                    <a href="#">RocksDB进行数据本地存储，数据高可靠</a>
                </p>
            </li>
            <li class="tserver-list4">
                支持mqtt协议
                <p class="animated zoomin">
                    <a href="#">支持mqtt协议qos,cleansession,retain,will等消息服务</a>
                </p>
            </li>
            <li class="tserver-list5">
                支持websocket协议
                <p class="animated zoomin">
                    <a href="#">完整支持mqtt Topic匹配过滤</a>
                </p>
            </li>
            <li class="tserver-list6">
                高性能动态配置
                <p class="animated zoomin">
                    <a href="#">支持Http Api动态配置,使用HServer Http高性能服务</a>
                </p>
            </li>
        </ul>

        <div class="index-foot">
            <button type="button" class="button_foot" onclick="goLoginPage()" style="width:100px; height:50px;">即刻前往
            </button>
        </div>
    </div>
</div>

<script>
    function goLoginPage() {
        window.location = 'login.html';
    }
</script>
</body>
</html>
