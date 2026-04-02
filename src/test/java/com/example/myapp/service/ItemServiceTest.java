package com.example.myapp.service;

import com.example.myapp.domain.entity.Item;
import com.example.myapp.domain.exception.ConflictException;
import com.example.myapp.domain.exception.ResourceNotFoundException;
import com.example.myapp.domain.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void create_shouldSaveAndReturnItem() {
        var item = Item.builder().id(UUID.randomUUID()).title("Test").description("Desc").build();
        given(itemRepository.existsByTitle("Test")).willReturn(false);
        given(itemRepository.save(any(Item.class))).willReturn(item);

        var result = itemService.create("Test", "Desc");

        assertThat(result.getTitle()).isEqualTo("Test");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_shouldThrowConflict_whenTitleExists() {
        given(itemRepository.existsByTitle("Dup")).willReturn(true);

        assertThatThrownBy(() -> itemService.create("Dup", "Desc"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findById_shouldReturnItem() {
        var id = UUID.randomUUID();
        var item = Item.builder().id(id).title("Test").build();
        given(itemRepository.findById(id)).willReturn(Optional.of(item));

        var result = itemService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowNotFound() {
        var id = UUID.randomUUID();
        given(itemRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPage() {
        var items = List.of(Item.builder().title("A").build(), Item.builder().title("B").build());
        given(itemRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(items));

        var result = itemService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void delete_shouldDeleteExistingItem() {
        var id = UUID.randomUUID();
        var item = Item.builder().id(id).title("Test").build();
        given(itemRepository.findById(id)).willReturn(Optional.of(item));

        itemService.delete(id);

        verify(itemRepository).delete(item);
    }
}
