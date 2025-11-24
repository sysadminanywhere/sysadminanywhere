package com.sysadminanywhere.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sysadminanywhere.entity.LoggingEntity;
import com.sysadminanywhere.repository.LoggingRepository;
import com.sysadminanywhere.security.AuthenticatedUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LoggingAspect {

    private final LoggingRepository loggingRepository;

    public LoggingAspect(LoggingRepository loggingRepository) {
        this.loggingRepository = loggingRepository;
    }

    @Pointcut("execution(* com.sysadminanywhere.service.UsersService.*(..))")
    public void usersServiceMethods() {
    }

    @Pointcut("execution(* com.sysadminanywhere.service.ComputersService.*(..))")
    public void computersServiceMethods() {
    }

    @Pointcut("execution(* com.sysadminanywhere.service.GroupsService.*(..))")
    public void groupsServiceMethods() {
    }

    @Around("usersServiceMethods() || computersServiceMethods() || groupsServiceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        if (methodName.toLowerCase().equalsIgnoreCase("add")
                || methodName.toLowerCase().equalsIgnoreCase("update")
                || methodName.toLowerCase().equalsIgnoreCase("delete")) {

            LoggingEntity loggingEntity = new LoggingEntity();
            loggingEntity.setLogDate(LocalDateTime.now());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof OidcUser user) {
                String userName = user.getClaim("preferred_username");
                loggingEntity.setUserName(userName);
            } else {
                loggingEntity.setUserName("anonymous");
            }

            loggingEntity.setAction(methodName.toLowerCase());
            loggingEntity.setSubject(className.replace("sService", "").toLowerCase());

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String json = mapper.writeValueAsString(args);
            loggingEntity.setParameters(json);

            loggingRepository.save(loggingEntity);
        }

        Object result = joinPoint.proceed();
        return result;
    }

}