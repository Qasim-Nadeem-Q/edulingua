package com.edulingua.core.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * Unified User entity representing all users in the system.
 * Includes admin users, state/district/school/class coordinators, and students.
 * Uses role-based access control (RBAC) with many-to-many role mapping.
 */
@Entity
@Table(name = "users", indexes = [
    Index(name = "idx_user_email", columnList = "email"),
    Index(name = "idx_user_username", columnList = "username"),
    Index(name = "idx_user_active", columnList = "active")
])
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true)
    val username: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    val name: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    val password: String,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "email_verified")
    val emailVerified: Boolean = false,

    // Many-to-many relationship with roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = emptySet(),

    // Hierarchical location identifiers (for non-admin users)
    @Column(name = "state_code")
    val stateCode: String? = null,

    @Column(name = "state_name")
    val stateName: String? = null,

    @Column(name = "district_code")
    val districtCode: String? = null,

    @Column(name = "district_name")
    val districtName: String? = null,

    @Column(name = "school_code")
    val schoolCode: String? = null,

    @Column(name = "school_name")
    val schoolName: String? = null,

    @Column(name = "class_code")
    val classCode: String? = null,

    @Column(name = "class_name")
    val className: String? = null,

    // Student specific fields
    @Column(name = "roll_number")
    val rollNumber: String? = null,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDateTime? = null,

    @Column(name = "parent_email")
    val parentEmail: String? = null,

    @JsonIgnore
    @Column(name = "last_login")
    val lastLogin: LocalDateTime? = null,

    @JsonIgnore
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @JsonIgnore
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Returns a safe representation of the user without sensitive information.
     */
    fun toResponse(): UserResponse {
        return UserResponse(
            id = id!!,
            email = email,
            username = username,
            name = name,
            phoneNumber = phoneNumber,
            active = active,
            emailVerified = emailVerified,
            roles = roles.map { RoleInfo(it.id!!, it.name, it.description) },
            permissions = getAllPermissions(),
            stateCode = stateCode,
            stateName = stateName,
            districtCode = districtCode,
            districtName = districtName,
            schoolCode = schoolCode,
            schoolName = schoolName,
            classCode = classCode,
            className = className,
            rollNumber = rollNumber,
            dateOfBirth = dateOfBirth?.toString(),
            lastLogin = lastLogin?.toString(),
            createdAt = createdAt.toString()
        )
    }

    /**
     * Get all permissions from all assigned roles
     */
    fun getAllPermissions(): List<String> {
        return roles.flatMap { role ->
            role.permissions.map { it.name }
        }.distinct()
    }

    /**
     * Check if user has a specific permission
     */
    fun hasPermission(permissionName: String): Boolean {
        return roles.any { role -> role.hasPermission(permissionName) }
    }

    /**
     * Check if user has a specific role
     */
    fun hasRole(roleName: String): Boolean {
        return roles.any { it.name == roleName }
    }

    /**
     * Check if user has permission for a specific action on a resource
     */
    fun hasPermission(resource: String, action: PermissionAction): Boolean {
        return roles.any { role -> role.hasPermission(resource, action) }
    }

    /**
     * Creates a copy with updated fields and refreshed timestamp.
     */
    fun updateWith(
        name: String? = null,
        phoneNumber: String? = null,
        active: Boolean? = null,
        parentEmail: String? = null,
        emailVerified: Boolean? = null
    ): User {
        return this.copy(
            name = name ?: this.name,
            phoneNumber = phoneNumber ?: this.phoneNumber,
            active = active ?: this.active,
            parentEmail = parentEmail ?: this.parentEmail,
            emailVerified = emailVerified ?: this.emailVerified,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Record login timestamp
     */
    fun recordLogin(): User {
        return this.copy(
            lastLogin = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * Response representation of a User without sensitive data.
 */
data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val name: String,
    val phoneNumber: String?,
    val active: Boolean,
    val emailVerified: Boolean,
    val roles: List<RoleInfo>,
    val permissions: List<String>,
    val stateCode: String?,
    val stateName: String?,
    val districtCode: String?,
    val districtName: String?,
    val schoolCode: String?,
    val schoolName: String?,
    val classCode: String?,
    val className: String?,
    val rollNumber: String?,
    val dateOfBirth: String?,
    val lastLogin: String?,
    val createdAt: String
)

/**
 * Role information for response
 */
data class RoleInfo(
    val id: UUID,
    val name: String,
    val description: String?
)

/**
 * Request model for user updates.
 */
data class UserUpdateRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    val phoneNumber: String? = null,
    val active: Boolean? = null,
    val parentEmail: String? = null
)

/**
 * Request model for user creation
 */
data class UserCreateRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    val password: String,

    val phoneNumber: String? = null,

    @field:NotBlank(message = "At least one role is required")
    val roleIds: Set<UUID>,

    // Optional location fields
    val stateCode: String? = null,
    val stateName: String? = null,
    val districtCode: String? = null,
    val districtName: String? = null,
    val schoolCode: String? = null,
    val schoolName: String? = null,
    val classCode: String? = null,
    val className: String? = null,
    val rollNumber: String? = null,
    val dateOfBirth: LocalDateTime? = null,
    val parentEmail: String? = null
)

/**
 * Repository interface for User entity data access operations.
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    /**
     * Finds a user by their email address.
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * Finds a user by username
     */
    fun findByUsername(username: String): Optional<User>

    /**
     * Check if email exists
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Check if username exists
     */
    fun existsByUsername(username: String): Boolean

    /**
     * Find all active users
     */
    fun findByActiveTrue(): List<User>

    /**
     * Find users by role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun findByRoleName(roleName: String): List<User>

    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    fun findByRoleNameAndActiveTrue(roleName: String): List<User>

    // Hierarchical queries
    fun findByStateCode(stateCode: String): List<User>
    fun findByDistrictCode(districtCode: String): List<User>
    fun findBySchoolCode(schoolCode: String): List<User>
    fun findByClassCode(classCode: String): List<User>

    @Query("SELECT u FROM User u WHERE u.stateCode = :stateCode AND u.districtCode = :districtCode")
    fun findByStateAndDistrict(stateCode: String, districtCode: String): List<User>

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.schoolCode = :schoolCode AND u.classCode = :classCode AND r.name = 'STUDENT'")
    fun findStudentsByClass(schoolCode: String, classCode: String): List<User>
}
