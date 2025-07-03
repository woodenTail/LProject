package com.pro.miniSpring.security;

import com.pro.miniSpring.init.ApplicationContext;
import com.pro.miniSpring.init.Auth;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class PermissionInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Auth annotation = method.getAnnotation(Auth.class);
        if (annotation == null) {
            return methodProxy.invokeSuper(o, objects);
        }
        String permission = annotation.value();
        PermissionService permissionService = ApplicationContext.getBean(PermissionService.class);
        if (permissionService!=null && !permissionService.hasPermission(permission)) {
            throw new SecurityException("user does not exist Permission" + permission );
        }
        return methodProxy.invokeSuper(o, objects);
    }
}
