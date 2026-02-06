package com.edulingua.core.service

import com.edulingua.core.exception.OperationNotPermittedException
import com.edulingua.core.models.PermissionAction
import com.edulingua.core.models.User
import com.edulingua.core.models.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Authorization service for hierarchical permission and data scope validation.
 * Enforces RBAC with organizational hierarchy awareness.
 */
@Service
@Transactional(readOnly = true)
class AuthorizationService(
    private val userRepository: UserRepository
) {

    /**
     * Check if user has specific permission
     */
    fun hasPermission(userId: UUID, permissionName: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return user.hasPermission(permissionName)
    }

    /**
     * Check if user has permission for resource and action
     */
    fun hasPermission(userId: UUID, resource: String, action: PermissionAction): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return user.hasPermission(resource, action)
    }

    /**
     * Check if user has any of the specified permissions
     */
    fun hasAnyPermission(userId: UUID, vararg permissions: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return permissions.any { user.hasPermission(it) }
    }

    /**
     * Check if user has all of the specified permissions
     */
    fun hasAllPermissions(userId: UUID, vararg permissions: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return permissions.all { user.hasPermission(it) }
    }

    /**
     * Check if user has specific role
     */
    fun hasRole(userId: UUID, roleName: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return user.hasRole(roleName)
    }

    /**
     * Check if user is admin
     */
    fun isAdmin(userId: UUID): Boolean {
        return hasRole(userId, "ADMIN")
    }

    /**
     * Check if user can manage another user based on hierarchy
     */
    fun canManageUser(managerId: UUID, targetUserId: UUID): Boolean {
        val manager = userRepository.findById(managerId).orElse(null) ?: return false
        val target = userRepository.findById(targetUserId).orElse(null) ?: return false

        // Admin can manage everyone
        if (manager.hasRole("ADMIN")) return true

        // State coordinator can manage users in their state
        if (manager.hasRole("STATE")) {
            return manager.stateCode != null && manager.stateCode == target.stateCode
        }

        // District coordinator can manage users in their district
        if (manager.hasRole("DISTRICT")) {
            return manager.stateCode == target.stateCode &&
                   manager.districtCode != null && manager.districtCode == target.districtCode
        }

        // School coordinator can manage users in their school
        if (manager.hasRole("SCHOOL")) {
            return manager.schoolCode != null && manager.schoolCode == target.schoolCode
        }

        // Class teacher can manage students in their class
        if (manager.hasRole("CLASS")) {
            return manager.schoolCode == target.schoolCode &&
                   manager.classCode != null && manager.classCode == target.classCode &&
                   target.hasRole("STUDENT")
        }

        return false
    }

    /**
     * Verify user can manage another user, throw exception if not
     */
    fun requireCanManageUser(managerId: UUID, targetUserId: UUID) {
        if (!canManageUser(managerId, targetUserId)) {
            throw OperationNotPermittedException(
                "You don't have permission to manage this user based on organizational hierarchy"
            )
        }
    }

    /**
     * Check if user can access data in specific state
     */
    fun canAccessState(userId: UUID, stateCode: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false

        // Admin can access all states
        if (user.hasRole("ADMIN")) return true

        // Others can only access their own state
        return user.stateCode == stateCode
    }

    /**
     * Check if user can access data in specific district
     */
    fun canAccessDistrict(userId: UUID, districtCode: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false

        // Admin can access all districts
        if (user.hasRole("ADMIN")) return true

        // State coordinator can access all districts in their state
        if (user.hasRole("STATE") && user.stateCode != null) {
            // Need to verify district belongs to user's state
            // For now, we'll check if district code prefix matches
            return true // TODO: Implement proper district-state mapping
        }

        // Others can only access their own district
        return user.districtCode == districtCode
    }

    /**
     * Check if user can access data in specific school
     */
    fun canAccessSchool(userId: UUID, schoolCode: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false

        // Admin can access all schools
        if (user.hasRole("ADMIN")) return true

        // State/District coordinators can access schools in their area
        if (user.hasRole("STATE") || user.hasRole("DISTRICT")) {
            return true // TODO: Implement proper school-district-state mapping
        }

        // Others can only access their own school
        return user.schoolCode == schoolCode
    }

    /**
     * Check if user can access data in specific class
     */
    fun canAccessClass(userId: UUID, schoolCode: String, classCode: String): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false

        // Admin can access all classes
        if (user.hasRole("ADMIN")) return true

        // State/District/School coordinators can access classes in their area
        if (user.hasRole("STATE") || user.hasRole("DISTRICT")) {
            return canAccessSchool(userId, schoolCode)
        }

        if (user.hasRole("SCHOOL")) {
            return user.schoolCode == schoolCode
        }

        // Class teacher can only access their own class
        if (user.hasRole("CLASS")) {
            return user.schoolCode == schoolCode && user.classCode == classCode
        }

        // Students can access their own class
        if (user.hasRole("STUDENT")) {
            return user.schoolCode == schoolCode && user.classCode == classCode
        }

        return false
    }

    /**
     * Check if user owns the resource (is the resource itself)
     */
    fun isResourceOwner(userId: UUID, resourceUserId: UUID): Boolean {
        return userId == resourceUserId
    }

    /**
     * Check if user can edit their own profile
     */
    fun canEditOwnProfile(userId: UUID, targetUserId: UUID): Boolean {
        return userId == targetUserId
    }

    /**
     * Get all permissions for a user
     */
    fun getUserPermissions(userId: UUID): List<String> {
        val user = userRepository.findById(userId).orElse(null) ?: return emptyList()
        return user.getAllPermissions()
    }

    /**
     * Get effective role hierarchy level (lower number = higher privilege)
     */
    fun getRoleLevel(userId: UUID): Int {
        val user = userRepository.findById(userId).orElse(null) ?: return 999

        return when {
            user.hasRole("ADMIN") -> 0
            user.hasRole("STATE") -> 1
            user.hasRole("DISTRICT") -> 2
            user.hasRole("SCHOOL") -> 3
            user.hasRole("CLASS") -> 4
            user.hasRole("STUDENT") -> 5
            else -> 999
        }
    }

    /**
     * Check if manager has higher privilege than target
     */
    fun hasHigherPrivilege(managerId: UUID, targetUserId: UUID): Boolean {
        val managerLevel = getRoleLevel(managerId)
        val targetLevel = getRoleLevel(targetUserId)
        return managerLevel < targetLevel
    }

    /**
     * Require permission or throw exception
     */
    fun requirePermission(userId: UUID, permissionName: String) {
        if (!hasPermission(userId, permissionName)) {
            throw OperationNotPermittedException("Required permission: $permissionName")
        }
    }

    /**
     * Require any of permissions or throw exception
     */
    fun requireAnyPermission(userId: UUID, vararg permissions: String) {
        if (!hasAnyPermission(userId, *permissions)) {
            throw OperationNotPermittedException(
                "Required one of permissions: ${permissions.joinToString(", ")}"
            )
        }
    }

    /**
     * Require all permissions or throw exception
     */
    fun requireAllPermissions(userId: UUID, vararg permissions: String) {
        if (!hasAllPermissions(userId, *permissions)) {
            throw OperationNotPermittedException(
                "Required all permissions: ${permissions.joinToString(", ")}"
            )
        }
    }

    /**
     * Require role or throw exception
     */
    fun requireRole(userId: UUID, roleName: String) {
        if (!hasRole(userId, roleName)) {
            throw OperationNotPermittedException("Required role: $roleName")
        }
    }

    /**
     * Require admin or throw exception
     */
    fun requireAdmin(userId: UUID) {
        if (!isAdmin(userId)) {
            throw OperationNotPermittedException("Admin access required")
        }
    }
}

