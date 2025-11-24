package com.sysadminanywhere.aspect;

import com.sysadminanywhere.entity.LoggingEntity;
import com.sysadminanywhere.repository.LoggingRepository;
import com.sysadminanywhere.security.AuthenticatedUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

        boolean isSave = true;

        LoggingEntity loggingEntity = new LoggingEntity();
        loggingEntity.setLogDate(LocalDateTime.now());

        String action = "User";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser user) {
            String userName = user.getClaim("preferred_username");
            loggingEntity.setUserName(userName);
            action += " '" + userName + "'";
        } else {
            loggingEntity.setUserName("anonymous");
            action += " 'anonymous'";
        }

        String subject = className.replace("sService", "").toLowerCase();
        String name = "";

        switch (methodName.toLowerCase()) {
            case "add":
                action += " added " + subject + " '" + name + "'";
                break;
            case "update":
                action += " updated " + subject + " '" + name + "'";
                break;
            case "delete":
                action += " deleted " + subject + " '" + name + "'";
                break;

            default:
                isSave = false;
        }

        loggingEntity.setAction(action);

        if (isSave)
            loggingRepository.save(loggingEntity);

        Object result = joinPoint.proceed();
        return result;
    }

}