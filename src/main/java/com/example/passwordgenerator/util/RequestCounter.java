package com.example.passwordgenerator.util;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public int getCount() {
        return counter.get();
    }
}