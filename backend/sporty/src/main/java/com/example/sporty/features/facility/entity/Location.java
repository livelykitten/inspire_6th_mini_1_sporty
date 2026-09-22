package com.example.sporty.features.facility.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "location")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String region;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String facilityName;

    @Column(length = 20)
    private String contact;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 8)
    private BigDecimal longitude;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "TIMESTAMP"
    )
    private Instant ceratedAt;

    @UpdateTimestamp 
    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "TIMESTAMP"
    )
    private Instant updateAt;

    @Builder 
    public Location(String region, String name, String facilityName, String contact, BigDecimal latitude, BigDecimal longitude) {
        this.region = region;
        this.name = name;
        this.facilityName = facilityName;
        this.contact = contact;
        this.latitude = latitude;
        this.longitude = longitude;

    }
}
