package com.edulingua.core.models

/**
 * DEPRECATED: This model has been unified into the User model.
 * Please use User model for all user types with role-based access control.
 *
 * The system now uses a single User entity with multiple roles and permissions
 * instead of separate AdminUser and Consumer entities.
 *
 * @see User
 * @see Role
 * @see Permission
 */
@Deprecated(
    message = "Use User model instead",
    replaceWith = ReplaceWith("User"),
    level = DeprecationLevel.ERROR
)
class AdminUser
    val role: String,
    val active: Boolean,
    val phoneNumber: String?,
    val lastLogin: String?,
    val createdAt: String
)

/**
 * Request model for updating admin user
 */
data class AdminUserUpdateRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    val phoneNumber: String? = null,
    val active: Boolean? = null
)

/**
 * Repository for AdminUser entity
 */
@Repository
interface AdminUserRepository : JpaRepository<AdminUser, Long> {
    fun findByEmail(email: String): Optional<AdminUser>
    fun existsByEmail(email: String): Boolean
    fun findByActiveTrue(): List<AdminUser>
}
