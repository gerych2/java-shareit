package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Для пользователя
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    // Для владельца
    @Query("select b from Booking b where b.item.ownerId = ?1 order by b.start desc")
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    @Query("select b from Booking b where b.item.ownerId = ?1 and b.start <= ?2 and b.end >= ?2 order by b.start desc")
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b where b.item.ownerId = ?1 and b.end < ?2 order by b.start desc")
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b where b.item.ownerId = ?1 and b.start > ?2 order by b.start desc")
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b where b.item.ownerId = ?1 and b.status = ?2 order by b.start desc")
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable pageable);

    // Для получения последнего и следующего бронирования для вещи
    @Query("select b from Booking b where b.item.id = ?1 and b.status = 'APPROVED' and b.end < ?2 order by b.end desc")
    List<Booking> findPastByItemOwnerIdOrderByStartDesc(Long itemId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b where b.item.id = ?1 and b.status = 'APPROVED' and b.start > ?2 order by b.start asc")
    List<Booking> findFutureByItemOwnerIdOrderByStartDesc(Long itemId, LocalDateTime now, Pageable pageable);

    // Для проверки возможности оставить комментарий
    @Query("select b from Booking b where b.item.id = ?1 and b.booker.id = ?2 and b.status = 'APPROVED' and b.end < ?3")
    List<Booking> findPastApprovedBookingsByItemIdAndBookerId(Long itemId, Long bookerId, LocalDateTime now);
}
