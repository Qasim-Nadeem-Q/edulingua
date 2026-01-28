package com.edulingua.api.filter

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

/**
 * Utility class for extracting authentication context from HTTP requests.
 * Provides helper methods to access user information set by the AuthFilter.
 */
@Component
class AuthContext {

    companion object {
        private const val USER_ID_ATTRIBUTE = "userId"
        private const val USER_EMAIL_ATTRIBUTE = "userEmail"
        private const val USER_ROLE_ATTRIBUTE = "userRole"
        private const val USER_TYPE_ATTRIBUTE = "userType"
        private const val PERMISSIONS_ATTRIBUTE = "permissions"
        private const val TOKEN_ATTRIBUTE = "token"

        /**
         * Extracts the authenticated user ID from the request.
         */
        fun getUserId(request: HttpServletRequest): Long? {
            return request.getAttribute(USER_ID_ATTRIBUTE) as? Long
        }

        /**
         * Extracts the authenticated user email from the request.
         */
        fun getUserEmail(request: HttpServletRequest): String? {
            return request.getAttribute(USER_EMAIL_ATTRIBUTE) as? String
        }

        /**
         * Extracts the user role code from the request.
         */
        fun getUserRole(request: HttpServletRequest): String? {
            return request.getAttribute(USER_ROLE_ATTRIBUTE) as? String
        }

        /**
         * Extracts the user type (ADMIN or CONSUMER) from the request.
         */
        fun getUserType(request: HttpServletRequest): String? {
            return request.getAttribute(USER_TYPE_ATTRIBUTE) as? String
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
         * Checks if user is admin.
         */
        fun isAdmin(request: HttpServletRequest): Boolean {
            return getUserType(request) == "ADMIN"
        }

        /**
         * Checks if user is consumer.
         */
        fun isConsumer(request: HttpServletRequest): Boolean {
            return getUserType(request) == "CONSUMER"
        }
    }
}
