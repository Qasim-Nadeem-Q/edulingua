package com.edulingua.core.service

import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service layer for role and permission management operations.
 * Handles RBAC (Role-Based Access Control) configuration.
 */
@Service
@Transactional
class RoleService(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository
) {

    /**
     * Creates a new role
     */
    fun createRole(name: String, description: String?, permissionIds: Set<UUID>): Role {
        if (roleRepository.existsByName(name)) {
            throw ResourceAlreadyExistsException("Role with name $name already exists")
        }

        val permissions = permissionIds.mapNotNull { permissionId ->
            permissionRepository.findById(permissionId).orElse(null)
        }.toSet()

        val role = Role(
            name = name,
            description = description,
            permissions = permissions
        )

        return roleRepository.save(role)
    }

    /**
     * Retrieves a role by ID
     */
    @Transactional(readOnly = true)
    fun getRoleById(id: UUID): Role {
        return roleRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Role with id $id not found") }
    }

    /**
     * Retrieves a role by name
     */
    @Transactional(readOnly = true)
    fun getRoleByName(name: String): Role {
        return roleRepository.findByName(name)
            .orElseThrow { ResourceNotFoundException("Role with name $name not found") }
    }

    /**
     * Retrieves all roles
     */
    @Transactional(readOnly = true)
    fun getAllRoles(): List<Role> {
        return roleRepository.findAll()
    }

    /**
     * Updates a role's permissions
     */
    fun updateRolePermissions(roleId: UUID, permissionIds: Set<UUID>): Role {
        val role = roleRepository.findById(roleId)
            .orElseThrow { ResourceNotFoundException("Role with id $roleId not found") }

        val permissions = permissionIds.mapNotNull { permissionId ->
            permissionRepository.findById(permissionId).orElse(null)
        }.toSet()

        val updatedRole = role.copy(permissions = permissions)
        return roleRepository.save(updatedRole)
    }

    /**
     * Updates a role's description
     */
    fun updateRoleDescription(roleId: UUID, description: String): Role {
        val role = roleRepository.findById(roleId)
            .orElseThrow { ResourceNotFoundException("Role with id $roleId not found") }

        val updatedRole = role.copy(description = description)
        return roleRepository.save(updatedRole)
    }

    /**
     * Deletes a role
     */
    fun deleteRole(roleId: UUID) {
        if (!roleRepository.existsById(roleId)) {
            throw ResourceNotFoundException("Role with id $roleId not found")
        }
        roleRepository.deleteById(roleId)
    }

    /**
     * Creates a new permission
     */
    fun createPermission(
        name: String,
        resource: String,
        action: PermissionAction,
        description: String?
    ): Permission {
        val permission = Permission(
            name = name,
            resource = resource,
            action = action,
            description = description
        )

        return permissionRepository.save(permission)
    }

    /**
     * Retrieves a permission by ID
     */
    @Transactional(readOnly = true)
    fun getPermissionById(id: UUID): Permission {
        return permissionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Permission with id $id not found") }
    }

    /**
     * Retrieves a permission by name
     */
    @Transactional(readOnly = true)
    fun getPermissionByName(name: String): Permission {
        return permissionRepository.findByName(name)
            .orElseThrow { ResourceNotFoundException("Permission with name $name not found") }
    }

    /**
     * Retrieves all permissions
     */
    @Transactional(readOnly = true)
    fun getAllPermissions(): List<Permission> {
        return permissionRepository.findAll()
    }

    /**
     * Retrieves permissions by resource
     */
    @Transactional(readOnly = true)
    fun getPermissionsByResource(resource: String): List<Permission> {
        return permissionRepository.findByResource(resource)
    }

    /**
     * Retrieves permissions by resource and action
     */
    @Transactional(readOnly = true)
    fun getPermissionsByResourceAndAction(resource: String, action: PermissionAction): List<Permission> {
        return permissionRepository.findByResourceAndAction(resource, action)
    }

    /**
     * Deletes a permission
     */
    fun deletePermission(permissionId: UUID) {
        if (!permissionRepository.existsById(permissionId)) {
            throw ResourceNotFoundException("Permission with id $permissionId not found")
        }
        permissionRepository.deleteById(permissionId)
    }

    /**
     * Assigns permissions to a role (replaces existing permissions)
     */
    fun assignPermissions(roleId: UUID, permissionIds: Set<UUID>): Role {
        return updateRolePermissions(roleId, permissionIds)
    }

    /**
     * Adds a single permission to a role
     */
    fun addPermission(roleId: UUID, permissionId: UUID): Role {
        val role = getRoleById(roleId)
        val permission = getPermissionById(permissionId)

        val updatedPermissions = role.permissions.toMutableSet()
        updatedPermissions.add(permission)

        val updatedRole = role.copy(permissions = updatedPermissions)
        return roleRepository.save(updatedRole)
    }

    /**
     * Removes a permission from a role
     */
    fun removePermission(roleId: UUID, permissionId: UUID): Role {
        val role = getRoleById(roleId)

        val updatedPermissions = role.permissions.filter { it.id != permissionId }.toSet()
        val updatedRole = role.copy(permissions = updatedPermissions)

        return roleRepository.save(updatedRole)
    }

    /**
     * Gets all permissions for a specific role
     */
    @Transactional(readOnly = true)
    fun getRolePermissions(roleId: UUID): Set<Permission> {
        val role = getRoleById(roleId)
        return role.permissions
    }

    /**
     * Checks if a role has a specific permission
     */
    @Transactional(readOnly = true)
    fun hasPermission(roleId: UUID, permissionName: String): Boolean {
        val role = getRoleById(roleId)
        return role.hasPermission(permissionName)
    }

    /**
     * Gets count of users with a specific role
     */
    @Transactional(readOnly = true)
    fun getUserCountByRole(roleId: UUID): Long {
        // This would need UserRepository - for now return 0
        // Will be implemented when UserRepository is available in this service
        return 0L
    }

    /**
     * Gets role statistics
     */
    @Transactional(readOnly = true)
    fun getRoleStatistics(): Map<String, Any> {
        val roles = getAllRoles()
        val permissions = getAllPermissions()

        return mapOf(
            "totalRoles" to roles.size,
            "totalPermissions" to permissions.size,
            "rolesWithPermissions" to roles.map { role ->
                mapOf(
                    "roleName" to role.name,
                    "permissionCount" to role.permissions.size
                )
            },
            "permissionsByResource" to permissions.groupBy { it.resource }
                .mapValues { it.value.size }
        )
    }

    /**
     * Creates a role with permissions (new signature for controller)
     */
    fun createRole(request: RoleCreateRequest): Role {
        return createRole(request.name, request.description, request.permissionIds)
    }

    /**
     * Updates a role (new signature for controller)
     */
    fun updateRole(roleId: UUID, request: RoleUpdateRequest): Role {
        val role = getRoleById(roleId)

        // Check if name is being changed and if it already exists
        if (request.name != null && request.name != role.name) {
            if (roleRepository.existsByName(request.name)) {
                throw ResourceAlreadyExistsException("Role with name ${request.name} already exists")
            }
        }

        val updatedRole = role.copy(
            name = request.name ?: role.name,
            description = request.description ?: role.description
        )

        return roleRepository.save(updatedRole)
    }
}

/**
 * Data class for role creation request
 */
data class RoleCreateRequest(
    val name: String,
    val description: String?,
    val permissionIds: Set<UUID> = emptySet()
)

/**
 * Data class for role update request
 */
data class RoleUpdateRequest(
    val name: String?,
    val description: String?
)

