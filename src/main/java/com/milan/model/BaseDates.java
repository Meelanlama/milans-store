package com.milan.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass //helps find out JPA to link up these entities
@EntityListeners(AuditingEntityListener.class) // Auto-populate timestamps
public abstract class BaseDates {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedOn;

}
