package com.example.dto;

import java.util.UUID;

public record UserRecord(
    UUID id,
    Long githubId,
    String uname,
    String displayName,
    String avatarUrl
) {}
