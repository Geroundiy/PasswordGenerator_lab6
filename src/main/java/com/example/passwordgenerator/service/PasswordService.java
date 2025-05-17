package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.repository.PasswordRepository;
import com.example.passwordgenerator.util.RequestCounter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PasswordService {

    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SYMBOLS = "!@#$%^&*()_-+=<>?/{}[]|";
    private final PasswordRepository passwordRepository;
    private final PasswordCache passwordCache;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RequestCounter requestCounter;

    public PasswordService(PasswordRepository passwordRepository, PasswordCache passwordCache, RequestCounter requestCounter) {
        this.passwordRepository = passwordRepository;
        this.passwordCache = passwordCache;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.requestCounter = requestCounter;
    }

    public String generatePassword(int length, int complexity) {
        requestCounter.increment();
        Optional<String> cachedPassword = passwordCache.getGeneratedPassword(length, complexity);
        if (cachedPassword.isPresent()) {
            return cachedPassword.get();
        }

        String characters = NUMBERS;
        if (complexity >= 2) {
            characters += LETTERS;
        }
        if (complexity >= 3) {
            characters += SYMBOLS;
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        String generatedPassword = password.toString();
        passwordCache.putGeneratedPassword(length, complexity, generatedPassword);
        return generatedPassword;
    }

    public List<String> generatePasswordsBulk(List<PasswordGenerationRequest> requests) {
        return requests.stream()
                .map(request -> {
                    if (request.getLength() < 4 || request.getLength() > 30) {
                        throw new IllegalArgumentException("Длина пароля должна быть от 4 до 30 символов.");
                    }
                    if (request.getComplexity() < 1 || request.getComplexity() > 3) {
                        throw new IllegalArgumentException("Уровень сложности должен быть от 1 до 3.");
                    }
                    String password = generatePassword(request.getLength(), request.getComplexity());
                    Password passwordEntity = new Password(password, request.getOwner());
                    create(passwordEntity);
                    return password;
                })
                .collect(Collectors.toList());
    }

    public Password create(Password password) {
        String plainPassword = password.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        password.setPassword(hashedPassword);
        Password saved = passwordRepository.save(password);
        passwordCache.clearDatabaseCache();
        return saved;
    }

    public List<Password> findAll() {
        Optional<List<Password>> cachedPasswords = passwordCache.getAllPasswords();
        if (cachedPasswords.isPresent()) {
            return cachedPasswords.get();
        }
        List<Password> passwords = passwordRepository.findAll();
        passwordCache.putAllPasswords(passwords);
        return passwords;
    }

    public Optional<Password> findById(Long id) {
        Optional<Password> cachedPassword = passwordCache.getPasswordById(id);
        if (cachedPassword.isPresent()) {
            return cachedPassword;
        }
        Optional<Password> password = passwordRepository.findById(id);
        password.ifPresent(p -> passwordCache.putPasswordById(id, p));
        return password;
    }

    public Password update(Password password) {
        String plainPassword = password.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        password.setPassword(hashedPassword);
        Password saved = passwordRepository.save(password);
        passwordCache.clearDatabaseCache();
        return saved;
    }

    public void delete(Long id) {
        passwordRepository.deleteById(id);
        passwordCache.clearDatabaseCache();
    }

    public List<Password> findPasswordsByTagName(String tagName) {
        Optional<List<Password>> cachedPasswords = passwordCache.getPasswordsByTag(tagName);
        if (cachedPasswords.isPresent()) {
            return cachedPasswords.get();
        }
        List<Password> passwords = passwordRepository.findPasswordsByTagName(tagName);
        passwordCache.putPasswordsByTag(tagName, passwords);
        return passwords;
    }
}