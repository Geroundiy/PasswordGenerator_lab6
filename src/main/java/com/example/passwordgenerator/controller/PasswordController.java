package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.service.PasswordService;
import com.example.passwordgenerator.util.RequestCounter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private final PasswordService passwordService;
    private final RequestCounter requestCounter;

    public PasswordController(PasswordService passwordService, RequestCounter requestCounter) {
        this.passwordService = passwordService;
        this.requestCounter = requestCounter;
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generatePassword(
            @RequestParam int length,
            @RequestParam int complexity,
            @RequestParam String owner) {

        if (length < 4 || length > 30) {
            throw new IllegalArgumentException("Длина пароля должна быть от 4 до 30 символов.");
        }
        if (complexity < 1 || complexity > 3) {
            throw new IllegalArgumentException("Уровень сложности должен быть от 1 до 3.");
        }

        String password = passwordService.generatePassword(length, complexity);
        Password passwordEntity = new Password(password, owner);
        passwordService.create(passwordEntity);

        return ResponseEntity.ok("✅ Пароль для " + owner + ": " + password);
    }

    @PostMapping("/generate-bulk")
    public ResponseEntity<List<String>> generatePasswordsBulk(@RequestBody List<PasswordGenerationRequest> requests) {
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        return ResponseEntity.ok(passwords);
    }

    @GetMapping
    public List<Password> getAll() {
        return passwordService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Password> getById(@PathVariable Long id) {
        return passwordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Password create(@RequestBody Password password) {
        return passwordService.create(password);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Password> update(@PathVariable Long id, @RequestBody Password password) {
        password.setId(id);
        Password updated = passwordService.update(password);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passwordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-tag")
    public List<Password> getPasswordsByTagName(@RequestParam String tagName) {
        return passwordService.findPasswordsByTagName(tagName);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getRequestCount() {
        return ResponseEntity.ok(requestCounter.getCount());
    }
}