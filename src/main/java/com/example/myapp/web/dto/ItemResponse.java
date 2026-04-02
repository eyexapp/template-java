package com.example.myapp.web.dto;

import com.example.myapp.domain.entity.Item;

import java.time.LocalDateTime;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
