package com.pro.miniSpring.init;

import com.pro.miniSpring.security.PermissionInterceptor;
import net.sf.cglib.proxy.Enhancer;

public class ProxyFactory {


    public static <T>T createProxy(Class<T> target){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setCallback(new PermissionInterceptor());
        return (T) enhancer.create();
    }
}
