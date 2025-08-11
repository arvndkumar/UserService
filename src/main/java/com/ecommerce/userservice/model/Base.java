package com.ecommerce.userservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    @Column(nullable = false)
    private boolean deleted = Boolean.FALSE;

    @PrePersist
    protected void onCreate() {
        if(createdAt == null) createdAt = Date.from(Instant.now());
        if(updatedAt == null) updatedAt = Date.from(Instant.now());
    }

    @PreUpdate
    protected void onUpdate() {
        if(updatedAt == null) updatedAt = Date.from(Instant.now());
    }


}
