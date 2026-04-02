package com.example.myapp.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        String description
) {}
