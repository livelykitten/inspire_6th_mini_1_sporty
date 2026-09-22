package com.example.sporty.features.facility.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "service")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SportService {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String status;

    private LocalTime startTime;
    private LocalDateTime endTime;

    @Column(length = 1)
    private String isFree;

    @Column(length = 200)
    private String url;

    private LocalDate cancelDeadlineAt;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "TIMESTAMP"
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "TIMESTAMP"
    )
    private Instant updatedAt;
    
    @Builder
    public SportService(Location location, String name, String status, LocalTime startTime, LocalDateTime endTime, String isFree, String url, LocalDate cancelDeadlineAt) {
        this.location = location;
        this.name = name;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isFree = isFree;
        this.url = url;
        this.cancelDeadlineAt = cancelDeadlineAt;
    }
}

