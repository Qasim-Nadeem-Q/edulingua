package com.edulingua.core.service

import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.User
import com.edulingua.core.models.UserRepository
import com.edulingua.core.models.UserResponse
import com.edulingua.core.models.UserUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service layer for user management operations.
 * Handles business logic for user CRUD operations and validation.
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {

    /**
     * Creates a new user account.
     *
     * @param user User entity with creation information
     * @return UserResponse containing the created user information
     * @throws ResourceAlreadyExistsException if a user with the given email already exists
     */
    fun createUser(user: User): UserResponse {
        if (userRepository.existsByEmail(user.email)) {
            throw ResourceAlreadyExistsException("User with email ${user.email} already exists")
        }

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id The unique identifier of the user
     * @return UserResponse containing the user information
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }
        return user.toResponse()
    }

    /**
     * Retrieves all users in the system.
     *
     * @return List of UserResponse containing all user information
     */
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    /**
     * Updates an existing user account.
     *
     * @param id The unique identifier of the user to update
     * @param updateRequest Request containing updated user information
     * @return UserResponse containing the updated user information
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    fun updateUser(id: Long, updateRequest: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        val updatedUser = user.updateWith(name = updateRequest.name)
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toResponse()
    }

    /**
     * Deletes a user account permanently.
     *
     * @param id The unique identifier of the user to delete
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User with id $id not found")
        }
        userRepository.deleteById(id)
    }
}
