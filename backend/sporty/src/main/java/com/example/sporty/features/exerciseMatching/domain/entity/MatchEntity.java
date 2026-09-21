package com.example.sporty.features.exerciseMatching.domain.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "`match`")
@Builder 
@Getter 
@ToString 
@AllArgsConstructor 
@NoArgsConstructor 
public class MatchEntity {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", length = 50)
    private String title;
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "start_at", columnDefinition = "DATETIME")
    private LocalDateTime startAt;
    @Column(name = "end_at", columnDefinition = "DATETIME")
    private LocalDateTime endAt;

    @Column(name = "max_participant")
    private Integer maxParticipant;

    @Builder.Default
    @Enumerated (EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", length = 10, nullable = false)
    @ColumnDefault ("'RECRUITING'")
    private MatchStatus status = MatchStatus.RECRUITING;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "skill_level", length = 30, nullable = false)
    @ColumnDefault("'BEGINNER'")
    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    // TODO: ServiceEntity가 만들어지면, private ServiceEntity로 교체
    @Column(name = "service_id")
    private Integer serviceId;
}
