package com.lms.leave.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "public_holidays")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PublicHoliday extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private Boolean isRecurring = false;  // e.g., Christmas recurs every year
}
