package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.repository.PasswordRepository;
import com.example.passwordgenerator.util.RequestCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PasswordServiceTest {

    @Mock
    private PasswordRepository passwordRepository;

    @Mock
    private PasswordCache passwordCache;

    @Mock
    private RequestCounter requestCounter;

    @InjectMocks
    private PasswordService passwordService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(passwordRepository, passwordCache, requestCounter);
    }

    @Test
    public void testGeneratePasswordsBulk() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );

        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> passwords = passwordService.generatePasswordsBulk(requests);

        assertEquals(2, passwords.size());
        assertEquals(8, passwords.get(0).length());
        assertEquals(10, passwords.get(1).length());
    }

    @Test
    public void testGeneratePasswordsBulkInvalidLength() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(3, 2, "user1") // Недопустимая длина
        );

        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePasswordsBulk(requests));
    }

    @Test
    public void testGeneratePasswordsBulkInvalidComplexity() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 4, "user1") // Недопустимая сложность
        );

        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePasswordsBulk(requests));
    }
}