package com.edulingua.core.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * Consumer entity - for test takers with hierarchical roles.
 * Roles: STATE (S), DISTRICT (D), SCHOOL (SC), CLASS (CL), STUDENT (ST)
 */
@Entity
@Table(name = "consumers", indexes = [
    Index(name = "idx_consumer_role", columnList = "role"),
    Index(name = "idx_consumer_state", columnList = "state_code"),
    Index(name = "idx_consumer_district", columnList = "district_code"),
    Index(name = "idx_consumer_school", columnList = "school_code")
])
data class Consumer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    val email: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    val name: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,

    @Column(nullable = false)
    val active: Boolean = true,

    // Hierarchical location identifiers
    @Column(name = "state_code")
    val stateCode: String? = null,

    @Column(name = "state_name")
    val stateName: String? = null,

    @Column(name = "district_code")
    val districtCode: String? = null,

    @Column(name = "district_name")
    val districtName: String? = null,

    @Column(name = "school_code")
    val schoolCode: String? = null,

    @Column(name = "school_name")
    val schoolName: String? = null,

    @Column(name = "class_code")
    val classCode: String? = null,

    @Column(name = "class_name")
    val className: String? = null,

    // Student specific fields
    @Column(name = "roll_number")
    val rollNumber: String? = null,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDateTime? = null,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(name = "parent_email")
    val parentEmail: String? = null,

    @JsonIgnore
    @Column(name = "last_login")
    val lastLogin: LocalDateTime? = null,

    @JsonIgnore
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @JsonIgnore
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
