package org.jmqtt.support.log;

import org.slf4j.Logger;

public class LogUtil {

    public static void debug(Logger log,String desc,Object... param){
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug(desc,param);
            }
        }
    }

    public static void info(Logger log,String desc,Object... param){
        if (log != null) {
            if (log.isInfoEnabled()) {
                log.info(desc,param);
            }
        }
    }

    public static void warn(Logger log,String desc,Object... param){
        if (log != null) {
            if (log.isWarnEnabled()) {
                log.warn(desc,param);
            }
        }
    }

    public static void error(Logger log,String desc,Object... param){
        if (log != null) {
            if (log.isErrorEnabled()) {
                log.error(desc,param);
            }
        }
    }
}
