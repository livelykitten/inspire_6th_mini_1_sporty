package com.example.sporty.features.facilities.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "location")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20) private String region;
    @Column(length = 100) private String name;
    @Column(name = "facility_name", length = 100) private String facilityName;
    @Column(length = 20) private String contact;
    @Column(precision = 10, scale = 8) private BigDecimal latitude;
    // Preserve the supplied database column name; longitude needs three integer digits.
    @Column(name = "longtitude", precision = 11, scale = 8) private BigDecimal longitude;
    @CreationTimestamp @Column(name = "created_at") private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private Instant updatedAt;
}
