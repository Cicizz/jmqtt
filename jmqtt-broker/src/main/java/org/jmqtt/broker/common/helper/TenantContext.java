package org.jmqtt.broker.common.helper;

import org.jmqtt.broker.common.model.TenantInfo;

public class TenantContext {


    private static final ThreadLocal<TenantInfo> authLocal = new ThreadLocal<>();

    public static TenantInfo getAuthInfo(){
        return authLocal.get();
    }

    public static void setAuthInfo(TenantInfo tenantInfo){
        authLocal.set(tenantInfo);
    }

    public static String getTenantCode(){
        return authLocal.get().getTenantCode();
    }

    public static String getBizCode(){
        return authLocal.get().getBizCode();
    }
}
