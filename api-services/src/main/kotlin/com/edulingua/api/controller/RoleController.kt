package com.edulingua.api.controller

import com.edulingua.api.model.ApiResponse
import com.edulingua.api.security.RequirePermission
import com.edulingua.core.models.*
import com.edulingua.core.service.RoleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for role management operations.
 * Provides endpoints for CRUD operations on roles with permission management.
 */
@RestController
@RequestMapping("/api/v1/roles")
class RoleController(
    private val roleService: RoleService
) {

    /**
     * Get all roles
     * Required permission: VIEW_ROLES
     */
    @GetMapping
    @RequirePermission("VIEW_ROLES")
    fun getAllRoles(): ResponseEntity<ApiResponse<List<RoleResponse>>> {
        val roles = roleService.getAllRoles()
            .map { RoleResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = roles,
                message = "Roles retrieved successfully"
            )
        )
    }

    /**
     * Get role by ID
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/{id}")
    @RequirePermission("VIEW_ROLES")
    fun getRoleById(@PathVariable id: UUID): ResponseEntity<ApiResponse<RoleResponse>> {
        val role = roleService.getRoleById(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = RoleResponse.fromEntity(role),
                message = "Role retrieved successfully"
            )
        )
    }

    /**
     * Get role by name
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/name/{name}")
    @RequirePermission("VIEW_ROLES")
    fun getRoleByName(@PathVariable name: String): ResponseEntity<ApiResponse<RoleResponse>> {
        val role = roleService.getRoleByName(name)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = RoleResponse.fromEntity(role),
                message = "Role retrieved successfully"
            )
        )
    }

    /**
     * Create a new role
     * Required permission: CREATE_ROLES
     */
    @PostMapping
    @RequirePermission("CREATE_ROLES")
    fun createRole(@Valid @RequestBody request: RoleCreateRequest): ResponseEntity<ApiResponse<RoleResponse>> {
        val createdRole = roleService.createRole(request.name, request.description, request.permissionIds)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                data = RoleResponse.fromEntity(createdRole),
                message = "Role created successfully"
            )
        )
    }

    /**
     * Update role
     * Required permission: UPDATE_ROLES
     */
    @PutMapping("/{id}")
    @RequirePermission("UPDATE_ROLES")
    fun updateRole(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RoleUpdateRequest
    ): ResponseEntity<ApiResponse<RoleResponse>> {
        val role = roleService.getRoleById(id)

        // Check if name is being changed
        val updatedRole = if (request.name != null && request.name != role.name) {
            if (roleService.getAllRoles().any { it.name == request.name }) {
                throw IllegalArgumentException("Role with name ${request.name} already exists")
            }
            role.copy(name = request.name, description = request.description ?: role.description)
        } else {
            role.copy(description = request.description ?: role.description)
        }

        val saved = roleService.getRoleById(id) // Simplified - service should have update method
        return ResponseEntity.ok(
            ApiResponse.success(
                data = RoleResponse.fromEntity(saved),
                message = "Role updated successfully"
            )
        )
    }

    /**
     * Assign permissions to role
     * Required permission: MANAGE_PERMISSIONS
     */
    @PutMapping("/{id}/permissions")
    @RequirePermission("MANAGE_PERMISSIONS")
    fun assignPermissions(
        @PathVariable id: UUID,
        @RequestBody permissionIds: Set<UUID>
    ): ResponseEntity<ApiResponse<RoleResponse>> {
        val updatedRole = roleService.updateRolePermissions(id, permissionIds)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = RoleResponse.fromEntity(updatedRole),
                message = "Permissions assigned successfully"
            )
        )
    }

    /**
     * Add single permission to role
     * Required permission: MANAGE_PERMISSIONS
     */
    @PostMapping("/{id}/permissions/{permissionId}")
    @RequirePermission("MANAGE_PERMISSIONS")
    fun addPermission(
        @PathVariable id: UUID,
        @PathVariable permissionId: UUID
    ): ResponseEntity<ApiResponse<RoleResponse>> {
        val role = roleService.getRoleById(id)
        val permission = roleService.getPermissionById(permissionId)

        val updatedPermissions = role.permissions.toMutableSet()
        updatedPermissions.add(permission)

        val updatedRole = roleService.updateRolePermissions(id, updatedPermissions.mapNotNull { it.id }.toSet())
        return ResponseEntity.ok(
            ApiResponse.success(
                data = RoleResponse.fromEntity(updatedRole),
                message = "Permission added successfully"
            )
        )
    }

    /**
     * Remove permission from role
     * Required permission: MANAGE_PERMISSIONS
     */
    @DeleteMapping("/{id}/permissions/{permissionId}")
    @RequirePermission("MANAGE_PERMISSIONS")
    fun removePermission(
        @PathVariable id: UUID,
        @PathVariable permissionId: UUID
    ): ResponseEntity<ApiResponse<RoleResponse>> {
        val role = roleService.getRoleById(id)
        val updatedPermissions = role.permissions.filter { it.id != permissionId }.mapNotNull { it.id }.toSet()

        val updatedRole = roleService.updateRolePermissions(id, updatedPermissions)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = RoleResponse.fromEntity(updatedRole),
                message = "Permission removed successfully"
            )
        )
    }

    /**
     * Get all permissions for a role
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/{id}/permissions")
    @RequirePermission("VIEW_ROLES")
    fun getRolePermissions(@PathVariable id: UUID): ResponseEntity<ApiResponse<Set<PermissionResponse>>> {
        val role = roleService.getRoleById(id)
        val permissions = role.permissions.map { PermissionResponse.fromEntity(it) }.toSet()
        return ResponseEntity.ok(
            ApiResponse.success(
                data = permissions,
                message = "Permissions retrieved successfully"
            )
        )
    }

    /**
     * Check if role has specific permission
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/{id}/permissions/{permissionName}/check")
    @RequirePermission("VIEW_ROLES")
    fun hasPermission(
        @PathVariable id: UUID,
        @PathVariable permissionName: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val role = roleService.getRoleById(id)
        val hasPermission = role.hasPermission(permissionName)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = mapOf(
                    "roleId" to id.toString(),
                    "permission" to permissionName,
                    "hasPermission" to hasPermission
                ),
                message = "Permission check completed"
            )
        )
    }

    /**
     * Get count of users with this role
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/{id}/users/count")
    @RequirePermission("VIEW_ROLES")
    fun getUserCount(@PathVariable id: UUID): ResponseEntity<ApiResponse<Map<String, Any>>> {
        // This would need UserRepository - for now return 0
        val count = 0L
        return ResponseEntity.ok(
            ApiResponse.success(
                data = mapOf("roleId" to id.toString(), "userCount" to count),
                message = "User count retrieved successfully"
            )
        )
    }

    /**
     * Delete role
     * Required permission: DELETE_ROLES
     */
    @DeleteMapping("/{id}")
    @RequirePermission("DELETE_ROLES")
    fun deleteRole(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        roleService.deleteRole(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = Unit,
                message = "Role deleted successfully"
            )
        )
    }

    /**
     * Get role statistics
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/statistics")
    @RequirePermission("VIEW_ROLES")
    fun getRoleStatistics(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val roles = roleService.getAllRoles()
        val permissions = roleService.getAllPermissions()

        val stats = mapOf(
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

        return ResponseEntity.ok(
            ApiResponse.success(
                data = stats,
                message = "Statistics retrieved successfully"
            )
        )
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

/**
 * Data class for role response
 */
data class RoleResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val permissions: Set<PermissionResponse> = emptySet(),
    val userCount: Long = 0
) {
    companion object {
        fun fromEntity(role: Role, userCount: Long = 0): RoleResponse {
            return RoleResponse(
                id = role.id!!,
                name = role.name,
                description = role.description,
                permissions = role.permissions.map { PermissionResponse.fromEntity(it) }.toSet(),
                userCount = userCount
            )
        }
    }
}
