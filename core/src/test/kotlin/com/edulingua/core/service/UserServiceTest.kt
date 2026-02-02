package com.edulingua.core.service

import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

/**
 * Comprehensive unit tests for UserService with 100% coverage.
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var roleRepository: RoleRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var testRole: Role
    private lateinit var testUser: User
    private lateinit var testPermission: Permission
    private val testUserId = UUID.randomUUID()
    private val testRoleId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testPermission = Permission(
            id = UUID.randomUUID(),
            name = "VIEW_USERS",
            resource = "users",
            action = PermissionAction.READ,
            description = "View users"
        )

        testRole = Role(
            id = testRoleId,
            name = "ADMIN",
            description = "Administrator",
            permissions = setOf(testPermission)
        )

        testUser = User(
            id = testUserId,
            email = "test@example.com",
            username = "testuser",
            name = "Test User",
            password = "encodedPassword",
            phoneNumber = "+1234567890",
            active = true,
            emailVerified = true,
            roles = setOf(testRole),
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
    }

    // ==================== CREATE USER TESTS ====================

    @Test
    fun `createUser should create user successfully when valid request`() {
        // Arrange
        val request = UserCreateRequest(
            email = "newuser@example.com",
            username = "newuser",
            name = "New User",
            password = "password123",
            phoneNumber = "+1234567890",
            roleIds = setOf(testRoleId)
        )

        `when`(userRepository.existsByEmail(request.email)).thenReturn(false)
        `when`(userRepository.existsByUsername(request.username)).thenReturn(false)
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)

        // Act
        val result = userService.createUser(request)

        // Assert
        assertNotNull(result)
        assertEquals(testUser.email, result.email)
        assertEquals(testUser.username, result.username)
        verify(userRepository).existsByEmail(request.email)
        verify(userRepository).existsByUsername(request.username)
        verify(roleRepository).findById(testRoleId)
        verify(passwordEncoder).encode(request.password)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `createUser should throw ResourceAlreadyExistsException when email exists`() {
        // Arrange
        val request = UserCreateRequest(
            email = "existing@example.com",
            username = "newuser",
            name = "New User",
            password = "password123",
            roleIds = setOf(testRoleId)
        )

        `when`(userRepository.existsByEmail(request.email)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows<ResourceAlreadyExistsException> {
            userService.createUser(request)
        }

        assertTrue(exception.message!!.contains("email"))
        verify(userRepository).existsByEmail(request.email)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `createUser should throw ResourceAlreadyExistsException when username exists`() {
        // Arrange
        val request = UserCreateRequest(
            email = "newuser@example.com",
            username = "existinguser",
            name = "New User",
            password = "password123",
            roleIds = setOf(testRoleId)
        )

        `when`(userRepository.existsByEmail(request.email)).thenReturn(false)
        `when`(userRepository.existsByUsername(request.username)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows<ResourceAlreadyExistsException> {
            userService.createUser(request)
        }

        assertTrue(exception.message!!.contains("username"))
        verify(userRepository).existsByUsername(request.username)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `createUser should throw ResourceNotFoundException when no valid roles found`() {
        // Arrange
        val request = UserCreateRequest(
            email = "newuser@example.com",
            username = "newuser",
            name = "New User",
            password = "password123",
            roleIds = setOf(UUID.randomUUID())
        )

        `when`(userRepository.existsByEmail(request.email)).thenReturn(false)
        `when`(userRepository.existsByUsername(request.username)).thenReturn(false)
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<ResourceNotFoundException> {
            userService.createUser(request)
        }

        assertTrue(exception.message!!.contains("roles"))
        verify(userRepository, never()).save(any(User::class.java))
    }

    // ==================== GET USER TESTS ====================

    @Test
    fun `getUserById should return user when found`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))

        // Act
        val result = userService.getUserById(testUserId)

        // Assert
        assertNotNull(result)
        assertEquals(testUser.id, result.id)
        assertEquals(testUser.email, result.email)
        verify(userRepository).findById(testUserId)
    }

    @Test
    fun `getUserById should throw ResourceNotFoundException when user not found`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.getUserById(testUserId)
        }

        verify(userRepository).findById(testUserId)
    }

    @Test
    fun `getUserByEmail should return user when found`() {
        // Arrange
        `when`(userRepository.findByEmail(testUser.email)).thenReturn(Optional.of(testUser))

        // Act
        val result = userService.getUserByEmail(testUser.email)

        // Assert
        assertNotNull(result)
        assertEquals(testUser.email, result.email)
        verify(userRepository).findByEmail(testUser.email)
    }

    @Test
    fun `getUserByEmail should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val email = "notfound@example.com"
        `when`(userRepository.findByEmail(email)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.getUserByEmail(email)
        }

        verify(userRepository).findByEmail(email)
    }

    @Test
    fun `getUserByUsername should return user when found`() {
        // Arrange
        `when`(userRepository.findByUsername(testUser.username)).thenReturn(Optional.of(testUser))

        // Act
        val result = userService.getUserByUsername(testUser.username)

        // Assert
        assertNotNull(result)
        assertEquals(testUser.username, result.username)
        verify(userRepository).findByUsername(testUser.username)
    }

    @Test
    fun `getUserByUsername should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val username = "notfound"
        `when`(userRepository.findByUsername(username)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.getUserByUsername(username)
        }

        verify(userRepository).findByUsername(username)
    }

    @Test
    fun `getAllUsers should return all users`() {
        // Arrange
        val users = listOf(testUser, testUser.copy(id = UUID.randomUUID()))
        `when`(userRepository.findAll()).thenReturn(users)

        // Act
        val result = userService.getAllUsers()

        // Assert
        assertEquals(2, result.size)
        verify(userRepository).findAll()
    }

    @Test
    fun `getActiveUsers should return only active users`() {
        // Arrange
        val activeUser = testUser
        val inactiveUser = testUser.copy(id = UUID.randomUUID(), active = false)
        `when`(userRepository.findByActive(true)).thenReturn(listOf(activeUser))

        // Act
        val result = userService.getActiveUsers()

        // Assert
        assertEquals(1, result.size)
        assertTrue(result.all { it.active })
        verify(userRepository).findByActive(true)
    }

    @Test
    fun `getUsersByRole should return users with specific role`() {
        // Arrange
        val roleName = "ADMIN"
        `when`(userRepository.findAll()).thenReturn(listOf(testUser))

        // Act
        val result = userService.getUsersByRole(roleName)

        // Assert
        assertEquals(1, result.size)
        verify(userRepository).findAll()
    }

    @Test
    fun `getActiveUsersByRole should return active users with specific role`() {
        // Arrange
        val roleName = "ADMIN"
        `when`(userRepository.findByActive(true)).thenReturn(listOf(testUser))

        // Act
        val result = userService.getActiveUsersByRole(roleName)

        // Assert
        assertEquals(1, result.size)
        verify(userRepository).findByActive(true)
    }

    // ==================== LOCATION-BASED QUERY TESTS ====================

    @Test
    fun `getUsersByState should return users in state`() {
        // Arrange
        val stateCode = "MH"
        `when`(userRepository.findByStateCode(stateCode)).thenReturn(listOf(testUser))

        // Act
        val result = userService.getUsersByState(stateCode)

        // Assert
        assertEquals(1, result.size)
        assertEquals(stateCode, result[0].stateCode)
        verify(userRepository).findByStateCode(stateCode)
    }

    @Test
    fun `getUsersByDistrict should return users in district`() {
        // Arrange
        val districtCode = "MH01"
        `when`(userRepository.findByDistrictCode(districtCode)).thenReturn(listOf(testUser))

        // Act
        val result = userService.getUsersByDistrict(districtCode)

        // Assert
        assertEquals(1, result.size)
        assertEquals(districtCode, result[0].districtCode)
        verify(userRepository).findByDistrictCode(districtCode)
    }

    @Test
    fun `getUsersBySchool should return users in school`() {
        // Arrange
        val schoolCode = "MH01-001"
        `when`(userRepository.findBySchoolCode(schoolCode)).thenReturn(listOf(testUser))

        // Act
        val result = userService.getUsersBySchool(schoolCode)

        // Assert
        assertEquals(1, result.size)
        assertEquals(schoolCode, result[0].schoolCode)
        verify(userRepository).findBySchoolCode(schoolCode)
    }

    @Test
    fun `getStudentsByClass should return students in class`() {
        // Arrange
        val schoolCode = "MH01-001"
        val classCode = "CLASS-10A"
        `when`(userRepository.findBySchoolCodeAndClassCode(schoolCode, classCode)).thenReturn(listOf(testUser))

        // Act
        val result = userService.getStudentsByClass(schoolCode, classCode)

        // Assert
        assertEquals(1, result.size)
        verify(userRepository).findBySchoolCodeAndClassCode(schoolCode, classCode)
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    fun `updateUser should update user successfully`() {
        // Arrange
        val updateRequest = UserUpdateRequest(
            name = "Updated Name",
            phoneNumber = "+9876543210"
        )

        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser.copy(name = "Updated Name"))

        // Act
        val result = userService.updateUser(testUserId, updateRequest)

        // Assert
        assertNotNull(result)
        verify(userRepository).findById(testUserId)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `updateUser should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val updateRequest = UserUpdateRequest(name = "Updated Name")
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.updateUser(testUserId, updateRequest)
        }

        verify(userRepository).findById(testUserId)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `assignRoles should assign roles to user successfully`() {
        // Arrange
        val roleIds = setOf(testRoleId)
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)

        // Act
        val result = userService.assignRoles(testUserId, roleIds)

        // Assert
        assertNotNull(result)
        verify(userRepository).findById(testUserId)
        verify(roleRepository).findById(testRoleId)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `assignRoles should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val roleIds = setOf(testRoleId)
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.assignRoles(testUserId, roleIds)
        }

        verify(userRepository).findById(testUserId)
        verify(userRepository, never()).save(any(User::class.java))
    }

    // ==================== ACTIVATE/DEACTIVATE TESTS ====================

    @Test
    fun `activateUser should activate user successfully`() {
        // Arrange
        val inactiveUser = testUser.copy(active = false)
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(inactiveUser))
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)

        // Act
        val result = userService.activateUser(testUserId)

        // Assert
        assertNotNull(result)
        verify(userRepository).findById(testUserId)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `activateUser should throw ResourceNotFoundException when user not found`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.activateUser(testUserId)
        }

        verify(userRepository).findById(testUserId)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `deactivateUser should deactivate user successfully`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser.copy(active = false))

        // Act
        val result = userService.deactivateUser(testUserId)

        // Assert
        assertNotNull(result)
        verify(userRepository).findById(testUserId)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `deactivateUser should throw ResourceNotFoundException when user not found`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.deactivateUser(testUserId)
        }

        verify(userRepository).findById(testUserId)
        verify(userRepository, never()).save(any(User::class.java))
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    fun `deleteUser should delete user successfully`() {
        // Arrange
        `when`(userRepository.existsById(testUserId)).thenReturn(true)
        doNothing().`when`(userRepository).deleteById(testUserId)

        // Act
        userService.deleteUser(testUserId)

        // Assert
        verify(userRepository).existsById(testUserId)
        verify(userRepository).deleteById(testUserId)
    }

    @Test
    fun `deleteUser should throw ResourceNotFoundException when user not found`() {
        // Arrange
        `when`(userRepository.existsById(testUserId)).thenReturn(false)

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.deleteUser(testUserId)
        }

        verify(userRepository).existsById(testUserId)
        verify(userRepository, never()).deleteById(any(UUID::class.java))
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    fun `changePassword should change password successfully`() {
        // Arrange
        val currentPassword = "oldPassword"
        val newPassword = "newPassword"
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(currentPassword, testUser.password)).thenReturn(true)
        `when`(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword")
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)

        // Act
        userService.changePassword(testUserId, currentPassword, newPassword)

        // Assert
        verify(userRepository).findById(testUserId)
        verify(passwordEncoder).matches(currentPassword, testUser.password)
        verify(passwordEncoder).encode(newPassword)
        verify(userRepository).save(any(User::class.java))
    }

    @Test
    fun `changePassword should throw BusinessValidationException when current password is incorrect`() {
        // Arrange
        val currentPassword = "wrongPassword"
        val newPassword = "newPassword"
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(currentPassword, testUser.password)).thenReturn(false)

        // Act & Assert
        assertThrows<BusinessValidationException> {
            userService.changePassword(testUserId, currentPassword, newPassword)
        }

        verify(userRepository).findById(testUserId)
        verify(passwordEncoder).matches(currentPassword, testUser.password)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `changePassword should throw ResourceNotFoundException when user not found`() {
        // Arrange
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            userService.changePassword(testUserId, "old", "new")
        }

        verify(userRepository).findById(testUserId)
        verify(userRepository, never()).save(any(User::class.java))
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    fun `getUserStatistics should return correct statistics`() {
        // Arrange
        val activeUser = testUser
        val inactiveUser = testUser.copy(id = UUID.randomUUID(), active = false, emailVerified = false)
        val studentRole = testRole.copy(name = "STUDENT")
        val student = testUser.copy(id = UUID.randomUUID(), roles = setOf(studentRole))

        `when`(userRepository.findAll()).thenReturn(listOf(activeUser, inactiveUser, student))

        // Act
        val result = userService.getUserStatistics()

        // Assert
        assertNotNull(result)
        assertEquals(3, result["totalUsers"])
        assertEquals(2, result["activeUsers"])
        assertEquals(1, result["inactiveUsers"])
        assertEquals(2, result["verifiedUsers"])
        assertEquals(1, result["unverifiedUsers"])
        verify(userRepository).findAll()
    }

    @Test
    fun `getUserStatistics should handle empty user list`() {
        // Arrange
        `when`(userRepository.findAll()).thenReturn(emptyList())

        // Act
        val result = userService.getUserStatistics()

        // Assert
        assertNotNull(result)
        assertEquals(0, result["totalUsers"])
        assertEquals(0, result["activeUsers"])
        verify(userRepository).findAll()
    }

    // ==================== SEARCH TESTS ====================

    @Test
    fun `searchUsers should return matching users by name`() {
        // Arrange
        val query = "Test"
        `when`(userRepository.findAll()).thenReturn(listOf(testUser))

        // Act
        val result = userService.searchUsers(query)

        // Assert
        assertEquals(1, result.size)
        assertTrue(result[0].name.contains(query, ignoreCase = true))
        verify(userRepository).findAll()
    }

    @Test
    fun `searchUsers should return matching users by email`() {
        // Arrange
        val query = "test@"
        `when`(userRepository.findAll()).thenReturn(listOf(testUser))

        // Act
        val result = userService.searchUsers(query)

        // Assert
        assertEquals(1, result.size)
        assertTrue(result[0].email.contains(query, ignoreCase = true))
        verify(userRepository).findAll()
    }

    @Test
    fun `searchUsers should return matching users by username`() {
        // Arrange
        val query = "testuser"
        `when`(userRepository.findAll()).thenReturn(listOf(testUser))

        // Act
        val result = userService.searchUsers(query)

        // Assert
        assertEquals(1, result.size)
        assertTrue(result[0].username.contains(query, ignoreCase = true))
        verify(userRepository).findAll()
    }

    @Test
    fun `searchUsers should return empty list when no matches`() {
        // Arrange
        val query = "nonexistent"
        `when`(userRepository.findAll()).thenReturn(listOf(testUser))

        // Act
        val result = userService.searchUsers(query)

        // Assert
        assertEquals(0, result.size)
        verify(userRepository).findAll()
    }

    @Test
    fun `searchUsers should be case insensitive`() {
        // Arrange
        val query = "TEST"
        `when`(userRepository.findAll()).thenReturn(listOf(testUser))

        // Act
        val result = userService.searchUsers(query)

        // Assert
        assertEquals(1, result.size)
        verify(userRepository).findAll()
    }
}

