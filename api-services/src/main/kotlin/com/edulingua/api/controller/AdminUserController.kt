package com.edulingua.api.controller

import com.edulingua.core.models.AdminUser
import com.edulingua.core.models.AdminUserResponse
import com.edulingua.core.models.AdminUserUpdateRequest
import com.edulingua.core.service.AdminUserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for admin user management.
 * Handles CRUD operations for system administrators.
 */
@RestController
@RequestMapping("/api/v1/admin-users")
class AdminUserController(
    private val adminUserService: AdminUserService
) {

    @PostMapping
    fun createAdminUser(@Valid @RequestBody adminUser: AdminUser): ResponseEntity<AdminUserResponse> {
        val created = adminUserService.createAdminUser(adminUser)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{id}")
    fun getAdminUserById(@PathVariable id: Long): ResponseEntity<AdminUserResponse> {
        val adminUser = adminUserService.getAdminUserById(id)
        return ResponseEntity.ok(adminUser)
    }

    @GetMapping("/email/{email}")
    fun getAdminUserByEmail(@PathVariable email: String): ResponseEntity<AdminUserResponse> {
        val adminUser = adminUserService.getAdminUserByEmail(email)
        return ResponseEntity.ok(adminUser)
    }

    @GetMapping
    fun getAllAdminUsers(@RequestParam(required = false, defaultValue = "false") activeOnly: Boolean): ResponseEntity<List<AdminUserResponse>> {
        val adminUsers = if (activeOnly) {
            adminUserService.getActiveAdminUsers()
        } else {
            adminUserService.getAllAdminUsers()
        }
        return ResponseEntity.ok(adminUsers)
    }

    @PutMapping("/{id}")
    fun updateAdminUser(
        @PathVariable id: Long,
        @Valid @RequestBody updateRequest: AdminUserUpdateRequest
    ): ResponseEntity<AdminUserResponse> {
        val updated = adminUserService.updateAdminUser(id, updateRequest)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteAdminUser(@PathVariable id: Long): ResponseEntity<Void> {
        adminUserService.deleteAdminUser(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/deactivate")
    fun deactivateAdminUser(@PathVariable id: Long): ResponseEntity<AdminUserResponse> {
        val deactivated = adminUserService.deactivateAdminUser(id)
        return ResponseEntity.ok(deactivated)
    }
}
