package com.sysadminanywhere.inventory.wmi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WMIAttribute {
    String name() default "";
    boolean IsReadOnly() default false;
}
