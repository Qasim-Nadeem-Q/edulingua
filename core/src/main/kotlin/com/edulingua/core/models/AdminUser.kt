package com.edulingua.core.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * Admin User entity - for system administrators who manage the platform.
 * Role: ADMIN (A)
 */
@Entity
@Table(name = "admin_users")
data class AdminUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    val email: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    val name: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.ADMIN,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

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
    fun toResponse(): AdminUserResponse {
        return AdminUserResponse(
            id = id!!,
            email = email,
            name = name,
            role = role.code,
            active = active,
            phoneNumber = phoneNumber,
            lastLogin = lastLogin?.toString(),
            createdAt = createdAt.toString()
        )
    }

    fun hasPermission(permission: Permission): Boolean {
        return role.hasPermission(permission)
    }

    fun updateWith(
        name: String? = null,
        phoneNumber: String? = null,
        active: Boolean? = null
    ): AdminUser {
        return this.copy(
            name = name ?: this.name,
            phoneNumber = phoneNumber ?: this.phoneNumber,
            active = active ?: this.active,
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * Response model for AdminUser
 */
data class AdminUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
    val active: Boolean,
    val phoneNumber: String?,
    val lastLogin: String?,
    val createdAt: String
)

/**
 * Request model for updating admin user
 */
data class AdminUserUpdateRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    val phoneNumber: String? = null,
    val active: Boolean? = null
)

/**
 * Repository for AdminUser entity
 */
@Repository
interface AdminUserRepository : JpaRepository<AdminUser, Long> {
    fun findByEmail(email: String): Optional<AdminUser>
    fun existsByEmail(email: String): Boolean
    fun findByActiveTrue(): List<AdminUser>
}
