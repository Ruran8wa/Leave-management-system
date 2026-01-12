package com.lms.leave.controller;

import com.lms.leave.dto.LeaveDto;
import com.lms.leave.entity.Leave;
import com.lms.leave.entity.LeaveStatus;
import com.lms.leave.entity.PublicHoliday;
import com.lms.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Employee Leave Controller - Staff member functions
 * 
 * Endpoints:
 * - View leave balance
 * - Apply for leave
 * - Track application status
 * - View colleagues on leave
 * - View public holidays
 */
@RestController
@RequestMapping("/api/employee/leaves")
@RequiredArgsConstructor
@Tag(name = "Employee Leave", description = "Leave management endpoints for staff members")
@SecurityRequirement(name = "bearer-jwt")
public class EmployeeLeaveController {
    
    private final LeaveService leaveService;
    
    /**
     * Apply for leave
     */
    @PostMapping("/apply")
    public ResponseEntity<LeaveDto.LeaveResponse> applyForLeave(
            @Valid @RequestBody LeaveDto.CreateLeaveRequest request) {
        try {
            LeaveDto.LeaveResponse response = leaveService.createLeaveRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * View my leave balance for current year
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<List<LeaveDto.LeaveBalanceResponse>> getMyBalance(
            @PathVariable Long userId) {
        int currentYear = LocalDate.now().getYear();
        List<LeaveDto.LeaveBalanceResponse> balances = 
                leaveService.getLeaveBalance(userId, currentYear);
        return ResponseEntity.ok(balances);
    }
    
    /**
     * View my leave balance for specific year
     */
    @GetMapping("/balance/{userId}/{year}")
    public ResponseEntity<List<LeaveDto.LeaveBalanceResponse>> getMyBalanceForYear(
            @PathVariable Long userId,
            @PathVariable Integer year) {
        List<LeaveDto.LeaveBalanceResponse> balances = 
                leaveService.getLeaveBalance(userId, year);
        return ResponseEntity.ok(balances);
    }
    
    /**
     * View my leave requests (all statuses)
     */
    @GetMapping("/my-leaves/{userId}")
    public ResponseEntity<List<Leave>> getMyLeaves(@PathVariable Long userId) {
        List<Leave> leaves = leaveService.getLeavesByUserId(userId);
        return ResponseEntity.ok(leaves);
    }
    
    /**
     * View specific leave request details
     */
    @GetMapping("/{leaveId}")
    public ResponseEntity<Leave> getLeaveDetails(@PathVariable Long leaveId) {
        Leave leave = leaveService.getLeaveById(leaveId);
        return ResponseEntity.ok(leave);
    }
    
    /**
     * Cancel pending leave request
     */
    @PatchMapping("/{leaveId}/cancel")
    public ResponseEntity<String> cancelLeave(
            @PathVariable Long leaveId,
            @RequestParam Long userId) {
        try {
            leaveService.cancelLeave(leaveId, userId);
            return ResponseEntity.ok("Leave request cancelled successfully");
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * View colleagues currently on leave
     */
    @GetMapping("/colleagues-on-leave")
    public ResponseEntity<List<LeaveDto.ColleagueOnLeaveResponse>> getColleaguesOnLeave() {
        List<LeaveDto.ColleagueOnLeaveResponse> colleagues = 
                leaveService.getColleaguesOnLeave();
        return ResponseEntity.ok(colleagues);
    }
    
    /**
     * View upcoming public holidays
     */
    @GetMapping("/public-holidays")
    public ResponseEntity<List<PublicHoliday>> getPublicHolidays() {
        List<PublicHoliday> holidays = leaveService.getUpcomingPublicHolidays();
        return ResponseEntity.ok(holidays);
    }
}
