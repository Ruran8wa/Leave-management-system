package com.lms.leave.repository;

import com.lms.leave.entity.Leave;
import com.lms.leave.entity.LeaveStatus;
import com.lms.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    
    List<Leave> findByUserId(Long userId);
    
    List<Leave> findByStatus(LeaveStatus status);
    
    List<Leave> findByUserIdAndStatus(Long userId, LeaveStatus status);
    
    List<Leave> findByLeaveType(LeaveType leaveType);
    
    List<Leave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}
