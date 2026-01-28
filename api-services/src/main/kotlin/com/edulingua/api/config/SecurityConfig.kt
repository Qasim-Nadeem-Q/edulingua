package com.edulingua.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for authentication and security settings.
 * Can be customized via application.yml or application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "edulingua.security")
data class SecurityConfig(
    /**
     * Enable or disable authentication filter globally.
     */
    var enabled: Boolean = true,

    /**
     * JWT secret key for token validation.
     */
    var jwtSecret: String = "your-secret-key-change-in-production",

    /**
     * JWT token expiration time in milliseconds (default: 24 hours).
     */
    var jwtExpirationMs: Long = 86400000,

    /**
     * List of additional public endpoints that don't require authentication.
     */
    var publicEndpoints: List<String> = emptyList(),

    /**
     * Enable API key authentication.
     */
    var apiKeyEnabled: Boolean = true,

    /**
     * Enable Bearer token authentication.
     */
    var bearerTokenEnabled: Boolean = true,

    /**
     * Maximum number of authentication attempts before rate limiting.
     */
    var maxAuthAttempts: Int = 5,

    /**
     * Rate limit window in minutes.
     */
    var rateLimitWindowMinutes: Int = 15
)
