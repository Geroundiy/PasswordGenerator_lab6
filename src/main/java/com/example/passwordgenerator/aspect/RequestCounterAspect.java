package com.example.passwordgenerator.aspect;

import com.example.passwordgenerator.service.RequestCounter;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequestCounterAspect {

    @Before("execution(* com.example.passwordgenerator.service.*.*(..)) && !within(com.example.passwordgenerator.service.PasswordService)")
    public void incrementCounter() {
        System.out.println("Аспект сработал: увеличиваю счётчик");
        RequestCounter.increment();
    }

}