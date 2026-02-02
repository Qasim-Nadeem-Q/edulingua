package com.edulingua.core.service

/**
 * DEPRECATED: This service has been merged into UserService.
 * Please use UserService for all user management operations.
 *
 * AdminUser and Consumer models have been unified into a single User model
 * with role-based access control (RBAC).
 *
 * @see UserService
 */
@Deprecated(
    message = "Use UserService instead",
    replaceWith = ReplaceWith("UserService"),
    level = DeprecationLevel.ERROR
)
class AdminUserService
        val user = adminUserRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Admin user with id $id not found") }

        val deactivatedUser = user.updateWith(active = false)
        val savedUser = adminUserRepository.save(deactivatedUser)
        return savedUser.toResponse()
    }
}
