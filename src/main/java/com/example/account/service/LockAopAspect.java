package com.example.account.service;

import com.example.account.aop.AccountLockIdInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
    private final LockService lockService;

    //AccountLock 을 어노테이션한 메소드가 수행될때 전후에 락을 취득했다가 해제함.
    @Around("@annotation(com.example.account.aop.AccountLock) && args(request)") //aspect j 문법, 어떤 경우에 aspect를 적용할 것인가를 정의
    public Object aroundMethod(
            ProceedingJoinPoint pjp,
            AccountLockIdInterface request
    ) throws Throwable {
        // lock 취득 시도
        lockService.lock(request.getAccountNumber());
        try{
            return pjp.proceed(); //aop 를 걸어놨던 부분을 그대로 동작시킴
        } finally {
            // lock 해제
            lockService.unlock(request.getAccountNumber());
        }
    }

}
