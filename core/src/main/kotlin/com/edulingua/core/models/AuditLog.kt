package com.edulingua.core.models

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Audit log entity for tracking all user actions (accountability).
 * Provides complete audit trail for compliance and security.
 */
@Entity
@Table(name = "audit_logs", indexes = [
    Index(name = "idx_audit_user", columnList = "user_id,user_type"),
    Index(name = "idx_audit_action", columnList = "action"),
    Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    Index(name = "idx_audit_resource", columnList = "resource_type,resource_id")
])
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "user_email", nullable = false)
    val userEmail: String,

    @Column(name = "user_type", nullable = false)
    val userType: String, // ADMIN or CONSUMER

    @Column(name = "user_role", nullable = false)
    val userRole: String, // A, S, D, SC, CL, ST

    @Column(nullable = false)
    val action: String, // CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, etc.

    @Column(name = "resource_type")
    val resourceType: String? = null, // User, Consumer, Test, Question, etc.

    @Column(name = "resource_id")
    val resourceId: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null,

    @Column(name = "request_method")
    val requestMethod: String? = null, // GET, POST, PUT, DELETE

    @Column(name = "request_path", columnDefinition = "TEXT")
    val requestPath: String? = null,

    @Column(name = "status_code")
    val statusCode: Int? = null,

    @Column(name = "old_value", columnDefinition = "TEXT")
    val oldValue: String? = null, // JSON of old state

    @Column(name = "new_value", columnDefinition = "TEXT")
    val newValue: String? = null, // JSON of new state

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val success: Boolean = true,

    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null
)

/**
 * Audit action types
 */
enum class AuditAction(val description: String) {
    // Authentication
    LOGIN("User login"),
    LOGOUT("User logout"),
    LOGIN_FAILED("Failed login attempt"),
    TOKEN_REFRESH("Token refresh"),
    PASSWORD_CHANGE("Password changed"),

    // User management
    USER_CREATE("User created"),
    USER_READ("User viewed"),
    USER_UPDATE("User updated"),
    USER_DELETE("User deleted"),
    USER_ACTIVATE("User activated"),
    USER_DEACTIVATE("User deactivated"),

    // Consumer management
    CONSUMER_CREATE("Consumer created"),
    CONSUMER_READ("Consumer viewed"),
    CONSUMER_UPDATE("Consumer updated"),
    CONSUMER_DELETE("Consumer deleted"),

    // Test management
    TEST_CREATE("Test created"),
    TEST_READ("Test viewed"),
    TEST_UPDATE("Test updated"),
    TEST_DELETE("Test deleted"),
    TEST_START("Test started"),
    TEST_SUBMIT("Test submitted"),

    // Question management
    QUESTION_CREATE("Question created"),
    QUESTION_READ("Question viewed"),
    QUESTION_UPDATE("Question updated"),
    QUESTION_DELETE("Question deleted"),

    // Report generation
    REPORT_GENERATE("Report generated"),
    REPORT_VIEW("Report viewed"),
    REPORT_EXPORT("Report exported"),

    // Data export
    DATA_EXPORT("Data exported"),

    // System actions
    CONFIG_CHANGE("Configuration changed"),
    PERMISSION_GRANT("Permission granted"),
    PERMISSION_REVOKE("Permission revoked")
}

/**
 * Repository for audit logs
 */
@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {

    fun findByUserId(userId: Long): List<AuditLog>

    fun findByUserIdAndUserType(userId: Long, userType: String): List<AuditLog>

    fun findByAction(action: String): List<AuditLog>

    fun findByResourceTypeAndResourceId(resourceType: String, resourceId: String): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.timestamp BETWEEN :startDate AND :endDate")
    fun findByUserAndDateRange(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.success = false ORDER BY a.timestamp DESC")
    fun findFailedActions(): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.action LIKE '%LOGIN%' ORDER BY a.timestamp DESC")
    fun findLoginAttempts(): List<AuditLog>
}
