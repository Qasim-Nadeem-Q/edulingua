package com.edulingua.core.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT utility for token generation, validation, and claims extraction.
 * Works with unified User model supporting multiple roles and permissions.
 */
@Component
class JwtTokenProvider {

    @Value("\${edulingua.security.jwt-secret:your-256-bit-secret-key-change-this-in-production-must-be-at-least-256-bits}")
    private lateinit var jwtSecret: String

    @Value("\${edulingua.security.jwt-expiration-ms:86400000}")
    private var jwtExpirationMs: Long = 86400000 // 24 hours

    @Value("\${edulingua.security.jwt-refresh-expiration-ms:604800000}")
    private var jwtRefreshExpirationMs: Long = 604800000 // 7 days

    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Generates access token for authenticated user
     */
    fun generateAccessToken(
        userId: UUID,
        email: String,
        username: String,
        roles: List<String>,
        permissions: List<String>
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("username", username)
            .claim("roles", roles)
            .claim("permissions", permissions)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    /**
     * Generates refresh token for token renewal
     */
    fun generateRefreshToken(userId: UUID, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtRefreshExpirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("tokenType", "REFRESH")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    /**
     * Extracts user ID from token
     */
    fun getUserIdFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims.subject
    }

    /**
     * Extracts email from token
     */
    fun getEmailFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims["email"] as String
    }

    /**
     * Extracts username from token
     */
    fun getUsernameFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims["username"] as String
    }

    /**
     * Extracts roles from token
     */
    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String): List<String> {
        val claims = getAllClaimsFromToken(token)
        return claims["roles"] as? List<String> ?: emptyList()
    }

    /**
     * Extracts permissions from token
     */
    @Suppress("UNCHECKED_CAST")
    fun getPermissionsFromToken(token: String): List<String> {
        val claims = getAllClaimsFromToken(token)
        return claims["permissions"] as? List<String> ?: emptyList()
    }

    /**
     * Extracts expiration date from token
     */
    fun getExpirationDateFromToken(token: String): Date {
        val claims = getAllClaimsFromToken(token)
        return claims.expiration
    }

    /**
     * Extracts all claims from token
     */
    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * Checks if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = getExpirationDateFromToken(token)
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Validates token
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
            !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if user has specific permission
     */
    fun hasPermission(token: String, permissionName: String): Boolean {
        val permissions = getPermissionsFromToken(token)
        return permissions.contains(permissionName)
    }

    /**
     * Checks if user has any of the specified permissions
     */
    fun hasAnyPermission(token: String, vararg permissionNames: String): Boolean {
        val userPermissions = getPermissionsFromToken(token)
        return permissionNames.any { userPermissions.contains(it) }
    }

    /**
     * Checks if user has all of the specified permissions
     */
    fun hasAllPermissions(token: String, vararg permissionNames: String): Boolean {
        val userPermissions = getPermissionsFromToken(token)
        return permissionNames.all { userPermissions.contains(it) }
    }

    /**
     * Checks if user has specific role
     */
    fun hasRole(token: String, roleName: String): Boolean {
        val roles = getRolesFromToken(token)
        return roles.contains(roleName)
    }

    /**
     * Checks if user has any of the specified roles
     */
    fun hasAnyRole(token: String, vararg roleNames: String): Boolean {
        val userRoles = getRolesFromToken(token)
        return roleNames.any { userRoles.contains(it) }
    }
}
