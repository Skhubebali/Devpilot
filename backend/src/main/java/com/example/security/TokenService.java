package com.example.security;

import com.example.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TextEncryptor textEncryptor;

    // Generate token containing userId and timestamp
    public String generateToken(User user) {
        String payload = user.getId().toString() + ":" + System.currentTimeMillis();
        return textEncryptor.encrypt(payload);
    }

    // Decrypt and extract userId from token
    public UUID extractUserId(String token) {
        try {
            String decrypted = textEncryptor.decrypt(token);
            String[] parts = decrypted.split(":");
            if (parts.length > 0) {
                return UUID.fromString(parts[0]);
            }
        } catch (Exception e) {
            log.warn("Invalid or expired auth token: {}", e.getMessage());
        }
        return null;
    }
}
