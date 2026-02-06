package com.edulingua.core.service

import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import com.edulingua.core.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Authentication service handling login, token generation, and validation.
 * Works with unified User model with role-based access control.
 */
@Service
@Transactional
class AuthenticationService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val auditService: AuditService,
    private val passwordEncoder: PasswordEncoder
) {

    @Value("\${edulingua.security.jwt-expiration-ms:86400000}")
    private var jwtExpirationMs: Long = 86400000

    /**
     * Authenticates user (email or username) and generates tokens
     */
    fun authenticate(
        loginRequest: LoginRequest,
        request: HttpServletRequest
    ): LoginResponse {
        // Try to find user by email or username
        val user = findUserByEmailOrUsername(loginRequest.emailOrUsername)
            ?: throw ResourceNotFoundException("Invalid credentials")

        if (!user.active) {
            auditService.logLogin(
                userId = user.id!!,
                userEmail = user.email,
                userRoles = user.roles.joinToString(",") { it.name },
                request = request,
                success = false
            )
            throw BusinessValidationException("Account is inactive. Please contact administrator.")
        }

        // Verify password with BCrypt
        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            auditService.logLogin(
                userId = user.id!!,
                userEmail = user.email,
                userRoles = user.roles.joinToString(",") { it.name },
                request = request,
                success = false
            )
            throw BusinessValidationException("Invalid credentials")
        }

        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id!!,
            email = user.email,
            username = user.username,
            roles = user.roles.map { it.name },
            permissions = user.getAllPermissions()
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            userId = user.id,
            email = user.email
        )

        // Log successful login
        auditService.logLogin(
            userId = user.id,
            userEmail = user.email,
            userRoles = user.roles.joinToString(",") { it.name },
            request = request,
            success = true
        )

        // Update last login
        val updatedUser = user.recordLogin()
        userRepository.save(updatedUser)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtExpirationMs / 1000,
            user = UserInfo(
                id = user.id,
                email = user.email,
                username = user.username,
                name = user.name,
                roles = user.roles.map { it.name },
                permissions = user.getAllPermissions()
            )
        )
    }

    /**
     * Refreshes access token using refresh token
     */
    fun refreshToken(refreshTokenRequest: RefreshTokenRequest): TokenRefreshResponse {
        val refreshToken = refreshTokenRequest.refreshToken

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessValidationException("Invalid or expired refresh token")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { ResourceNotFoundException("User not found") }

        if (!user.active) {
            throw BusinessValidationException("Account is inactive")
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id!!,
            email = user.email,
            username = user.username,
            roles = user.roles.map { it.name },
            permissions = user.getAllPermissions()
        )

        return TokenRefreshResponse(
            accessToken = accessToken,
            expiresIn = jwtExpirationMs / 1000
        )
    }

    /**
     * Changes user password
     */
    fun changePassword(
        userId: UUID,
        passwordChangeRequest: PasswordChangeRequest,
        request: HttpServletRequest
    ): AuthResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        if (!passwordEncoder.matches(passwordChangeRequest.currentPassword, user.password)) {
            auditService.logPasswordChange(
                userId = userId,
                userEmail = user.email,
                request = request,
                success = false
            )
            throw BusinessValidationException("Current password is incorrect")
        }

        val updatedUser = user.copy(
            password = passwordEncoder.encode(passwordChangeRequest.newPassword),
            updatedAt = java.time.LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        auditService.logPasswordChange(
            userId = userId,
            userEmail = user.email,
            request = request,
            success = true
        )

        return AuthResponse(
            success = true,
            message = "Password changed successfully"
        )
    }

    /**
     * Validates a token
     */
    @Transactional(readOnly = true)
    fun validateToken(token: String): Boolean {
        return jwtTokenProvider.validateToken(token)
    }

    /**
     * Gets user ID from token
     */
    @Transactional(readOnly = true)
    fun getUserIdFromToken(token: String): UUID {
        val userIdString = jwtTokenProvider.getUserIdFromToken(token)
        return UUID.fromString(userIdString)
    }

    /**
     * Helper method to find user by email or username
     */
    private fun findUserByEmailOrUsername(emailOrUsername: String): User? {
        return userRepository.findByEmail(emailOrUsername).orElse(null)
            ?: userRepository.findByUsername(emailOrUsername).orElse(null)
    }
}
