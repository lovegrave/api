package cn.yanss.m.cookhouse.api.anntate.impl;

import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hl
 */
@Component
@Scope
@Aspect
@Slf4j
public class LimitAspect {
    /**
     * 每秒发出20个令牌，此处是单进程服务的限流,内部采用令牌捅算法实现
     */
    private static final RateLimiter rateLimiter = RateLimiter.create(20.0);

    /**
     * Service层切点  限流
     */
    @Pointcut("@annotation(cn.yanss.m.cookhouse.api.anntate.ServiceLimit)")
    public void ServiceAspect() {

    }

    @Around("ServiceAspect()")
    public Object around(ProceedingJoinPoint joinPoint) {
        Boolean flag = rateLimiter.tryAcquire();
        try {
            if (flag) {
                return joinPoint.proceed();
            }
        } catch (Throwable e) {
            LogUtils.create().methodName("LimitAspect").message(e.getMessage()).error();
        }
        LogUtils.create().methodName("LimitAspect").message("流量控制").info();
        return new ReturnEntity(500, "流量控制");
    }
}
