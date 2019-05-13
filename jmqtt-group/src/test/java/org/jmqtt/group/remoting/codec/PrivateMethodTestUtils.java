package org.jmqtt.group.remoting.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrivateMethodTestUtils {
    /**
     *
     */
    public static Object invoke(Object methodHostInstance, String methodName,
                                Object arg) {
        Class<?>[] parameterTypes = { arg.getClass() };
        Object[] args = { arg };
        return invoke(methodHostInstance, methodName, parameterTypes, args);
    }

    /**
     *
     * @param methodHostInstance  调用类示例 new xxx()
     * @param methodName          调用方法名 testmethod
     * @param parameterTypes      参数类型  [String.class]
     * @param args                参数列表 ["123456"]
     * @return
     */
    public static Object invoke(Object methodHostInstance, String methodName,
                                Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = methodHostInstance.getClass().getDeclaredMethod(
                    methodName, parameterTypes);
            method.setAccessible(true);
            try {
                return method.invoke(methodHostInstance, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            method.setAccessible(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
