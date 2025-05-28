package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.repository.PasswordRepository;
import com.example.passwordgenerator.util.PasswordGenerator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final PasswordCache passwordCache;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService(PasswordRepository passwordRepository, PasswordCache passwordCache) {
        this.passwordRepository = passwordRepository;
        this.passwordCache = passwordCache;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String generatePassword(int length, int complexity) {
        Optional<String> cachedPassword = passwordCache.getGeneratedPassword(length, complexity);
        if (cachedPassword.isPresent()) {
            return cachedPassword.get();
        }
        RequestCounter.increment();
        String generatedPassword = PasswordGenerator.generatePassword(length, complexity);
        passwordCache.putGeneratedPassword(length, complexity, generatedPassword);
        return generatedPassword;
    }

    public List<String> generatePasswordsBulk(List<PasswordGenerationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        String cacheKey = requests.stream()
                .filter(Objects::nonNull)
                .map(req -> req.getLength() + "_" + req.getComplexity() + "_" + req.getOwner())
                .collect(Collectors.joining("|"));
        Optional<List<String>> cachedPasswords = passwordCache.getBulkPasswords(cacheKey);
        if (cachedPasswords.isPresent()) {
            return cachedPasswords.get();
        }
        List<String> generatedPasswords = IntStream.range(0, requests.size())
                .mapToObj(i -> {
                    PasswordGenerationRequest request = requests.get(i);
                    return (request != null) ? generatePassword(request.getLength(), request.getComplexity()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        passwordCache.putBulkPasswords(cacheKey, generatedPasswords);
        List<Password> passwordsToSave = IntStream.range(0, requests.size())
                .filter(i -> requests.get(i) != null && generatedPasswords.contains(generatePassword(requests.get(i).getLength(), requests.get(i).getComplexity())))
                .mapToObj(i -> new Password(generatedPasswords.get(generatedPasswords.indexOf(generatePassword(requests.get(i).getLength(), requests.get(i).getComplexity()))), requests.get(i).getOwner()))
                .collect(Collectors.toList());
        createBulk(passwordsToSave);
        return generatedPasswords;
    }

    public Password create(Password password) {
        String plainPassword = password.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        password.setPassword(hashedPassword);
        Password saved = passwordRepository.save(password);
        return saved;
    }

    public List<Password> createBulk(List<Password> passwords) {
        List<Password> savedPasswords = passwords.stream()
                .map(password -> {
                    String plainPassword = password.getPassword();
                    String hashedPassword = passwordEncoder.encode(plainPassword);
                    password.setPassword(hashedPassword);
                    return passwordRepository.save(password);
                })
                .collect(Collectors.toList());
        return savedPasswords;
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
        return saved;
    }

    public void delete(Long id) {
        passwordRepository.deleteById(id);
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