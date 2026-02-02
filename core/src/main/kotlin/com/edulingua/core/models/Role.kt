package com.edulingua.core.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Role entity representing user roles with associated permissions.
 * Supports hierarchical role-based access control (RBAC).
 */
@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val name: String, // ADMIN, STATE, DISTRICT, SCHOOL, CLASS, STUDENT, etc.

    val description: String?,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: Set<Permission> = emptySet()
) {
    /**
     * Check if this role has a specific permission
     */
    fun hasPermission(permissionName: String): Boolean {
        return permissions.any { it.name == permissionName }
    }

    /**
     * Check if this role has permission for a specific action on a resource
     */
    fun hasPermission(resource: String, action: PermissionAction): Boolean {
        return permissions.any { it.resource == resource && it.action == action }
    }
}

/**
 * Permission entity representing granular access controls.
 * Associates actions with specific resources.
 */
@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val name: String, // VIEW_TESTS, CREATE_TESTS, SCORE_TESTS, etc.

    val resource: String, // tests, users, scores, reports

    @Enumerated(EnumType.STRING)
    val action: PermissionAction, // READ, WRITE, DELETE, EXECUTE

    val description: String?
)

/**
 * Enum defining possible actions for permissions
 */
enum class PermissionAction {
    READ,
    WRITE,
    DELETE,
    EXECUTE
}

/**
 * Repository for Role entity
 */
@Repository
interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByName(name: String): Optional<Role>
    fun existsByName(name: String): Boolean
}

/**
 * Repository for Permission entity
 */
@Repository
interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByName(name: String): Optional<Permission>
    fun findByResource(resource: String): List<Permission>
    fun findByResourceAndAction(resource: String, action: PermissionAction): List<Permission>
}
