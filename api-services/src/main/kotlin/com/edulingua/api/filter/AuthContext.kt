package com.edulingua.api.filter

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.util.*

/**
 * Utility class for extracting authentication context from HTTP requests.
 * Provides helper methods to access user information set by the AuthFilter.
 */
@Component
class AuthContext {

    companion object {
        private const val USER_ID_ATTRIBUTE = "userId"
        private const val USER_EMAIL_ATTRIBUTE = "userEmail"
        private const val USERNAME_ATTRIBUTE = "username"
        private const val USER_ROLES_ATTRIBUTE = "userRoles"
        private const val PERMISSIONS_ATTRIBUTE = "permissions"
        private const val TOKEN_ATTRIBUTE = "token"

        /**
         * Extracts the authenticated user ID from the request.
         */
        fun getUserId(request: HttpServletRequest): UUID? {
            return request.getAttribute(USER_ID_ATTRIBUTE) as? UUID
        }

        /**
         * Extracts the authenticated user email from the request.
         */
        fun getUserEmail(request: HttpServletRequest): String? {
            return request.getAttribute(USER_EMAIL_ATTRIBUTE) as? String
        }

        /**
         * Extracts the username from the request.
         */
        fun getUsername(request: HttpServletRequest): String? {
            return request.getAttribute(USERNAME_ATTRIBUTE) as? String
        }

        /**
         * Extracts the user roles from the request.
         */
        @Suppress("UNCHECKED_CAST")
        fun getUserRoles(request: HttpServletRequest): List<String> {
            return request.getAttribute(USER_ROLES_ATTRIBUTE) as? List<String> ?: emptyList()
        }

        /**
         * Extracts the user permissions from the request.
         */
        @Suppress("UNCHECKED_CAST")
        fun getPermissions(request: HttpServletRequest): List<String> {
            return request.getAttribute(PERMISSIONS_ATTRIBUTE) as? List<String> ?: emptyList()
        }

        /**
         * Extracts the JWT token from the request.
         */
        fun getToken(request: HttpServletRequest): String? {
            return request.getAttribute(TOKEN_ATTRIBUTE) as? String
        }

        /**
         * Checks if the request is authenticated.
         */
        fun isAuthenticated(request: HttpServletRequest): Boolean {
            return getUserId(request) != null
        }

        /**
         * Checks if user has a specific permission.
         */
        fun hasPermission(request: HttpServletRequest, permission: String): Boolean {
            val permissions = getPermissions(request)
            return permissions.contains(permission)
        }

        /**
         * Checks if user has any of the specified permissions.
         */
        fun hasAnyPermission(request: HttpServletRequest, vararg permissions: String): Boolean {
            val userPermissions = getPermissions(request)
            return permissions.any { userPermissions.contains(it) }
        }

        /**
         * Checks if user has all of the specified permissions.
         */
        fun hasAllPermissions(request: HttpServletRequest, vararg permissions: String): Boolean {
            val userPermissions = getPermissions(request)
            return permissions.all { userPermissions.contains(it) }
        }

        /**
         * Checks if user has a specific role.
         */
        fun hasRole(request: HttpServletRequest, roleName: String): Boolean {
            val roles = getUserRoles(request)
            return roles.contains(roleName)
        }

        /**
         * Checks if user has any of the specified roles.
         */
        fun hasAnyRole(request: HttpServletRequest, vararg roleNames: String): Boolean {
            val roles = getUserRoles(request)
            return roleNames.any { roles.contains(it) }
        }

        /**
         * Checks if user is admin.
         */
        fun isAdmin(request: HttpServletRequest): Boolean {
            return hasRole(request, "ADMIN")
        }

        /**
         * Checks if user is consumer (non-admin users).
         */
        fun isConsumer(request: HttpServletRequest): Boolean {
            val roles = getUserRoles(request)
            return roles.any { it in listOf("STATE", "DISTRICT", "SCHOOL", "CLASS", "STUDENT") }
        }

        /**
         * Gets the primary user type based on roles.
         * Returns the highest privilege role.
         */
        fun getUserType(request: HttpServletRequest): String {
            val roles = getUserRoles(request)
            return when {
                roles.contains("ADMIN") -> "ADMIN"
                roles.contains("STATE") -> "STATE"
                roles.contains("DISTRICT") -> "DISTRICT"
                roles.contains("SCHOOL") -> "SCHOOL"
                roles.contains("CLASS") -> "CLASS"
                roles.contains("STUDENT") -> "STUDENT"
                else -> "UNKNOWN"
            }
        }
    }
}
