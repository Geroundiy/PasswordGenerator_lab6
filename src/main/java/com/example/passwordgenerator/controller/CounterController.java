package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.service.RequestCounter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counter")
public class CounterController {

    @GetMapping("/get")
    public ResponseEntity<Integer> getCounter() {
        return ResponseEntity.ok(RequestCounter.getCount());
    }

    @GetMapping("/reset")
    public ResponseEntity<String> resetCounter() {
        RequestCounter.reset();
        return ResponseEntity.ok("Счётчик сброшен");
    }
}