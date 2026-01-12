package com.lms.leave.controller;

import com.lms.leave.entity.Leave;
import com.lms.leave.entity.LeaveBalance;
import com.lms.leave.entity.LeaveType;
import com.lms.leave.entity.PublicHoliday;
import com.lms.leave.service.AdminService;
import com.lms.leave.service.LeaveService;
import com.lms.leave.dto.LeaveDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin/HR Controller - Administrative functions
 * 
 * Functions:
 * - Manage public holidays
 * - Adjust leave balances
 * - View all leave calendars
 * - Generate reports
 * - Process year-end carryover
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative and HR management endpoints for system administrators")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {
    
    private final AdminService adminService;
    private final LeaveService leaveService;
    
    // ========== Public Holiday Management ==========
    
    /**
     * Get pending leave requests from STAFF users only (for admin approval)
     */
    @GetMapping("/leaves/pending-staff")
    @Operation(summary = "Get pending leave requests from staff users only")
    public ResponseEntity<List<Leave>> getPendingStaffLeaves() {
        List<Leave> pendingStaffLeaves = leaveService.getPendingStaffLeaves();
        return ResponseEntity.ok(pendingStaffLeaves);
    }
    
    /**
     * Approve a staff leave request
     */
    @PatchMapping("/leaves/{leaveId}/approve")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<LeaveDto.LeaveResponse> approveLeave(
            @PathVariable Long leaveId,
            @RequestParam Long adminId,
            @Valid @RequestBody LeaveDto.ApprovalRequest request) {
        try {
            LeaveDto.LeaveResponse response = 
                    leaveService.approveOrRejectLeave(leaveId, adminId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Reject a staff leave request
     */
    @PatchMapping("/leaves/{leaveId}/reject")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<LeaveDto.LeaveResponse> rejectLeave(
            @PathVariable Long leaveId,
            @RequestParam Long adminId,
            @Valid @RequestBody LeaveDto.ApprovalRequest request) {
        try {
            LeaveDto.LeaveResponse response = 
                    leaveService.approveOrRejectLeave(leaveId, adminId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @PostMapping("/holidays")
    public ResponseEntity<PublicHoliday> addPublicHoliday(
            @Valid @RequestBody PublicHoliday holiday) {
        PublicHoliday created = adminService.addPublicHoliday(holiday);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> removePublicHoliday(@PathVariable Long id) {
        adminService.removePublicHoliday(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/holidays")
    public ResponseEntity<List<PublicHoliday>> getAllPublicHolidays() {
        return ResponseEntity.ok(adminService.getAllPublicHolidays());
    }
    
    // ========== Leave Balance Management ==========
    
    @PatchMapping("/balances/adjust")
    public ResponseEntity<LeaveBalance> adjustEmployeeBalance(
            @Valid @RequestBody BalanceAdjustmentRequest request) {
        LeaveBalance adjusted = adminService.adjustEmployeeBalance(
                request.getUserId(),
                request.getLeaveType(),
                request.getYear(),
                request.getAdjustment(),
                request.getReason()
        );
        return ResponseEntity.ok(adjusted);
    }
    
    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalance>> getAllLeaveBalances() {
        return ResponseEntity.ok(adminService.getAllLeaveBalances());
    }
    
    @GetMapping("/balances/{userId}/{year}")
    public ResponseEntity<List<LeaveBalance>> getUserLeaveBalance(
            @PathVariable Long userId,
            @PathVariable Integer year) {
        return ResponseEntity.ok(adminService.getUserLeaveBalance(userId, year));
    }
    
    // ========== Year-End Processing ==========
    
    @PostMapping("/year-end-carryover/{year}")
    public ResponseEntity<String> processYearEndCarryover(@PathVariable Integer year) {
        adminService.processYearEndCarryover(year);
        return ResponseEntity.ok("Year-end carryover processed for " + year);
    }
    
    // ========== Reports ==========
    
    @GetMapping("/reports/leaves")
    public ResponseEntity<?> generateLeaveReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        var report = adminService.generateLeaveReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/reports/balances/{year}")
    public ResponseEntity<List<LeaveBalance>> generateBalanceReport(
            @PathVariable Integer year) {
        return ResponseEntity.ok(adminService.generateBalanceReport(year));
    }
    
    @GetMapping("/reports/statistics/{year}")
    public ResponseEntity<AdminService.LeaveStatistics> getLeaveStatistics(
            @PathVariable Integer year) {
        return ResponseEntity.ok(adminService.getLeaveStatistics(year));
    }
    
    // ========== DTOs ==========
    
    @Data
    public static class BalanceAdjustmentRequest {
        private Long userId;
        private LeaveType leaveType;
        private Integer year;
        private Integer adjustment;  // Can be positive or negative
        private String reason;
    }
}
