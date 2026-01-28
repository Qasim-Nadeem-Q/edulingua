package com.edulingua.core.service

import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.AdminUser
import com.edulingua.core.models.AdminUserRepository
import com.edulingua.core.models.AdminUserResponse
import com.edulingua.core.models.AdminUserUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service layer for admin user management operations.
 */
@Service
@Transactional
class AdminUserService(
    private val adminUserRepository: AdminUserRepository
) {

    /**
     * Creates a new admin user account.
     */
    fun createAdminUser(adminUser: AdminUser): AdminUserResponse {
        if (adminUserRepository.existsByEmail(adminUser.email)) {
            throw ResourceAlreadyExistsException("Admin user with email ${adminUser.email} already exists")
        }

        // TODO: Hash password before saving
        val savedUser = adminUserRepository.save(adminUser)
        return savedUser.toResponse()
    }

    /**
     * Retrieves an admin user by ID.
     */
    @Transactional(readOnly = true)
    fun getAdminUserById(id: Long): AdminUserResponse {
        val user = adminUserRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Admin user with id $id not found") }
        return user.toResponse()
    }

    /**
     * Retrieves an admin user by email.
     */
    @Transactional(readOnly = true)
    fun getAdminUserByEmail(email: String): AdminUserResponse {
        val user = adminUserRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("Admin user with email $email not found") }
        return user.toResponse()
    }

    /**
     * Retrieves all admin users.
     */
    @Transactional(readOnly = true)
    fun getAllAdminUsers(): List<AdminUserResponse> {
        return adminUserRepository.findAll().map { it.toResponse() }
    }

    /**
     * Retrieves all active admin users.
     */
    @Transactional(readOnly = true)
    fun getActiveAdminUsers(): List<AdminUserResponse> {
        return adminUserRepository.findByActiveTrue().map { it.toResponse() }
    }

    /**
     * Updates an existing admin user.
     */
    fun updateAdminUser(id: Long, updateRequest: AdminUserUpdateRequest): AdminUserResponse {
        val user = adminUserRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Admin user with id $id not found") }

        val updatedUser = user.updateWith(
            name = updateRequest.name,
            phoneNumber = updateRequest.phoneNumber,
            active = updateRequest.active
        )

        val savedUser = adminUserRepository.save(updatedUser)
        return savedUser.toResponse()
    }

    /**
     * Deletes an admin user.
     */
    fun deleteAdminUser(id: Long) {
        if (!adminUserRepository.existsById(id)) {
            throw ResourceNotFoundException("Admin user with id $id not found")
        }
        adminUserRepository.deleteById(id)
    }

    /**
     * Deactivates an admin user instead of deleting.
     */
    fun deactivateAdminUser(id: Long): AdminUserResponse {
        val user = adminUserRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Admin user with id $id not found") }

        val deactivatedUser = user.updateWith(active = false)
        val savedUser = adminUserRepository.save(deactivatedUser)
        return savedUser.toResponse()
    }
}
