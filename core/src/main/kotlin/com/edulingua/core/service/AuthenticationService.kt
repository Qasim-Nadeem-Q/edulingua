package com.edulingua.core.service

import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import com.edulingua.core.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Authentication service handling login, token generation, and validation.
 * TODO: Integrate password hashing (BCrypt) in production.
 */
@Service
@Transactional
class AuthenticationService(
    private val adminUserRepository: AdminUserRepository,
    private val consumerRepository: ConsumerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val auditService: AuditService
) {

    @Value("\${edulingua.security.jwt-expiration-ms:86400000}")
    private var jwtExpirationMs: Long = 86400000

    /**
     * Authenticates admin user and generates tokens
     */
    fun authenticateAdmin(
        loginRequest: LoginRequest,
        request: HttpServletRequest
    ): LoginResponse {
        val adminUser = adminUserRepository.findByEmail(loginRequest.email)
            .orElseThrow { ResourceNotFoundException("Invalid email or password") }

        if (!adminUser.active) {
            auditService.logLogin(
                userId = adminUser.id!!,
                userEmail = adminUser.email,
                userType = "ADMIN",
                userRole = adminUser.role.code,
                request = request,
                success = false
            )
            throw BusinessValidationException("Account is inactive. Please contact administrator.")
        }

        // TODO: Replace with BCrypt password verification
        if (adminUser.password != loginRequest.password) {
            auditService.logLogin(
                userId = adminUser.id!!,
                userEmail = adminUser.email,
                userType = "ADMIN",
                userRole = adminUser.role.code,
                request = request,
                success = false
            )
            throw BusinessValidationException("Invalid email or password")
        }

        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = adminUser.id!!,
            email = adminUser.email,
            role = adminUser.role,
            userType = "ADMIN"
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            userId = adminUser.id,
            email = adminUser.email
        )

        // Log successful login
        auditService.logLogin(
            userId = adminUser.id,
            userEmail = adminUser.email,
            userType = "ADMIN",
            userRole = adminUser.role.code,
            request = request,
            success = true
        )

        // Update last login
        val updatedUser = adminUser.copy(lastLogin = java.time.LocalDateTime.now())
        adminUserRepository.save(updatedUser)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtExpirationMs / 1000,
            user = UserInfo(
                id = adminUser.id,
                email = adminUser.email,
                name = adminUser.name,
                role = adminUser.role.code,
                roleDescription = adminUser.role.description,
                userType = "ADMIN",
                permissions = adminUser.role.permissions.map { it.name }
            )
        )
    }

    /**
     * Authenticates consumer and generates tokens
     */
    fun authenticateConsumer(
        loginRequest: LoginRequest,
        request: HttpServletRequest
    ): LoginResponse {
        val consumer = consumerRepository.findByEmail(loginRequest.email)
            .orElseThrow { ResourceNotFoundException("Invalid email or password") }

        if (!consumer.active) {
            auditService.logLogin(
                userId = consumer.id!!,
                userEmail = consumer.email,
                userType = "CONSUMER",
                userRole = consumer.role.code,
                request = request,
                success = false
            )
            throw BusinessValidationException("Account is inactive. Please contact administrator.")
        }

        // TODO: Replace with BCrypt password verification
        if (consumer.password != loginRequest.password) {
            auditService.logLogin(
                userId = consumer.id!!,
                userEmail = consumer.email,
                userType = "CONSUMER",
                userRole = consumer.role.code,
                request = request,
                success = false
            )
            throw BusinessValidationException("Invalid email or password")
        }

        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = consumer.id!!,
            email = consumer.email,
            role = consumer.role,
            userType = "CONSUMER"
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            userId = consumer.id,
            email = consumer.email
        )

        // Log successful login
        auditService.logLogin(
            userId = consumer.id,
            userEmail = consumer.email,
            userType = "CONSUMER",
            userRole = consumer.role.code,
            request = request,
            success = true
        )

        // Update last login
        val updatedConsumer = consumer.copy(lastLogin = java.time.LocalDateTime.now())
        consumerRepository.save(updatedConsumer)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtExpirationMs / 1000,
            user = UserInfo(
                id = consumer.id,
                email = consumer.email,
                name = consumer.name,
                role = consumer.role.code,
                roleDescription = consumer.role.description,
                userType = "CONSUMER",
                permissions = consumer.role.permissions.map { it.name }
            )
        )
    }

    /**
     * Refreshes access token using refresh token
     */
    fun refreshAccessToken(refreshTokenRequest: RefreshTokenRequest): TokenRefreshResponse {
        val refreshToken = refreshTokenRequest.refreshToken

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessValidationException("Invalid or expired refresh token")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val email = jwtTokenProvider.getEmailFromToken(refreshToken)

        // Try to find user in both repositories
        val adminUser = adminUserRepository.findById(userId).orElse(null)
        val consumer = consumerRepository.findById(userId).orElse(null)

        val (role, userType) = when {
            adminUser != null -> Pair(adminUser.role, "ADMIN")
            consumer != null -> Pair(consumer.role, "CONSUMER")
            else -> throw ResourceNotFoundException("User not found")
        }

        // Generate new access token
        val newAccessToken = jwtTokenProvider.generateAccessToken(
            userId = userId,
            email = email,
            role = role,
            userType = userType
        )

        return TokenRefreshResponse(
            accessToken = newAccessToken,
            expiresIn = jwtExpirationMs / 1000
        )
    }

    /**
     * Validates token and returns user info
     */
    fun validateTokenAndGetUserInfo(token: String): UserInfo {
        if (!jwtTokenProvider.validateToken(token)) {
            throw BusinessValidationException("Invalid or expired token")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(token)
        val email = jwtTokenProvider.getEmailFromToken(token)
        val roleCode = jwtTokenProvider.getRoleFromToken(token)
        val userType = jwtTokenProvider.getUserTypeFromToken(token)
        val permissions = jwtTokenProvider.getPermissionsFromToken(token)

        val role = Role.fromCode(roleCode)
            ?: throw BusinessValidationException("Invalid role in token")

        return UserInfo(
            id = userId,
            email = email,
            name = email.substringBefore("@"), // Fallback, should fetch from DB if needed
            role = roleCode,
            roleDescription = role.description,
            userType = userType,
            permissions = permissions
        )
    }
}
