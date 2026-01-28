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
 * User entity representing a user account in the system.
 * Combines domain model with validation for direct use in API requests/responses.
 */
@Entity
@Table(name = "users")
data class User(
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
    @field:Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    val password: String,

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
            name = name,
            createdAt = createdAt.toString()
        )
    }

    /**
     * Creates a copy with updated fields and refreshed timestamp.
     */
    fun updateWith(name: String? = null): User {
        return this.copy(
            name = name ?: this.name,
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * Response representation of a User without sensitive data.
 */
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val createdAt: String
)

/**
 * Request model for user updates.
 */
data class UserUpdateRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String?
)

/**
 * Repository interface for User entity data access operations.
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    /**
     * Finds a user by their email address.
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * Checks if a user exists with the given email address.
     */
    fun existsByEmail(email: String): Boolean
}
