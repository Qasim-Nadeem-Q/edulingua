package com.edulingua.api.filter

import com.edulingua.core.exception.OperationNotPermittedException
import com.edulingua.core.security.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

/**
 * Authentication filter for API endpoints.
 * Validates JWT tokens and enforces security policies.
 * Executes once per request before reaching the controller layer.
 */
@Component
@Order(1)
class AuthFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(AuthFilter::class.java)

    companion object {
        private const val AUTH_HEADER = "x-edulingua-authorization"
        private const val BEARER_PREFIX = "Bearer "

        // Public endpoints that don't require authentication
        private val PUBLIC_ENDPOINTS = setOf(
            "/api/health",
            "/api/v1/health",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "/h2-console"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestPath = request.requestURI
        val method = request.method

        logger.debug("Processing authentication for {} {}", method, requestPath)

        try {
            // Skip authentication for public endpoints
            if (isPublicEndpoint(requestPath)) {
                logger.debug("Public endpoint accessed: {}", requestPath)
                filterChain.doFilter(request, response)
                return
            }

            // Extract and validate JWT token
            val token = extractTokenFromRequest(request)

            if (token == null || !jwtTokenProvider.validateToken(token)) {
                logger.warn("Invalid or missing token for {} {}", method, requestPath)
                throw OperationNotPermittedException("Authentication required. Please provide valid credentials.")
            }

            // Extract user information from token and set in request attributes
            val userId = jwtTokenProvider.getUserIdFromToken(token)
            val email = jwtTokenProvider.getEmailFromToken(token)
            val username = jwtTokenProvider.getUsernameFromToken(token)
            val roles = jwtTokenProvider.getRolesFromToken(token)
            val permissions = jwtTokenProvider.getPermissionsFromToken(token)

            request.setAttribute("userId", UUID.fromString(userId))
            request.setAttribute("userEmail", email)
            request.setAttribute("username", username)
            request.setAttribute("userRoles", roles)
            request.setAttribute("permissions", permissions)
            request.setAttribute("token", token)

            logger.debug("Authentication successful for user: {} ({})", email, roles.joinToString(","))

            // Continue with the filter chain
            filterChain.doFilter(request, response)

        } catch (ex: OperationNotPermittedException) {
            logger.warn("Authentication error: {}", ex.message)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.message)
        } catch (ex: Exception) {
            logger.error("Unexpected error in authentication filter", ex)
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication processing error")
        }
    }

    /**
     * Checks if the endpoint is public and doesn't require authentication.
     */
    private fun isPublicEndpoint(path: String): Boolean {
        return PUBLIC_ENDPOINTS.any { path.startsWith(it) }
    }

    /**
     * Extracts JWT token from Authorization header
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(AUTH_HEADER)

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length).trim()
        }

        return null
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        // Filter all requests by default
        return false
    }
}
