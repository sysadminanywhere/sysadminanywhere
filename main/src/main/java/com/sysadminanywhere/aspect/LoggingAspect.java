package com.sysadminanywhere.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.sysadminanywhere.service.UsersService.*(..))")
    public void usersServiceMethods() {}

    @Pointcut("execution(* com.sysadminanywhere.service.ComputersService.*(..))")
    public void computersServiceMethods() {}

    @Pointcut("execution(* com.sysadminanywhere.service.GroupsService.*(..))")
    public void groupsServiceMethods() {}

    @Around("usersServiceMethods() || computersServiceMethods() || groupsServiceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        System.out.println("Method " + methodName + " in " + className);

        Object result = joinPoint.proceed();
        return result;
    }

}