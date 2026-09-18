package com.example.dto;

import com.example.entity.IndexStatus;

import java.util.UUID;


public record IndexStatusResponse(
        UUID repositoryId,
        IndexStatus indexStatus
) {
}
