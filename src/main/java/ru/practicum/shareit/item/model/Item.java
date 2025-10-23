package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название вещи не может быть пустым")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым")
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull(message = "Статус доступности вещи не может быть пустым")
    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "request_id")
    private Long requestId; // Для связи с запросами на вещи
}
