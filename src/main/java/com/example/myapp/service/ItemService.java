package com.example.myapp.service;

import com.example.myapp.domain.entity.Item;
import com.example.myapp.domain.exception.ConflictException;
import com.example.myapp.domain.exception.ResourceNotFoundException;
import com.example.myapp.domain.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public Page<Item> findAll(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public Item findById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    @Transactional
    public Item create(String title, String description) {
        if (itemRepository.existsByTitle(title)) {
            throw new ConflictException("Item with title '" + title + "' already exists");
        }
        var item = Item.builder()
                .title(title)
                .description(description)
                .build();
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(UUID id, String title, String description) {
        var item = findById(id);

        if (title != null) {
            itemRepository.findByTitle(title)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ConflictException("Item with title '" + title + "' already exists");
                    });
            item.setTitle(title);
        }
        if (description != null) {
            item.setDescription(description);
        }

        return itemRepository.save(item);
    }

    @Transactional
    public void delete(UUID id) {
        var item = findById(id);
        itemRepository.delete(item);
    }
}
