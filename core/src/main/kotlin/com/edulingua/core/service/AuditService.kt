package com.edulingua.core.service

import com.edulingua.core.models.AuditAction
import com.edulingua.core.models.AuditLog
import com.edulingua.core.models.AuditLogRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service for audit logging and accountability tracking.
 * All actions are logged asynchronously to not impact performance.
 */
@Service
@Transactional
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(AuditService::class.java)

    /**
     * Logs an action asynchronously
     */
    @Async
    fun logAction(
        userId: Long,
        userEmail: String,
        userType: String,
        userRole: String,
        action: AuditAction,
        resourceType: String? = null,
        resourceId: String? = null,
        description: String? = null,
        request: HttpServletRequest? = null,
        oldValue: Any? = null,
        newValue: Any? = null,
        success: Boolean = true,
        errorMessage: String? = null
    ) {
        try {
            val auditLog = AuditLog(
                userId = userId,
                userEmail = userEmail,
                userType = userType,
                userRole = userRole,
                action = action.name,
                resourceType = resourceType,
                resourceId = resourceId,
                description = description ?: action.description,
                ipAddress = request?.let { getClientIpAddress(it) },
                userAgent = request?.getHeader("User-Agent"),
                requestMethod = request?.method,
                requestPath = request?.requestURI,
                statusCode = null, // Can be set from response
                oldValue = oldValue?.let { toJson(it) },
                newValue = newValue?.let { toJson(it) },
                timestamp = LocalDateTime.now(),
                success = success,
                errorMessage = errorMessage
            )

            auditLogRepository.save(auditLog)

            logger.info(
                "Audit: {} - User: {} ({}:{}) - Action: {} - Resource: {}:{} - Success: {}",
                auditLog.id,
                userEmail,
                userType,
                userRole,
                action.name,
                resourceType ?: "N/A",
                resourceId ?: "N/A",
                success
            )
        } catch (e: Exception) {
            logger.error("Failed to save audit log", e)
        }
    }

    /**
     * Logs a login attempt
     */
    @Async
    fun logLogin(
        userId: Long,
        userEmail: String,
        userType: String,
        userRole: String,
        request: HttpServletRequest,
        success: Boolean = true
    ) {
        logAction(
            userId = userId,
            userEmail = userEmail,
            userType = userType,
            userRole = userRole,
            action = if (success) AuditAction.LOGIN else AuditAction.LOGIN_FAILED,
            description = if (success) "Successful login" else "Failed login attempt",
            request = request,
            success = success
        )
    }

    /**
     * Logs a logout
     */
    @Async
    fun logLogout(
        userId: Long,
        userEmail: String,
        userType: String,
        userRole: String,
        request: HttpServletRequest
    ) {
        logAction(
            userId = userId,
            userEmail = userEmail,
            userType = userType,
            userRole = userRole,
            action = AuditAction.LOGOUT,
            description = "User logged out",
            request = request
        )
    }

    /**
     * Retrieves audit logs by user
     */
    @Transactional(readOnly = true)
    fun getAuditLogsByUser(userId: Long): List<AuditLog> {
        return auditLogRepository.findByUserId(userId)
    }

    /**
     * Retrieves audit logs by date range
     */
    @Transactional(readOnly = true)
    fun getAuditLogsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<AuditLog> {
        return auditLogRepository.findByDateRange(startDate, endDate)
    }

    /**
     * Retrieves failed actions
     */
    @Transactional(readOnly = true)
    fun getFailedActions(): List<AuditLog> {
        return auditLogRepository.findFailedActions()
    }

    /**
     * Retrieves login attempts
     */
    @Transactional(readOnly = true)
    fun getLoginAttempts(): List<AuditLog> {
        return auditLogRepository.findLoginAttempts()
    }

    /**
     * Converts object to JSON string
     */
    private fun toJson(obj: Any): String? {
        return try {
            objectMapper.writeValueAsString(obj)
        } catch (e: Exception) {
            logger.warn("Failed to convert object to JSON", e)
            obj.toString()
        }
    }

    /**
     * Extracts client IP address from request
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        return request.remoteAddr ?: "unknown"
    }
}
