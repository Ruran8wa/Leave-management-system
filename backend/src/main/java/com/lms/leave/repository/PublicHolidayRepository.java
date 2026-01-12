package com.lms.leave.repository;

import com.lms.leave.entity.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    
    List<PublicHoliday> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<PublicHoliday> findByDateAfter(LocalDate date);
    
    boolean existsByDate(LocalDate date);
}
