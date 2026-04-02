package com.example.myapp.web.controller;

import com.example.myapp.service.ItemService;
import com.example.myapp.web.dto.CreateItemRequest;
import com.example.myapp.web.dto.ItemResponse;
import com.example.myapp.web.dto.UpdateItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item CRUD operations")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "List all items (paginated)")
    public Page<ItemResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return itemService.findAll(pageable).map(ItemResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ItemResponse get(@PathVariable UUID id) {
        return ItemResponse.from(itemService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new item")
    public ItemResponse create(@Valid @RequestBody CreateItemRequest request) {
        var item = itemService.create(request.title(), request.description());
        return ItemResponse.from(item);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing item")
    public ItemResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request) {
        var item = itemService.update(id, request.title(), request.description());
        return ItemResponse.from(item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an item")
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }
}
