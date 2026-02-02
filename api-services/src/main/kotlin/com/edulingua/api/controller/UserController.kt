package com.edulingua.api.controller

import com.edulingua.api.model.ApiResponse
import com.edulingua.api.security.RequirePermission
import com.edulingua.core.models.*
import com.edulingua.core.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for unified user management operations.
 * Provides endpoints for CRUD operations on all user types with role-based access control.
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    /**
     * Create a new user with roles
     * Required permission: CREATE_USERS
     */
    @PostMapping
    @RequirePermission("CREATE_USERS")
    fun createUser(@Valid @RequestBody request: UserCreateRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val createdUser = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                data = createdUser,
                message = "User created successfully"
            )
        )
    }

    /**
     * Get user by ID
     * Required permission: VIEW_USERS
     */
    @GetMapping("/{id}")
    @RequirePermission("VIEW_USERS")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = user,
                message = "User retrieved successfully"
            )
        )
    }

    /**
     * Get user by email
     * Required permission: VIEW_USERS
     */
    @GetMapping("/email/{email}")
    @RequirePermission("VIEW_USERS")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.getUserByEmail(email)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = user,
                message = "User retrieved successfully"
            )
        )
    }

    /**
     * Get user by username
     * Required permission: VIEW_USERS
     */
    @GetMapping("/username/{username}")
    @RequirePermission("VIEW_USERS")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.getUserByUsername(username)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = user,
                message = "User retrieved successfully"
            )
        )
    }

    /**
     * Get all users
     * Required permission: VIEW_USERS
     */
    @GetMapping
    @RequirePermission("VIEW_USERS")
    fun getAllUsers(@RequestParam(required = false) active: Boolean?): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = if (active == true) {
            userService.getActiveUsers()
        } else {
            userService.getAllUsers()
        }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = users,
                message = "${users.size} users retrieved successfully"
            )
        )
    }

    /**
     * Get users by role
     * Required permission: VIEW_USERS
     */
    @GetMapping("/role/{roleName}")
    @RequirePermission("VIEW_USERS")
    fun getUsersByRole(
        @PathVariable roleName: String,
        @RequestParam(required = false) active: Boolean?
    ): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = if (active == true) {
            userService.getActiveUsersByRole(roleName)
        } else {
            userService.getUsersByRole(roleName)
        }
        return ResponseEntity.ok(
            ApiResponse.success(
                data = users,
                message = "${users.size} users with role '$roleName' retrieved successfully"
            )
        )
    }

    /**
     * Get users by location hierarchy
     * Required permission: VIEW_USERS
     */
    @GetMapping("/state/{stateCode}")
    @RequirePermission("VIEW_USERS")
    fun getUsersByState(@PathVariable stateCode: String): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = userService.getUsersByState(stateCode)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = users,
                message = "${users.size} users in state '$stateCode' retrieved successfully"
            )
        )
    }

    @GetMapping("/district/{districtCode}")
    @RequirePermission("VIEW_USERS")
    fun getUsersByDistrict(@PathVariable districtCode: String): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = userService.getUsersByDistrict(districtCode)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = users,
                message = "${users.size} users in district '$districtCode' retrieved successfully"
            )
        )
    }

    @GetMapping("/school/{schoolCode}")
    @RequirePermission("VIEW_USERS")
    fun getUsersBySchool(@PathVariable schoolCode: String): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = userService.getUsersBySchool(schoolCode)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = users,
                message = "${users.size} users in school '$schoolCode' retrieved successfully"
            )
        )
    }

    @GetMapping("/school/{schoolCode}/class/{classCode}/students")
    @RequirePermission("VIEW_USERS")
    fun getStudentsByClass(
        @PathVariable schoolCode: String,
        @PathVariable classCode: String
    ): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val students = userService.getStudentsByClass(schoolCode, classCode)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = students,
                message = "${students.size} students in class '$classCode' retrieved successfully"
            )
        )
    }

    /**
     * Update user
     * Required permission: UPDATE_USERS
     */
    @PutMapping("/{id}")
    @RequirePermission("UPDATE_USERS")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody updateRequest: UserUpdateRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val updatedUser = userService.updateUser(id, updateRequest)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = updatedUser,
                message = "User updated successfully"
            )
        )
    }

    /**
     * Assign roles to user
     * Required permission: MANAGE_ROLES
     */
    @PutMapping("/{id}/roles")
    @RequirePermission("MANAGE_ROLES")
    fun assignRoles(
        @PathVariable id: UUID,
        @RequestBody roleIds: Set<UUID>
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val updatedUser = userService.assignRoles(id, roleIds)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = updatedUser,
                message = "Roles assigned successfully"
            )
        )
    }

    /**
     * Activate user
     * Required permission: UPDATE_USERS
     */
    @PatchMapping("/{id}/activate")
    @RequirePermission("UPDATE_USERS")
    fun activateUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.activateUser(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = user,
                message = "User activated successfully"
            )
        )
    }

    /**
     * Deactivate user
     * Required permission: UPDATE_USERS
     */
    @PatchMapping("/{id}/deactivate")
    @RequirePermission("UPDATE_USERS")
    fun deactivateUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.deactivateUser(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = user,
                message = "User deactivated successfully"
            )
        )
    }

    /**
     * Delete user
     * Required permission: DELETE_USERS
     */
    @DeleteMapping("/{id}")
    @RequirePermission("DELETE_USERS")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<Void>> {
        userService.deleteUser(id)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = null,
                message = "User deleted successfully"
            )
        )
    }

    /**
     * Get user statistics
     * Required permission: VIEW_USERS
     */
    @GetMapping("/statistics")
    @RequirePermission("VIEW_USERS")
    fun getUserStatistics(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val stats = userService.getUserStatistics()
        return ResponseEntity.ok(
            ApiResponse.success(
                data = stats,
                message = "Statistics retrieved successfully"
            )
        )
    }

    /**
     * Search users by name, email, or username
     * Required permission: VIEW_USERS
     */
    @GetMapping("/search")
    @RequirePermission("VIEW_USERS")
    fun searchUsers(@RequestParam query: String): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = userService.searchUsers(query)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = users,
                message = "${users.size} users found"
            )
        )
    }
}
