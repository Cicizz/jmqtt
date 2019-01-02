package org.jmqtt.common.helper;

import com.alibaba.fastjson.JSON;

public class SerializeHelper {

    public static <T> byte[] serialize(T obj){
        return JSON.toJSONBytes(obj);
    }

    public static <T> T deserialize(byte[] bytes,Class<T> clazz){
        return JSON.parseObject(bytes,clazz);
    }
}
