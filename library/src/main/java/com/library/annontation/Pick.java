package com.library.annontation;

import com.library.constant.Attrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Pick {
    String value() default "";

    String attr() default Attrs.TEXT;
}
