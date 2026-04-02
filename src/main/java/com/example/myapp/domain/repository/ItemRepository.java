package com.example.myapp.domain.repository;

import com.example.myapp.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByTitle(String title);

    boolean existsByTitle(String title);
}
