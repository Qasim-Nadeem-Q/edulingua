package com.edulingua.core.service

import com.edulingua.core.models.*
import java.time.LocalDateTime
import java.util.*

/**
 * Test data factory for creating test fixtures.
 * Centralizes test data creation to avoid duplication.
 */
object TestData {

    // ==================== PERMISSION FIXTURES ====================

    fun createPermission(
        id: UUID = UUID.randomUUID(),
        name: String = "VIEW_USERS",
        resource: String = "users",
        action: PermissionAction = PermissionAction.READ,
        description: String? = "View users"
    ) = Permission(
        id = id,
        name = name,
        resource = resource,
        action = action,
        description = description
    )

    fun createViewUsersPermission(id: UUID = UUID.randomUUID()) = createPermission(
        id = id,
        name = "VIEW_USERS",
        resource = "users",
        action = PermissionAction.READ,
        description = "View users"
    )

    fun createCreateUsersPermission(id: UUID = UUID.randomUUID()) = createPermission(
        id = id,
        name = "CREATE_USERS",
        resource = "users",
        action = PermissionAction.WRITE,
        description = "Create users"
    )

    fun createUpdateUsersPermission(id: UUID = UUID.randomUUID()) = createPermission(
        id = id,
        name = "UPDATE_USERS",
        resource = "users",
        action = PermissionAction.WRITE,
        description = "Update users"
    )

    fun createDeleteUsersPermission(id: UUID = UUID.randomUUID()) = createPermission(
        id = id,
        name = "DELETE_USERS",
        resource = "users",
        action = PermissionAction.DELETE,
        description = "Delete users"
    )

    // ==================== ROLE FIXTURES ====================

    fun createRole(
        id: UUID = UUID.randomUUID(),
        name: String = "ADMIN",
        description: String? = "Administrator role",
        permissions: Set<Permission> = emptySet()
    ) = Role(
        id = id,
        name = name,
        description = description,
        permissions = permissions
    )

    fun createAdminRole(id: UUID = UUID.randomUUID()) = createRole(
        id = id,
        name = "ADMIN",
        description = "Administrator with full access",
        permissions = setOf(
            createViewUsersPermission(),
            createCreateUsersPermission(),
            createUpdateUsersPermission(),
            createDeleteUsersPermission()
        )
    )

    fun createTeacherRole(id: UUID = UUID.randomUUID()) = createRole(
        id = id,
        name = "TEACHER",
        description = "Class teacher",
        permissions = setOf(createViewUsersPermission())
    )

    fun createStudentRole(id: UUID = UUID.randomUUID()) = createRole(
        id = id,
        name = "STUDENT",
        description = "Student taking tests",
        permissions = emptySet()
    )

    // ==================== USER FIXTURES ====================

    fun createUser(
        id: UUID = UUID.randomUUID(),
        email: String = "test@example.com",
        username: String = "testuser",
        name: String = "Test User",
        password: String = "encodedPassword",
        phoneNumber: String? = "+1234567890",
        active: Boolean = true,
        emailVerified: Boolean = true,
        roles: Set<Role> = emptySet(),
        stateCode: String? = null,
        stateName: String? = null,
        districtCode: String? = null,
        districtName: String? = null,
        schoolCode: String? = null,
        schoolName: String? = null,
        classCode: String? = null,
        className: String? = null,
        rollNumber: String? = null,
        dateOfBirth: LocalDateTime? = null,
        parentEmail: String? = null
    ) = User(
        id = id,
        email = email,
        username = username,
        name = name,
        password = password,
        phoneNumber = phoneNumber,
        active = active,
        emailVerified = emailVerified,
        roles = roles,
        stateCode = stateCode,
        stateName = stateName,
        districtCode = districtCode,
        districtName = districtName,
        schoolCode = schoolCode,
        schoolName = schoolName,
        classCode = classCode,
        className = className,
        rollNumber = rollNumber,
        dateOfBirth = dateOfBirth,
        parentEmail = parentEmail
    )

    fun createAdminUser(id: UUID = UUID.randomUUID()) = createUser(
        id = id,
        email = "admin@example.com",
        username = "admin",
        name = "Admin User",
        roles = setOf(createAdminRole())
    )

    fun createTeacherUser(id: UUID = UUID.randomUUID()) = createUser(
        id = id,
        email = "teacher@example.com",
        username = "teacher",
        name = "Teacher User",
        roles = setOf(createTeacherRole()),
        schoolCode = "MH01-001",
        schoolName = "Test School"
    )

    fun createStudentUser(id: UUID = UUID.randomUUID()) = createUser(
        id = id,
        email = "student@example.com",
        username = "student",
        name = "Student User",
        roles = setOf(createStudentRole()),
        stateCode = "MH",
        stateName = "Maharashtra",
        districtCode = "MH01",
        districtName = "Mumbai",
        schoolCode = "MH01-001",
        schoolName = "Test School",
        classCode = "CLASS-10A",
        className = "Class 10-A",
        rollNumber = "ROLL-001",
        dateOfBirth = LocalDateTime.now().minusYears(15),
        parentEmail = "parent@example.com"
    )

    // ==================== REQUEST FIXTURES ====================

    fun createUserCreateRequest(
        email: String = "newuser@example.com",
        username: String = "newuser",
        name: String = "New User",
        password: String = "password123",
        phoneNumber: String? = "+1234567890",
        roleIds: Set<UUID> = emptySet(),
        stateCode: String? = null,
        stateName: String? = null,
        districtCode: String? = null,
        districtName: String? = null,
        schoolCode: String? = null,
        schoolName: String? = null,
        classCode: String? = null,
        className: String? = null,
        rollNumber: String? = null,
        dateOfBirth: LocalDateTime? = null,
        parentEmail: String? = null
    ) = UserCreateRequest(
        email = email,
        username = username,
        name = name,
        password = password,
        phoneNumber = phoneNumber,
        roleIds = roleIds,
        stateCode = stateCode,
        stateName = stateName,
        districtCode = districtCode,
        districtName = districtName,
        schoolCode = schoolCode,
        schoolName = schoolName,
        classCode = classCode,
        className = className,
        rollNumber = rollNumber,
        dateOfBirth = dateOfBirth,
        parentEmail = parentEmail
    )

    fun createUserUpdateRequest(
        name: String? = null,
        phoneNumber: String? = null,
        stateCode: String? = null,
        stateName: String? = null,
        districtCode: String? = null,
        districtName: String? = null,
        schoolCode: String? = null,
        schoolName: String? = null,
        classCode: String? = null,
        className: String? = null,
        rollNumber: String? = null,
        dateOfBirth: LocalDateTime? = null,
        parentEmail: String? = null
    ) = UserUpdateRequest(
        name = name,
        phoneNumber = phoneNumber,
        stateCode = stateCode,
        stateName = stateName,
        districtCode = districtCode,
        districtName = districtName,
        schoolCode = schoolCode,
        schoolName = schoolName,
        classCode = classCode,
        className = className,
        rollNumber = rollNumber,
        dateOfBirth = dateOfBirth,
        parentEmail = parentEmail
    )

    fun createLoginRequest(
        emailOrUsername: String = "test@example.com",
        password: String = "password123"
    ) = LoginRequest(
        emailOrUsername = emailOrUsername,
        password = password
    )

    fun createPasswordChangeRequest(
        currentPassword: String = "oldPassword",
        newPassword: String = "newPassword123"
    ) = PasswordChangeRequest(
        currentPassword = currentPassword,
        newPassword = newPassword
    )

    fun createRefreshTokenRequest(
        refreshToken: String = "test.refresh.token"
    ) = RefreshTokenRequest(
        refreshToken = refreshToken
    )

    fun createRoleCreateRequest(
        name: String = "NEW_ROLE",
        description: String? = "New role description",
        permissionIds: Set<UUID> = emptySet()
    ) = RoleCreateRequest(
        name = name,
        description = description,
        permissionIds = permissionIds
    )

    fun createRoleUpdateRequest(
        name: String? = null,
        description: String? = null
    ) = RoleUpdateRequest(
        name = name,
        description = description
    )

    // ==================== AUDIT LOG FIXTURES ====================

    fun createAuditLog(
        id: Long = 1L,
        userId: UUID = UUID.randomUUID(),
        userEmail: String = "test@example.com",
        userRoles: String = "ADMIN",
        action: String = "LOGIN",
        resourceType: String? = null,
        resourceId: String? = null,
        description: String? = "User logged in",
        ipAddress: String? = "192.168.1.1",
        userAgent: String? = "Mozilla/5.0",
        timestamp: LocalDateTime = LocalDateTime.now(),
        success: Boolean = true,
        errorMessage: String? = null
    ) = AuditLog(
        id = id,
        userId = userId,
        userEmail = userEmail,
        userRoles = userRoles,
        action = action,
        resourceType = resourceType,
        resourceId = resourceId,
        description = description,
        ipAddress = ipAddress,
        userAgent = userAgent,
        timestamp = timestamp,
        success = success,
        errorMessage = errorMessage
    )
}
