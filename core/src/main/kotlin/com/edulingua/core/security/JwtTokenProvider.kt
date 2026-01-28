package com.edulingua.core.security

import com.edulingua.core.models.Permission
import com.edulingua.core.models.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT utility for token generation, validation, and claims extraction.
 */
@Component
class JwtTokenProvider {

    @Value("\${edulingua.security.jwt-secret:your-256-bit-secret-key-change-this-in-production-must-be-at-least-256-bits}")
    private lateinit var jwtSecret: String

    @Value("\${edulingua.security.jwt-expiration-ms:86400000}")
    private var jwtExpirationMs: Long = 86400000 // 24 hours

    @Value("\${edulingua.security.jwt-refresh-expiration-ms:604800000}")
    private var jwtRefreshExpirationMs: Long = 604800000 // 7 days

    private fun getSigningKey(): Key {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Generates access token for authenticated user
     */
    fun generateAccessToken(
        userId: Long,
        email: String,
        role: Role,
        userType: String // "ADMIN" or "CONSUMER"
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("role", role.code)
            .claim("roleDescription", role.description)
            .claim("userType", userType)
            .claim("permissions", role.permissions.map { it.name })
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Generates refresh token for token renewal
     */
    fun generateRefreshToken(userId: Long, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtRefreshExpirationMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("tokenType", "REFRESH")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Extracts user ID from token
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getAllClaimsFromToken(token)
        return claims.subject.toLong()
    }

    /**
     * Extracts email from token
     */
    fun getEmailFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims["email"] as String
    }

    /**
     * Extracts role from token
     */
    fun getRoleFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims["role"] as String
    }

    /**
     * Extracts user type from token
     */
    fun getUserTypeFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims["userType"] as String
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
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
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
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
            !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if user has specific permission
     */
    fun hasPermission(token: String, permission: Permission): Boolean {
        val permissions = getPermissionsFromToken(token)
        return permissions.contains(permission.name)
    }

    /**
     * Checks if user has any of the specified permissions
     */
    fun hasAnyPermission(token: String, vararg permissions: Permission): Boolean {
        val userPermissions = getPermissionsFromToken(token)
        return permissions.any { userPermissions.contains(it.name) }
    }

    /**
     * Checks if user has all of the specified permissions
     */
    fun hasAllPermissions(token: String, vararg permissions: Permission): Boolean {
        val userPermissions = getPermissionsFromToken(token)
        return permissions.all { userPermissions.contains(it.name) }
    }
}
