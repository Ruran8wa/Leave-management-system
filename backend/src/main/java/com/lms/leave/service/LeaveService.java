package com.lms.leave.service;

import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.common.service.NotificationService;
import com.lms.leave.dto.LeaveDto;
import com.lms.leave.entity.Leave;
import com.lms.leave.entity.LeaveStatus;
import com.lms.leave.entity.PublicHoliday;
import com.lms.leave.repository.LeaveRepository;
import com.lms.leave.repository.PublicHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced Leave Service - Full business logic for leave management
 */
@Service
@RequiredArgsConstructor
public class LeaveService {
    
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final NotificationService notificationService;
    
    /**
     * Calculate working days between two dates (excluding weekends and public holidays)
     */
    public Integer calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate currentDate = startDate;
        
        // Get public holidays in the range
        List<PublicHoliday> holidays = publicHolidayRepository
                .findByDateBetween(startDate, endDate);
        List<LocalDate> holidayDates = holidays.stream()
                .map(PublicHoliday::getDate)
                .collect(Collectors.toList());
        
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends and public holidays
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                currentDate.getDayOfWeek() != DayOfWeek.SUNDAY &&
                !holidayDates.contains(currentDate)) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return workingDays;
    }
    
    /**
     * Employee: Create leave request
     */
    @Transactional
    public LeaveDto.LeaveResponse createLeaveRequest(LeaveDto.CreateLeaveRequest request) {
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }
        
        // Calculate working days
        Integer workingDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());
        
        if (workingDays == 0) {
            throw new RuntimeException("Leave request must include at least one working day");
        }
        
        // Check balance availability for PTO
        if (!leaveBalanceService.hasAvailableBalance(
                request.getUserId(), 
                request.getLeaveType(), 
                request.getStartDate().getYear(), 
                workingDays)) {
            throw new RuntimeException("Insufficient leave balance");
        }
        
        // Create leave
        Leave leave = new Leave();
        leave.setUserId(request.getUserId());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        leave.setDocumentPath(request.getDocumentPath());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setNumberOfDays(workingDays);
        
        Leave savedLeave = leaveRepository.save(leave);
        
        // Add to pending balance
        leaveBalanceService.addPending(
                request.getUserId(), 
                request.getLeaveType(), 
                request.getStartDate().getYear(), 
                workingDays
        );
        
        // Notify manager
        User employee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (employee.getManagerId() != null) {
            User manager = userRepository.findById(employee.getManagerId())
                    .orElse(null);
            if (manager != null) {
                notificationService.sendLeaveRequestNotification(manager, employee, savedLeave);
            }
        }
        
        return mapToResponse(savedLeave, employee, null, "Leave request submitted successfully");
    }
    
    /**
     * Manager/Admin: Approve or reject leave
     */
    @Transactional
    public LeaveDto.LeaveResponse approveOrRejectLeave(
            Long leaveId, 
            Long approverId, 
            LeaveDto.ApprovalRequest request) {
        
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with id: " + leaveId));
        
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending leave requests can be approved/rejected");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));
        
        User employee = userRepository.findById(leave.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Update leave
        leave.setStatus(request.getStatus());
        leave.setApprovedBy(approverId);
        leave.setApprovalComments(request.getApprovalComments());
        
        if (request.getStatus() == LeaveStatus.APPROVED) {
            // Deduct from balance
            leaveBalanceService.deductLeave(
                    leave.getUserId(),
                    leave.getLeaveType(),
                    leave.getStartDate().getYear(),
                    leave.getNumberOfDays()
            );
            
            notificationService.sendLeaveApprovedNotification(
                    employee, 
                    leave, 
                    approver.getFirstName() + " " + approver.getLastName()
            );
        } else if (request.getStatus() == LeaveStatus.REJECTED) {
            leave.setRejectionReason(request.getRejectionReason());
            
            // Remove from pending
            leaveBalanceService.removePending(
                    leave.getUserId(),
                    leave.getLeaveType(),
                    leave.getStartDate().getYear(),
                    leave.getNumberOfDays()
            );
            
            notificationService.sendLeaveRejectedNotification(
                    employee, 
                    leave, 
                    approver.getFirstName() + " " + approver.getLastName()
            );
        }
        
        Leave updatedLeave = leaveRepository.save(leave);
        
        return mapToResponse(
                updatedLeave, 
                employee, 
                approver, 
                "Leave request " + request.getStatus().name().toLowerCase()
        );
    }
    
    /**
     * Get leave balance for employee
     */
    public List<LeaveDto.LeaveBalanceResponse> getLeaveBalance(Long userId, Integer year) {
        var balances = leaveBalanceService.getUserBalances(userId, year);
        
        return balances.stream().map(balance -> {
            LeaveDto.LeaveBalanceResponse response = new LeaveDto.LeaveBalanceResponse();
            response.setLeaveType(balance.getLeaveType());
            response.setLeaveTypeName(balance.getLeaveType().getDisplayName());
            response.setTotalAllocated(balance.getTotalAllocated());
            response.setUsed(balance.getUsed());
            response.setPending(balance.getPending());
            response.setCarriedOver(balance.getCarriedOver());
            response.setRemaining(balance.getRemaining());
            response.setAvailable(balance.getAvailable());
            response.setCarryOverExpiryDate(balance.getCarryOverExpiryDate());
            response.setAccruedToDate(leaveBalanceService.getCurrentAccruedDays());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get colleagues currently on leave (today)
     */
    public List<LeaveDto.ColleagueOnLeaveResponse> getColleaguesOnLeave() {
        LocalDate today = LocalDate.now();
        
        List<Leave> leavesToday = leaveRepository.findAll().stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .filter(leave -> !leave.getStartDate().isAfter(today) && !leave.getEndDate().isBefore(today))
                .collect(Collectors.toList());
        
        return leavesToday.stream().map(leave -> {
            User user = userRepository.findById(leave.getUserId()).orElse(null);
            
            LeaveDto.ColleagueOnLeaveResponse response = new LeaveDto.ColleagueOnLeaveResponse();
            response.setUserId(leave.getUserId());
            response.setEmployeeName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown");
            response.setStartDate(leave.getStartDate());
            response.setEndDate(leave.getEndDate());
            response.setLeaveType(leave.getLeaveType());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get upcoming public holidays
     */
    public List<PublicHoliday> getUpcomingPublicHolidays() {
        return publicHolidayRepository.findByDateAfter(LocalDate.now());
    }
    
    public List<Leave> getAllLeaves() {
        return leaveRepository.findAll();
    }
    
    public Leave getLeaveById(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found with id: " + id));
    }
    
    public List<Leave> getLeavesByUserId(Long userId) {
        return leaveRepository.findByUserId(userId);
    }
    
    public List<Leave> getLeavesByStatus(LeaveStatus status) {
        return leaveRepository.findByStatus(status);
    }
    
    /**
     * Admin: Get pending leave requests from STAFF role users only
     */
    public List<Leave> getPendingStaffLeaves() {
        List<Leave> pendingLeaves = leaveRepository.findByStatus(LeaveStatus.PENDING);
        
        // Filter to only include leaves from STAFF users
        return pendingLeaves.stream()
                .filter(leave -> {
                    User user = userRepository.findById(leave.getUserId()).orElse(null);
                    return user != null && user.getRole() == com.lms.auth.entity.Role.STAFF;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void cancelLeave(Long leaveId, Long userId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        
        if (!leave.getUserId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own leave requests");
        }
        
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending leave requests can be cancelled");
        }
        
        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepository.save(leave);
        
        // Remove from pending balance
        leaveBalanceService.removePending(
                leave.getUserId(),
                leave.getLeaveType(),
                leave.getStartDate().getYear(),
                leave.getNumberOfDays()
        );
    }
    
    /**
     * Helper: Map entity to response DTO
     */
    private LeaveDto.LeaveResponse mapToResponse(Leave leave, User employee, User approver, String message) {
        LeaveDto.LeaveResponse response = new LeaveDto.LeaveResponse();
        response.setId(leave.getId());
        response.setUserId(leave.getUserId());
        response.setEmployeeName(employee != null ? 
                employee.getFirstName() + " " + employee.getLastName() : "Unknown");
        response.setStartDate(leave.getStartDate());
        response.setEndDate(leave.getEndDate());
        response.setLeaveType(leave.getLeaveType());
        response.setReason(leave.getReason());
        response.setStatus(leave.getStatus());
        response.setApprovedBy(leave.getApprovedBy());
        response.setApproverName(approver != null ? 
                approver.getFirstName() + " " + approver.getLastName() : null);
        response.setApprovalComments(leave.getApprovalComments());
        response.setRejectionReason(leave.getRejectionReason());
        response.setNumberOfDays(leave.getNumberOfDays());
        response.setDocumentPath(leave.getDocumentPath());
        response.setMessage(message);
        return response;
    }
}
