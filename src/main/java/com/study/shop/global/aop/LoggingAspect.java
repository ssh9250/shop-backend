package com.study.shop.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.study.shop..controller..*(..)")
    public void controllerPointcut() {
    }

    @Before("controllerPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("[START] {}", methodName);
    }

    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("[END] {} return={}", methodName, result);
    }

    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("[EXCEPTION] {} message = {}", methodName, ex.getMessage());
    }

}
