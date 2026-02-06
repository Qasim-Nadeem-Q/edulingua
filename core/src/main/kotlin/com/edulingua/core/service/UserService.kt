package com.edulingua.core.service

import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.models.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Unified service layer for user management operations.
 * Handles all user types (Admin, State, District, School, Class, Student)
 * with role-based access control.
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorizationService: AuthorizationService
) {

    /**
     * Creates a new user account with specified roles.
     *
     * @param request UserCreateRequest with creation information
     * @return UserResponse containing the created user information
     * @throws ResourceAlreadyExistsException if a user with the given email or username already exists
     * @throws ResourceNotFoundException if any specified role doesn't exist
     */
    fun createUser(request: UserCreateRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ResourceAlreadyExistsException("User with email ${request.email} already exists")
        }

        if (userRepository.existsByUsername(request.username)) {
            throw ResourceAlreadyExistsException("User with username ${request.username} already exists")
        }

        // Fetch roles from database
        val roles = request.roleIds.mapNotNull { roleId ->
            roleRepository.findById(roleId).orElse(null)
        }.toSet()

        if (roles.isEmpty()) {
            throw ResourceNotFoundException("No valid roles found for the provided role IDs")
        }

        val user = User(
            email = request.email,
            username = request.username,
            name = request.name,
            password = passwordEncoder.encode(request.password),
            phoneNumber = request.phoneNumber,
            roles = roles,
            stateCode = request.stateCode,
            stateName = request.stateName,
            districtCode = request.districtCode,
            districtName = request.districtName,
            schoolCode = request.schoolCode,
            schoolName = request.schoolName,
            classCode = request.classCode,
            className = request.className,
            rollNumber = request.rollNumber,
            dateOfBirth = request.dateOfBirth,
            parentEmail = request.parentEmail
        )

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
    fun getUserById(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }
        return user.toResponse()
    }

    /**
     * Retrieves a user by email.
     *
     * @param email The email of the user
     * @return UserResponse containing the user information
     * @throws ResourceNotFoundException if no user exists with the given email
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User with email $email not found") }
        return user.toResponse()
    }

    /**
     * Retrieves a user by username.
     *
     * @param username The username of the user
     * @return UserResponse containing the user information
     * @throws ResourceNotFoundException if no user exists with the given username
     */
    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): UserResponse {
        val user = userRepository.findByUsername(username)
            .orElseThrow { ResourceNotFoundException("User with username $username not found") }
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
     * Retrieves all active users.
     *
     * @return List of UserResponse containing active user information
     */
    @Transactional(readOnly = true)
    fun getActiveUsers(): List<UserResponse> {
        return userRepository.findByActiveTrue().map { it.toResponse() }
    }

    /**
     * Retrieves users by role name.
     *
     * @param roleName The name of the role
     * @return List of UserResponse containing users with specified role
     */
    @Transactional(readOnly = true)
    fun getUsersByRole(roleName: String): List<UserResponse> {
        return userRepository.findByRoleName(roleName).map { it.toResponse() }
    }

    /**
     * Retrieves active users by role name.
     *
     * @param roleName The name of the role
     * @return List of UserResponse containing active users with specified role
     */
    @Transactional(readOnly = true)
    fun getActiveUsersByRole(roleName: String): List<UserResponse> {
        return userRepository.findByRoleNameAndActiveTrue(roleName).map { it.toResponse() }
    }

    /**
     * Retrieves users by state code.
     *
     * @param stateCode The state code
     * @return List of UserResponse containing users in the specified state
     */
    @Transactional(readOnly = true)
    fun getUsersByState(stateCode: String): List<UserResponse> {
        return userRepository.findByStateCode(stateCode).map { it.toResponse() }
    }

    /**
     * Retrieves users by district code.
     *
     * @param districtCode The district code
     * @return List of UserResponse containing users in the specified district
     */
    @Transactional(readOnly = true)
    fun getUsersByDistrict(districtCode: String): List<UserResponse> {
        return userRepository.findByDistrictCode(districtCode).map { it.toResponse() }
    }

    /**
     * Retrieves users by school code.
     *
     * @param schoolCode The school code
     * @return List of UserResponse containing users in the specified school
     */
    @Transactional(readOnly = true)
    fun getUsersBySchool(schoolCode: String): List<UserResponse> {
        return userRepository.findBySchoolCode(schoolCode).map { it.toResponse() }
    }

    /**
     * Retrieves students by class.
     *
     * @param schoolCode The school code
     * @param classCode The class code
     * @return List of UserResponse containing students in the specified class
     */
    @Transactional(readOnly = true)
    fun getStudentsByClass(schoolCode: String, classCode: String): List<UserResponse> {
        return userRepository.findStudentsByClass(schoolCode, classCode).map { it.toResponse() }
    }

    /**
     * Updates an existing user account.
     *
     * @param id The unique identifier of the user to update
     * @param updateRequest Request containing updated user information
     * @return UserResponse containing the updated user information
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    fun updateUser(id: UUID, updateRequest: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        val updatedUser = user.updateWith(
            name = updateRequest.name,
            phoneNumber = updateRequest.phoneNumber,
            active = updateRequest.active,
            parentEmail = updateRequest.parentEmail
        )

        val savedUser = userRepository.save(updatedUser)
        return savedUser.toResponse()
    }

    /**
     * Assigns roles to a user.
     *
     * @param userId The unique identifier of the user
     * @param roleIds Set of role IDs to assign
     * @return UserResponse containing the updated user information
     * @throws ResourceNotFoundException if user or roles don't exist
     */
    fun assignRoles(userId: UUID, roleIds: Set<UUID>): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User with id $userId not found") }

        val roles = roleIds.mapNotNull { roleId ->
            roleRepository.findById(roleId).orElse(null)
        }.toSet()

        if (roles.isEmpty()) {
            throw ResourceNotFoundException("No valid roles found for the provided role IDs")
        }

        val updatedUser = user.copy(roles = roles)
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toResponse()
    }

    /**
     * Activates a user account.
     *
     * @param id The unique identifier of the user
     * @return UserResponse containing the updated user information
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    fun activateUser(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        val activatedUser = user.updateWith(active = true)
        val savedUser = userRepository.save(activatedUser)
        return savedUser.toResponse()
    }

    /**
     * Deactivates a user account.
     *
     * @param id The unique identifier of the user
     * @return UserResponse containing the updated user information
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    fun deactivateUser(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        val deactivatedUser = user.updateWith(active = false)
        val savedUser = userRepository.save(deactivatedUser)
        return savedUser.toResponse()
    }

    /**
     * Deletes a user account permanently.
     *
     * @param id The unique identifier of the user to delete
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User with id $id not found")
        }
        userRepository.deleteById(id)
    }

    /**
     * Changes user password.
     *
     * @param userId The user ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @throws BusinessValidationException if current password doesn't match
     */
    fun changePassword(userId: UUID, currentPassword: String, newPassword: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User with id $userId not found") }

        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw BusinessValidationException("Current password is incorrect")
        }

        val updatedUser = user.copy(
            password = passwordEncoder.encode(newPassword),
            updatedAt = java.time.LocalDateTime.now()
        )

        userRepository.save(updatedUser)
    }

    /**
     * Gets user statistics including counts by role, active/inactive, location, etc.
     *
     * @return Map containing various user statistics
     */
    @Transactional(readOnly = true)
    fun getUserStatistics(): Map<String, Any> {
        val allUsers = userRepository.findAll()

        return mapOf(
            "totalUsers" to allUsers.size,
            "activeUsers" to allUsers.count { it.active },
            "inactiveUsers" to allUsers.count { !it.active },
            "verifiedUsers" to allUsers.count { it.emailVerified },
            "unverifiedUsers" to allUsers.count { !it.emailVerified },
            "byRole" to allUsers.flatMap { user -> user.roles.map { role -> role.name } }
                .groupingBy { it }
                .eachCount(),
            "byState" to allUsers.mapNotNull { it.stateName }
                .groupingBy { it }
                .eachCount(),
            "studentsCount" to allUsers.count { user ->
                user.roles.any { it.name == "STUDENT" }
            },
            "teachersCount" to allUsers.count { user ->
                user.roles.any { it.name == "CLASS" }
            }
        )
    }

    /**
     * Searches users by name, email, or username.
     *
     * @param query Search query string
     * @return List of matching users
     */
    @Transactional(readOnly = true)
    fun searchUsers(query: String): List<UserResponse> {
        val normalizedQuery = query.lowercase().trim()

        return userRepository.findAll()
            .filter { user ->
                user.name.lowercase().contains(normalizedQuery) ||
                user.email.lowercase().contains(normalizedQuery) ||
                user.username.lowercase().contains(normalizedQuery)
            }
            .map { it.toResponse() }
    }

    // ========================================
    // Data Scope Enforcement Methods
    // ========================================

    /**
     * Creates a user with hierarchical validation.
     * Ensures the creator can only create users within their scope.
     */
    fun createUserWithValidation(currentUserId: UUID, request: UserCreateRequest): UserResponse {
        val creator = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("Creator user not found")
        }

        // Admins can create users anywhere
        if (!creator.hasRole("ADMIN")) {
            // Validate scope based on creator's role
            when {
                creator.hasRole("STATE") -> {
                    if (request.stateCode != creator.stateCode) {
                        throw BusinessValidationException(
                            "State coordinators can only create users in their state"
                        )
                    }
                }
                creator.hasRole("DISTRICT") -> {
                    if (request.stateCode != creator.stateCode ||
                        request.districtCode != creator.districtCode) {
                        throw BusinessValidationException(
                            "District coordinators can only create users in their district"
                        )
                    }
                }
                creator.hasRole("SCHOOL") -> {
                    if (request.schoolCode != creator.schoolCode) {
                        throw BusinessValidationException(
                            "School coordinators can only create users in their school"
                        )
                    }
                }
                creator.hasRole("CLASS") -> {
                    if (request.schoolCode != creator.schoolCode ||
                        request.classCode != creator.classCode) {
                        throw BusinessValidationException(
                            "Class teachers can only create students in their class"
                        )
                    }
                    // Class teachers can only create students
                    val requestedRoles = roleRepository.findAllById(request.roleIds)
                    if (requestedRoles.any { it.name != "STUDENT" }) {
                        throw BusinessValidationException(
                            "Class teachers can only create student users"
                        )
                    }
                }
                else -> {
                    throw BusinessValidationException(
                        "You don't have permission to create users"
                    )
                }
            }
        }

        return createUser(request)
    }

    /**
     * Gets users filtered by current user's data scope.
     */
    @Transactional(readOnly = true)
    fun getAllUsersWithScope(currentUserId: UUID, activeOnly: Boolean = false): List<UserResponse> {
        val currentUser = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("Current user not found")
        }

        val allUsers = if (activeOnly) getActiveUsers() else getAllUsers()

        // Admin sees all users
        if (currentUser.hasRole("ADMIN")) {
            return allUsers
        }

        // Filter by hierarchy
        return allUsers.filter { userResponse ->
            val targetUser = userRepository.findById(userResponse.id).orElse(null)
            targetUser != null && authorizationService.canManageUser(currentUserId, targetUser.id!!)
        }
    }

    /**
     * Gets users by role with scope validation.
     */
    @Transactional(readOnly = true)
    fun getUsersByRoleWithScope(
        currentUserId: UUID,
        roleName: String,
        activeOnly: Boolean = false
    ): List<UserResponse> {
        val baseUsers = if (activeOnly) {
            getActiveUsersByRole(roleName)
        } else {
            getUsersByRole(roleName)
        }

        val currentUser = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("Current user not found")
        }

        // Admin sees all
        if (currentUser.hasRole("ADMIN")) {
            return baseUsers
        }

        // Filter by scope
        return baseUsers.filter { userResponse ->
            val targetUser = userRepository.findById(userResponse.id).orElse(null)
            targetUser != null && authorizationService.canManageUser(currentUserId, targetUser.id!!)
        }
    }

    /**
     * Validates that current user can perform action on target user.
     */
    private fun validateHierarchicalAccess(currentUserId: UUID, targetUserId: UUID) {
        authorizationService.requireCanManageUser(currentUserId, targetUserId)
    }

    /**
     * Update user with hierarchical validation.
     */
    fun updateUserWithValidation(
        currentUserId: UUID,
        targetUserId: UUID,
        updateRequest: UserUpdateRequest
    ): UserResponse {
        validateHierarchicalAccess(currentUserId, targetUserId)
        return updateUser(targetUserId, updateRequest)
    }

    /**
     * Delete user with hierarchical validation.
     */
    fun deleteUserWithValidation(currentUserId: UUID, targetUserId: UUID) {
        validateHierarchicalAccess(currentUserId, targetUserId)
        deleteUser(targetUserId)
    }

    /**
     * Assign roles with hierarchical validation.
     */
    fun assignRolesWithValidation(
        currentUserId: UUID,
        targetUserId: UUID,
        roleIds: Set<UUID>
    ): UserResponse {
        validateHierarchicalAccess(currentUserId, targetUserId)
        return assignRoles(targetUserId, roleIds)
    }
}
