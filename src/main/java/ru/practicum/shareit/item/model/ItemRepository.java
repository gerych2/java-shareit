package ru.practicum.shareit.item.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);
    
    List<Item> findByRequestId(Long requestId);
    
    List<Item> findByRequestIdIn(List<Long> requestIds);
}
