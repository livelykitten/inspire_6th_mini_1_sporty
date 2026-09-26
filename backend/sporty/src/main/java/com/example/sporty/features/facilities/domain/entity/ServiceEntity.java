package com.example.sporty.features.facilities.domain.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "service")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_location"))
    private LocationEntity location;
    @Column(length = 255) private String name;
    @Column(length = 255) private String status;
    @Column(name = "start_time") private LocalDateTime startTime;
    @Column(name = "end_time") private LocalDateTime endTime;
    @Column(name = "is_free", columnDefinition = "CHAR(1)") private Character isFree;
    @Column(length = 200) private String url;
    @Column(name = "cancel_deadline_at") private LocalDate cancelDeadlineAt;
    @CreationTimestamp @Column(name = "created_at") private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private Instant updatedAt;
}
