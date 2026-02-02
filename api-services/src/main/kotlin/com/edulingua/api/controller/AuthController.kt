package com.edulingua.api.controller

import com.edulingua.api.filter.AuthContext
import com.edulingua.core.models.*
import com.edulingua.core.service.AuthenticationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Authentication controller for login, token refresh, and auth operations.
 * Works with unified User model supporting multiple roles.
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {

    /**
     * Unified login endpoint for all user types
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        val response = authenticationService.authenticate(loginRequest, request)
        return ResponseEntity.ok(response)
    }

    /**
     * Token refresh endpoint
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody refreshRequest: RefreshTokenRequest
    ): ResponseEntity<TokenRefreshResponse> {
        val response = authenticationService.refreshToken(refreshRequest)
        return ResponseEntity.ok(response)
    }

    /**
     * Change password endpoint
     */
    @PostMapping("/password/change")
    fun changePassword(
        @Valid @RequestBody passwordChangeRequest: PasswordChangeRequest,
        request: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val userId = AuthContext.getUserId(request)
            ?: return ResponseEntity.status(401).body(AuthResponse(false, "Unauthorized"))

        val response = authenticationService.changePassword(userId, passwordChangeRequest, request)
        return ResponseEntity.ok(response)
    }

    /**
     * Validate token endpoint
     */
    @GetMapping("/validate")
    fun validateToken(request: HttpServletRequest): ResponseEntity<Map<String, Boolean>> {
        val isAuthenticated = AuthContext.isAuthenticated(request)
        return ResponseEntity.ok(mapOf("valid" to isAuthenticated))
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    fun getCurrentUser(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val userId = AuthContext.getUserId(request)
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Unauthorized"))

        val email = AuthContext.getUserEmail(request) ?: ""
        val username = AuthContext.getUsername(request) ?: ""
        val roles = AuthContext.getUserRoles(request)
        val permissions = AuthContext.getPermissions(request)

        return ResponseEntity.ok(
            mapOf(
                "id" to userId.toString(),
                "email" to email,
                "username" to username,
                "roles" to roles,
                "permissions" to permissions
            )
        )
    }
}
