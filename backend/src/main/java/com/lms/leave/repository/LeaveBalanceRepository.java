package com.lms.leave.repository;

import com.lms.leave.entity.LeaveBalance;
import com.lms.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    
    Optional<LeaveBalance> findByUserIdAndLeaveTypeAndYear(Long userId, LeaveType leaveType, Integer year);
    
    List<LeaveBalance> findByUserId(Long userId);
    
    List<LeaveBalance> findByUserIdAndYear(Long userId, Integer year);
}
