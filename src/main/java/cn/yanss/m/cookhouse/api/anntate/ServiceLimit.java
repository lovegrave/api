package cn.yanss.m.cookhouse.api.anntate;

import java.lang.annotation.*;

/**
 * @author hl
 * @desc 限流注解
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceLimit {
    String description() default "";
}
