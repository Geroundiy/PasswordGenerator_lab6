package com.example.passwordgenerator.util;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SYMBOLS = "!@#$%^&*()_-+=<>?/{}[]|";

    public static String generatePassword(int length, int complexity) {
        if (length < 4 || length > 30) {
            throw new IllegalArgumentException("Длина пароля должна быть от 4 до 30 символов.");
        }
        if (complexity < 1 || complexity > 3) {
            throw new IllegalArgumentException("Уровень сложности должен быть от 1 до 3.");
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

        int guaranteedChars = 0;
        if (complexity >= 1 && length > guaranteedChars) {
            int index = random.nextInt(NUMBERS.length());
            password.append(NUMBERS.charAt(index));
            guaranteedChars++;
        }
        if (complexity >= 2 && length > guaranteedChars) {
            int index = random.nextInt(LETTERS.length());
            password.append(LETTERS.charAt(index));
            guaranteedChars++;
        }
        if (complexity >= 3 && length > guaranteedChars) {
            int index = random.nextInt(SYMBOLS.length());
            password.append(SYMBOLS.charAt(index));
            guaranteedChars++;
        }

        for (int i = guaranteedChars; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}