package com.edulingua.api.controller

import com.edulingua.api.model.ApiResponse
import com.edulingua.api.security.RequirePermission
import com.edulingua.core.models.Permission
import com.edulingua.core.models.PermissionAction
import com.edulingua.core.models.PermissionRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for permission management operations.
 * Provides endpoints for CRUD operations on permissions.
 */
@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val permissionRepository: PermissionRepository
) {

    /**
     * Get all permissions
     * Required permission: VIEW_ROLES (permissions are part of role management)
     */
    @GetMapping
    @RequirePermission("VIEW_ROLES")
    fun getAllPermissions(): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionRepository.findAll()
            .map { PermissionResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = permissions,
                message = "Permissions retrieved successfully"
            )
        )
    }

    /**
     * Get permission by ID
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/{id}")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionById(@PathVariable id: UUID): ResponseEntity<ApiResponse<PermissionResponse>> {
        val permission = permissionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Permission not found with id: $id") }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = PermissionResponse.fromEntity(permission),
                message = "Permission retrieved successfully"
            )
        )
    }

    /**
     * Get permission by name
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/name/{name}")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionByName(@PathVariable name: String): ResponseEntity<ApiResponse<PermissionResponse>> {
        val permission = permissionRepository.findByName(name)
            .orElseThrow { NoSuchElementException("Permission not found with name: $name") }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = PermissionResponse.fromEntity(permission),
                message = "Permission retrieved successfully"
            )
        )
    }

    /**
     * Get permissions by resource
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/resource/{resource}")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionsByResource(@PathVariable resource: String): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionRepository.findByResource(resource)
            .map { PermissionResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = permissions,
                message = "Permissions retrieved successfully"
            )
        )
    }

    /**
     * Get permissions by action
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/action/{action}")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionsByAction(@PathVariable action: PermissionAction): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionRepository.findAll()
            .filter { it.action == action }
            .map { PermissionResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = permissions,
                message = "Permissions retrieved successfully"
            )
        )
    }

    /**
     * Get permissions by resource and action
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/resource/{resource}/action/{action}")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionsByResourceAndAction(
        @PathVariable resource: String,
        @PathVariable action: PermissionAction
    ): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionRepository.findByResourceAndAction(resource, action)
            .map { PermissionResponse.fromEntity(it) }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = permissions,
                message = "Permissions retrieved successfully"
            )
        )
    }

    /**
     * Get permissions grouped by resource
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/grouped/resource")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionsGroupedByResource(): ResponseEntity<ApiResponse<Map<String, List<PermissionResponse>>>> {
        val permissions = permissionRepository.findAll()
        val grouped = permissions.groupBy { it.resource }
            .mapValues { entry -> entry.value.map { PermissionResponse.fromEntity(it) } }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = grouped,
                message = "Permissions grouped by resource retrieved successfully"
            )
        )
    }

    /**
     * Get permissions grouped by action
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/grouped/action")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionsGroupedByAction(): ResponseEntity<ApiResponse<Map<String, List<PermissionResponse>>>> {
        val permissions = permissionRepository.findAll()
        val grouped = permissions.groupBy { it.action.name }
            .mapValues { entry -> entry.value.map { PermissionResponse.fromEntity(it) } }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = grouped,
                message = "Permissions grouped by action retrieved successfully"
            )
        )
    }

    /**
     * Create a new permission
     * Required permission: MANAGE_PERMISSIONS
     */
    @PostMapping
    @RequirePermission("MANAGE_PERMISSIONS")
    fun createPermission(@Valid @RequestBody request: PermissionCreateRequest): ResponseEntity<ApiResponse<PermissionResponse>> {
        // Check if permission already exists
        if (permissionRepository.findByName(request.name).isPresent) {
            throw IllegalArgumentException("Permission already exists with name: ${request.name}")
        }

        val permission = Permission(
            id = null,
            name = request.name,
            resource = request.resource,
            action = request.action,
            description = request.description
        )

        val savedPermission = permissionRepository.save(permission)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                data = PermissionResponse.fromEntity(savedPermission),
                message = "Permission created successfully"
            )
        )
    }

    /**
     * Update permission
     * Required permission: MANAGE_PERMISSIONS
     */
    @PutMapping("/{id}")
    @RequirePermission("MANAGE_PERMISSIONS")
    fun updatePermission(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PermissionUpdateRequest
    ): ResponseEntity<ApiResponse<PermissionResponse>> {
        val permission = permissionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Permission not found with id: $id") }

        // Check if new name already exists (if name is being changed)
        if (request.name != null && request.name != permission.name) {
            if (permissionRepository.findByName(request.name).isPresent) {
                throw IllegalArgumentException("Permission already exists with name: ${request.name}")
            }
        }

        val updatedPermission = permission.copy(
            name = request.name ?: permission.name,
            resource = request.resource ?: permission.resource,
            action = request.action ?: permission.action,
            description = request.description ?: permission.description
        )

        val savedPermission = permissionRepository.save(updatedPermission)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = PermissionResponse.fromEntity(savedPermission),
                message = "Permission updated successfully"
            )
        )
    }

    /**
     * Delete permission
     * Required permission: MANAGE_PERMISSIONS
     */
    @DeleteMapping("/{id}")
    @RequirePermission("MANAGE_PERMISSIONS")
    fun deletePermission(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        if (!permissionRepository.existsById(id)) {
            throw NoSuchElementException("Permission not found with id: $id")
        }

        permissionRepository.deleteById(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = Unit,
                message = "Permission deleted successfully"
            )
        )
    }

    /**
     * Get permission statistics
     * Required permission: VIEW_ROLES
     */
    @GetMapping("/statistics")
    @RequirePermission("VIEW_ROLES")
    fun getPermissionStatistics(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val permissions = permissionRepository.findAll()

        val stats = mapOf(
            "totalPermissions" to permissions.size,
            "byResource" to permissions.groupBy { it.resource }
                .mapValues { it.value.size },
            "byAction" to permissions.groupBy { it.action.name }
                .mapValues { it.value.size },
            "resources" to permissions.map { it.resource }.distinct().sorted()
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
 * Data class for permission creation request
 */
data class PermissionCreateRequest(
    val name: String,
    val resource: String,
    val action: PermissionAction,
    val description: String?
)

/**
 * Data class for permission update request
 */
data class PermissionUpdateRequest(
    val name: String?,
    val resource: String?,
    val action: PermissionAction?,
    val description: String?
)

/**
 * Data class for permission response
 */
data class PermissionResponse(
    val id: UUID,
    val name: String,
    val resource: String,
    val action: PermissionAction,
    val description: String?
) {
    companion object {
        fun fromEntity(permission: Permission): PermissionResponse {
            return PermissionResponse(
                id = permission.id!!,
                name = permission.name,
                resource = permission.resource,
                action = permission.action,
                description = permission.description
            )
        }
    }
}
