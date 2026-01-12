package com.lms.leave.service;

import com.lms.leave.entity.Leave;
import com.lms.leave.entity.LeaveBalance;
import com.lms.leave.entity.LeaveStatus;
import com.lms.leave.entity.PublicHoliday;
import com.lms.leave.repository.LeaveBalanceRepository;
import com.lms.leave.repository.LeaveRepository;
import com.lms.leave.repository.PublicHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin Service - HR/Admin management functions
 */
@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final PublicHolidayRepository publicHolidayRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRepository leaveRepository;
    
    /**
     * Add a new public holiday
     */
    @Transactional
    public PublicHoliday addPublicHoliday(PublicHoliday holiday) {
        if (publicHolidayRepository.existsByDate(holiday.getDate())) {
            throw new RuntimeException("Public holiday already exists for this date");
        }
        return publicHolidayRepository.save(holiday);
    }
    
    /**
     * Remove a public holiday
     */
    @Transactional
    public void removePublicHoliday(Long holidayId) {
        publicHolidayRepository.deleteById(holidayId);
    }
    
    /**
     * Get all public holidays
     */
    public List<PublicHoliday> getAllPublicHolidays() {
        return publicHolidayRepository.findAll();
    }
    
    /**
     * Manually adjust employee leave balance
     */
    @Transactional
    public LeaveBalance adjustEmployeeBalance(
            Long userId,
            com.lms.leave.entity.LeaveType leaveType,
            Integer year,
            Integer adjustment,
            String reason) {
        return leaveBalanceService.adjustBalance(userId, leaveType, year, adjustment, reason);
    }
    
    /**
     * Get all leave balances for all users
     */
    public List<LeaveBalance> getAllLeaveBalances() {
        return leaveBalanceRepository.findAll();
    }
    
    /**
     * Get user's leave balance for specific year
     */
    public List<LeaveBalance> getUserLeaveBalance(Long userId, Integer year) {
        return leaveBalanceRepository.findByUserIdAndYear(userId, year);
    }
    
    /**
     * Process year-end carryover for all employees
     */
    @Transactional
    public void processYearEndCarryover(Integer year) {
        // Get all unique user IDs from leave balances
        List<Long> userIds = leaveBalanceRepository.findAll().stream()
                .map(LeaveBalance::getUserId)
                .distinct()
                .toList();
        
        for (Long userId : userIds) {
            leaveBalanceService.processYearEndCarryover(userId, year);
        }
    }
    
    /**
     * Generate leave report data for CSV/Excel export
     */
    public List<Leave> generateLeaveReport(LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findByStartDateBetween(startDate, endDate);
    }
    
    /**
     * Generate leave balance report for all employees
     */
    public List<LeaveBalance> generateBalanceReport(Integer year) {
        return leaveBalanceRepository.findAll().stream()
                .filter(balance -> balance.getYear().equals(year))
                .toList();
    }
    
    /**
     * Get leave statistics
     */
    public LeaveStatistics getLeaveStatistics(Integer year) {
        List<Leave> leaves = leaveRepository.findAll().stream()
                .filter(leave -> leave.getStartDate().getYear() == year)
                .toList();
        
        long totalRequests = leaves.size();
        long approved = leaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .count();
        long rejected = leaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.REJECTED)
                .count();
        long pending = leaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.PENDING)
                .count();
        
        int totalDaysRequested = leaves.stream()
                .mapToInt(Leave::getNumberOfDays)
                .sum();
        
        return new LeaveStatistics(
                totalRequests,
                approved,
                rejected,
                pending,
                totalDaysRequested
        );
    }
    
    /**
     * Leave Statistics DTO
     */
    public record LeaveStatistics(
            long totalRequests,
            long approved,
            long rejected,
            long pending,
            int totalDaysRequested
    ) {}
}
