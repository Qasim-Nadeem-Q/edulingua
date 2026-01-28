package com.edulingua.core.models

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Login request model
 */
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * Login response model
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserInfo
)

/**
 * User information in token response
 */
data class UserInfo(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
    val roleDescription: String,
    val userType: String,
    val permissions: List<String>
)

/**
 * Token refresh request
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

/**
 * Token refresh response
 */
data class TokenRefreshResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

/**
 * Password change request
 */
data class PasswordChangeRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    val newPassword: String
)

/**
 * Generic API response for auth operations
 */
data class AuthResponse(
    val success: Boolean,
    val message: String
)
