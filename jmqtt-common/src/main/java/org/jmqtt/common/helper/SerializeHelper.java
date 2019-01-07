package org.jmqtt.common.helper;

import com.alibaba.fastjson.JSONObject;


public class SerializeHelper {

    public static <T> byte[] serialize(T obj){
        if(obj instanceof String){
            return ((String) obj).getBytes();
        }
        return JSONObject.toJSONBytes(obj);
    }

    public static <T> T deserialize(byte[] bytes,Class<T> clazz){
        if(clazz == String.class){
            return (T) new String(bytes);
        }
        return JSONObject.parseObject(bytes,clazz);
    }

}
