package com.lms.leave.controller;

import com.lms.leave.dto.LeaveDto;
import com.lms.leave.entity.Leave;
import com.lms.leave.entity.LeaveStatus;
import com.lms.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Manager Leave Controller - Manager approval functions
 * 
 * Endpoints:
 * - View pending leave requests
 * - Approve/reject leave requests with comments
 * - View team leave calendar
 */
@RestController
@RequestMapping("/api/manager/leaves")
@RequiredArgsConstructor
@Tag(name = "Manager Leave", description = "Leave approval and team management endpoints for managers")
@SecurityRequirement(name = "bearer-jwt")
public class ManagerLeaveController {
    
    private final LeaveService leaveService;
    
    /**
     * Get all pending leave requests (for manager review)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Leave>> getPendingLeaves() {
        List<Leave> pendingLeaves = leaveService.getLeavesByStatus(LeaveStatus.PENDING);
        return ResponseEntity.ok(pendingLeaves);
    }
    
    /**
     * Approve or reject a leave request
     */
    @PatchMapping("/{leaveId}/approve")
    public ResponseEntity<LeaveDto.LeaveResponse> approveOrRejectLeave(
            @PathVariable Long leaveId,
            @RequestParam Long managerId,
            @Valid @RequestBody LeaveDto.ApprovalRequest request) {
        try {
            LeaveDto.LeaveResponse response = 
                    leaveService.approveOrRejectLeave(leaveId, managerId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * View all leave requests (all statuses)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Leave>> getAllLeaves() {
        List<Leave> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(leaves);
    }
    
    /**
     * View leave requests by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Leave>> getLeavesByStatus(@PathVariable LeaveStatus status) {
        List<Leave> leaves = leaveService.getLeavesByStatus(status);
        return ResponseEntity.ok(leaves);
    }
    
    /**
     * View specific employee's leave history
     */
    @GetMapping("/employee/{userId}")
    public ResponseEntity<List<Leave>> getEmployeeLeaves(@PathVariable Long userId) {
        List<Leave> leaves = leaveService.getLeavesByUserId(userId);
        return ResponseEntity.ok(leaves);
    }
}
