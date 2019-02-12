package org.jmqtt.common.helper;

import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;


public class MixAll {

    public static String MQTT_VERSION_SUPPORT = "mqtt, mqtt3.1, mqtt3.1.1";

    public static boolean createIfNotExistsDir(File file){
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static void printProperties(Logger log,Object obj){
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if(fields != null){
            for(Field field : fields){
                try {
                    field.setAccessible(true);
                    String key = field.getName();
                    Object value = field.get(obj);
                    log.info("{} = {}",key,value);
                } catch (IllegalAccessException e) {
                }
            }
        }
    }

    /**
     * transfer properties 2 pojo
     */
    public static void properties2POJO(Properties properties,Object obj){
        Method[] methods = obj.getClass().getMethods();
        if(methods != null){
            for(Method method : methods){
                String methodName = method.getName();
                if(methodName.startsWith("set")){
                    try{
                        String tmp = methodName.substring(4);
                        String firstChar = methodName.substring(3,4);
                        String key = firstChar.toLowerCase() + tmp;
                        String value = properties.getProperty(key);
                        if(value != null){
                            Class<?>[]  types = method.getParameterTypes();
                            if(types != null && types.length > 0){
                                String type = types[0].getSimpleName();
                                Object arg = null;
                                if(type.equals("int") || type.equals("Integer")){
                                    arg = Integer.parseInt(value);
                                }else if(type.equals("float") || type.equals("Float")){
                                    arg = Float.parseFloat(value);
                                }else if(type.equals("double") || type.equals("Double")){
                                    arg = Double.parseDouble(value);
                                }else if(type.equals("long") || type.equals("Long")){
                                    arg = Long.parseLong(value);
                                }else if(type.equals("boolean") || type.equals("Boolean")){
                                    arg = Boolean.parseBoolean(value);
                                }else if(type.equals("String")){
                                    arg = value;
                                }else{
                                    continue;
                                }
                                method.invoke(obj,arg);
                            }
                        }

                    }catch (Exception ex){
                    }
                }
            }
        }
    }
}
