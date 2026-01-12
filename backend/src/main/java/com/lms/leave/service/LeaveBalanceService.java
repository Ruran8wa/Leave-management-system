package com.lms.leave.service;

import com.lms.leave.entity.LeaveBalance;
import com.lms.leave.entity.LeaveType;
import com.lms.leave.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Leave Balance Service - Manages leave allocations, accruals, and carryover
 * 
 * Accrual Rules:
 * - Personal Time Off: 1.66 days per month (20 days per year)
 * - Carryover: Max 5 days to next year
 * - Carryover expires: January 31st of next year
 */
@Service
@RequiredArgsConstructor
public class LeaveBalanceService {
    
    private final LeaveBalanceRepository leaveBalanceRepository;
    
    // Constants
    private static final BigDecimal MONTHLY_ACCRUAL = new BigDecimal("1.66");
    private static final int ANNUAL_PTO_DAYS = 20;
    private static final int MAX_CARRYOVER = 5;
    
    /**
     * Initialize leave balance for a new user
     */
    @Transactional
    public LeaveBalance initializeBalance(Long userId, LeaveType leaveType, Integer year) {
        // Check if balance already exists
        var existing = leaveBalanceRepository
                .findByUserIdAndLeaveTypeAndYear(userId, leaveType, year);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        LeaveBalance balance = new LeaveBalance();
        balance.setUserId(userId);
        balance.setLeaveType(leaveType);
        balance.setYear(year);
        
        // Set allocation based on leave type
        if (leaveType == LeaveType.PERSONAL_TIME_OFF) {
            balance.setTotalAllocated(ANNUAL_PTO_DAYS);
        } else {
            balance.setTotalAllocated(0);  // Unlimited types don't have allocation
        }
        
        balance.setUsed(0);
        balance.setPending(0);
        balance.setCarriedOver(0);
        
        return leaveBalanceRepository.save(balance);
    }
    
    /**
     * Calculate accrued days for PTO up to current month
     * 1.66 days per month * number of months passed
     */
    public Integer calculateAccruedDays(int monthsPassed) {
        BigDecimal accrued = MONTHLY_ACCRUAL.multiply(new BigDecimal(monthsPassed));
        return accrued.setScale(0, RoundingMode.DOWN).intValue();
    }
    
    /**
     * Get current accrued days for the year (up to current month)
     */
    public Integer getCurrentAccruedDays() {
        int currentMonth = LocalDate.now().getMonthValue();
        return calculateAccruedDays(currentMonth);
    }
    
    /**
     * Get or create leave balance for user
     */
    @Transactional
    public LeaveBalance getOrCreateBalance(Long userId, LeaveType leaveType, Integer year) {
        return leaveBalanceRepository
                .findByUserIdAndLeaveTypeAndYear(userId, leaveType, year)
                .orElseGet(() -> initializeBalance(userId, leaveType, year));
    }
    
    /**
     * Get all balances for a user in a specific year
     */
    public List<LeaveBalance> getUserBalances(Long userId, Integer year) {
        return leaveBalanceRepository.findByUserIdAndYear(userId, year);
    }
    
    /**
     * Deduct days when leave is approved
     */
    @Transactional
    public void deductLeave(Long userId, LeaveType leaveType, Integer year, Integer days) {
        LeaveBalance balance = getOrCreateBalance(userId, leaveType, year);
        
        // First use carryover days if available and not expired
        if (balance.getCarriedOver() > 0 && 
            balance.getCarryOverExpiryDate() != null &&
            LocalDate.now().isBefore(balance.getCarryOverExpiryDate())) {
            
            int fromCarryover = Math.min(days, balance.getCarriedOver());
            balance.setCarriedOver(balance.getCarriedOver() - fromCarryover);
            days -= fromCarryover;
        }
        
        // Then use current year allocation
        balance.setUsed(balance.getUsed() + days);
        balance.setPending(balance.getPending() - days);
        
        leaveBalanceRepository.save(balance);
    }
    
    /**
     * Add days to pending when leave is requested
     */
    @Transactional
    public void addPending(Long userId, LeaveType leaveType, Integer year, Integer days) {
        LeaveBalance balance = getOrCreateBalance(userId, leaveType, year);
        balance.setPending(balance.getPending() + days);
        leaveBalanceRepository.save(balance);
    }
    
    /**
     * Remove days from pending when leave is rejected or cancelled
     */
    @Transactional
    public void removePending(Long userId, LeaveType leaveType, Integer year, Integer days) {
        LeaveBalance balance = getOrCreateBalance(userId, leaveType, year);
        balance.setPending(Math.max(0, balance.getPending() - days));
        leaveBalanceRepository.save(balance);
    }
    
    /**
     * Process year-end carryover
     * Max 5 days can be carried over, expires Jan 31 of next year
     */
    @Transactional
    public void processYearEndCarryover(Long userId, Integer fromYear) {
        var balances = leaveBalanceRepository.findByUserIdAndYear(userId, fromYear);
        
        for (LeaveBalance oldBalance : balances) {
            if (oldBalance.getLeaveType() == LeaveType.PERSONAL_TIME_OFF) {
                int remaining = oldBalance.getRemaining();
                int carryover = Math.min(remaining, MAX_CARRYOVER);
                
                if (carryover > 0) {
                    // Create or update next year's balance
                    LeaveBalance newBalance = getOrCreateBalance(
                            userId, 
                            LeaveType.PERSONAL_TIME_OFF, 
                            fromYear + 1
                    );
                    
                    newBalance.setCarriedOver(carryover);
                    newBalance.setCarryOverExpiryDate(
                            LocalDate.of(fromYear + 1, 1, 31)
                    );
                    
                    leaveBalanceRepository.save(newBalance);
                }
            }
        }
    }
    
    /**
     * Admin function: Manually adjust leave balance
     */
    @Transactional
    public LeaveBalance adjustBalance(Long userId, LeaveType leaveType, Integer year, 
                                       Integer adjustment, String reason) {
        LeaveBalance balance = getOrCreateBalance(userId, leaveType, year);
        balance.setTotalAllocated(balance.getTotalAllocated() + adjustment);
        return leaveBalanceRepository.save(balance);
    }
    
    /**
     * Check if user has sufficient balance for leave request
     */
    public boolean hasAvailableBalance(Long userId, LeaveType leaveType, Integer year, Integer days) {
        if (leaveType != LeaveType.PERSONAL_TIME_OFF) {
            return true;  // Unlimited leave types always allowed
        }
        
        LeaveBalance balance = getOrCreateBalance(userId, leaveType, year);
        return balance.canRequest(days);
    }
}
