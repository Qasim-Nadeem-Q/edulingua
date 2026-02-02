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
class Consumer
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toResponse(): ConsumerResponse {
        return ConsumerResponse(
            id = id!!,
            email = email,
            name = name,
            role = role.code,
            roleDescription = role.description,
            active = active,
            stateCode = stateCode,
            stateName = stateName,
            districtCode = districtCode,
            districtName = districtName,
            schoolCode = schoolCode,
            schoolName = schoolName,
            classCode = classCode,
            className = className,
            rollNumber = rollNumber,
            phoneNumber = phoneNumber,
            lastLogin = lastLogin?.toString(),
            createdAt = createdAt.toString()
        )
    }

    fun hasPermission(permission: Permission): Boolean {
        return role.hasPermission(permission)
    }

    fun updateWith(
        name: String? = null,
        phoneNumber: String? = null,
        active: Boolean? = null,
        parentEmail: String? = null
    ): Consumer {
        return this.copy(
            name = name ?: this.name,
            phoneNumber = phoneNumber ?: this.phoneNumber,
            active = active ?: this.active,
            parentEmail = parentEmail ?: this.parentEmail,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Validates that the consumer has the required hierarchy data based on their role
     */
    fun isValidHierarchy(): Boolean {
        return when (role) {
            Role.STATE -> stateCode != null && stateName != null
            Role.DISTRICT -> stateCode != null && districtCode != null && districtName != null
            Role.SCHOOL -> stateCode != null && districtCode != null && schoolCode != null && schoolName != null
            Role.CLASS -> stateCode != null && districtCode != null && schoolCode != null && classCode != null && className != null
            Role.STUDENT -> stateCode != null && districtCode != null && schoolCode != null && classCode != null && rollNumber != null
            else -> false
        }
    }
}

/**
 * Response model for Consumer
 */
data class ConsumerResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
    val roleDescription: String,
    val active: Boolean,
    val stateCode: String?,
    val stateName: String?,
    val districtCode: String?,
    val districtName: String?,
    val schoolCode: String?,
    val schoolName: String?,
    val classCode: String?,
    val className: String?,
    val rollNumber: String?,
    val phoneNumber: String?,
    val lastLogin: String?,
    val createdAt: String
)

/**
 * Request model for updating consumer
 */
data class ConsumerUpdateRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,
    val phoneNumber: String? = null,
    val active: Boolean? = null,
    val parentEmail: String? = null
)

/**
 * Repository for Consumer entity
 */
@Repository
interface ConsumerRepository : JpaRepository<Consumer, Long> {
    fun findByEmail(email: String): Optional<Consumer>
    fun existsByEmail(email: String): Boolean
    fun findByActiveTrue(): List<Consumer>
    fun findByRole(role: Role): List<Consumer>
    fun findByRoleAndActiveTrue(role: Role): List<Consumer>

    // Hierarchical queries
    fun findByStateCode(stateCode: String): List<Consumer>
    fun findByStateCodeAndRole(stateCode: String, role: Role): List<Consumer>
    fun findByDistrictCode(districtCode: String): List<Consumer>
    fun findBySchoolCode(schoolCode: String): List<Consumer>
    fun findByClassCode(classCode: String): List<Consumer>

    @Query("SELECT c FROM Consumer c WHERE c.stateCode = :stateCode AND c.districtCode = :districtCode")
    fun findByStateAndDistrict(stateCode: String, districtCode: String): List<Consumer>

    @Query("SELECT c FROM Consumer c WHERE c.schoolCode = :schoolCode AND c.classCode = :classCode AND c.role = 'STUDENT'")
    fun findStudentsByClass(schoolCode: String, classCode: String): List<Consumer>
}
