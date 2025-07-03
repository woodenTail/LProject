package com.pro.miniSpring.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface Auth {
    /**
     * @return the Spring-EL expression to be evaluated before invoking the protected
     * method
     */
    String value() default "";
}
