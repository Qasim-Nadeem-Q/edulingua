package com.edulingua.api.controller

import com.edulingua.core.models.*
import com.edulingua.core.service.AuthenticationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Authentication controller for login, token refresh, and auth operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {

    /**
     * Admin login endpoint
     */
    @PostMapping("/admin/login")
    fun adminLogin(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        val response = authenticationService.authenticateAdmin(loginRequest, request)
        return ResponseEntity.ok(response)
    }

    /**
     * Consumer login endpoint
     */
    @PostMapping("/consumer/login")
    fun consumerLogin(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        val response = authenticationService.authenticateConsumer(loginRequest, request)
        return ResponseEntity.ok(response)
    }

    /**
     * Token refresh endpoint
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody refreshRequest: RefreshTokenRequest
    ): ResponseEntity<TokenRefreshResponse> {
        val response = authenticationService.refreshAccessToken(refreshRequest)
        return ResponseEntity.ok(response)
    }

    /**
     * Validate token endpoint
     */
    @GetMapping("/validate")
    fun validateToken(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserInfo> {
        val token = authHeader.removePrefix("Bearer ")
        val userInfo = authenticationService.validateTokenAndGetUserInfo(token)
        return ResponseEntity.ok(userInfo)
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    fun getCurrentUser(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserInfo> {
        val token = authHeader.removePrefix("Bearer ")
        val userInfo = authenticationService.validateTokenAndGetUserInfo(token)
        return ResponseEntity.ok(userInfo)
    }
}
