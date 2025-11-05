package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    
    // Получить список запросов конкретного пользователя, отсортированных по дате создания (от новых к старым)
    List<ItemRequest> findByRequesterIdOrderByCreateDateDesc(Long requesterId);
    
    // Получить список запросов, созданных другими пользователями (не requesterId)
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requester.id <> :userId ORDER BY ir.createDate DESC")
    List<ItemRequest> findByOtherUsers(@Param("userId") Long userId);
}

