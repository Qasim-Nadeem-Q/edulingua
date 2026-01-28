package com.edulingua.api.controller

import com.edulingua.api.security.RequireAdmin
import com.edulingua.api.security.RequirePermission
import com.edulingua.core.models.AuditLog
import com.edulingua.core.models.Permission
import com.edulingua.core.service.AuditService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * Controller for viewing audit logs.
 * Only accessible by admins with appropriate permissions.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequireAdmin
class AuditController(
    private val auditService: AuditService
) {

    /**
     * Get audit logs by user ID
     */
    @GetMapping("/user/{userId}")
    @RequirePermission(Permission.VIEW_ALL_DATA)
    fun getAuditLogsByUser(@PathVariable userId: Long): ResponseEntity<List<AuditLog>> {
        val logs = auditService.getAuditLogsByUser(userId)
        return ResponseEntity.ok(logs)
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/date-range")
    @RequirePermission(Permission.VIEW_ALL_DATA)
    fun getAuditLogsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<AuditLog>> {
        val logs = auditService.getAuditLogsByDateRange(startDate, endDate)
        return ResponseEntity.ok(logs)
    }

    /**
     * Get failed actions (security monitoring)
     */
    @GetMapping("/failed-actions")
    @RequirePermission(Permission.VIEW_ALL_DATA)
    fun getFailedActions(): ResponseEntity<List<AuditLog>> {
        val logs = auditService.getFailedActions()
        return ResponseEntity.ok(logs)
    }

    /**
     * Get login attempts (security monitoring)
     */
    @GetMapping("/login-attempts")
    @RequirePermission(Permission.VIEW_ALL_DATA)
    fun getLoginAttempts(): ResponseEntity<List<AuditLog>> {
        val logs = auditService.getLoginAttempts()
        return ResponseEntity.ok(logs)
    }
}
