package com.example.passwordgenerator.service;

public class RequestCounter {
    private static int count = 0;

    public static synchronized void increment() {
        count++;
    }

    public static synchronized int getCount() {
        return count;
    }

    public static synchronized void reset() {
        count = 0;
    }
}