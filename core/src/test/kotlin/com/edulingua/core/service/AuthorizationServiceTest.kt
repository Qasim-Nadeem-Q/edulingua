package com.edulingua.core.service

import com.edulingua.core.exception.OperationNotPermittedException
import com.edulingua.core.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.*

/**
 * Integration tests for AuthorizationService.
 * Tests hierarchical permission validation and data scope enforcement.
 */
class AuthorizationServiceTest {

    private lateinit var authorizationService: AuthorizationService
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository = mock()
        authorizationService = AuthorizationService(userRepository)
    }

    // ========================================
    // Permission Tests
    // ========================================

    @Test
    fun `hasPermission should return true when user has permission`() {
        val userId = UUID.randomUUID()
        val permission = Permission(
            id = UUID.randomUUID(),
            name = "VIEW_USERS",
            resource = "users",
            action = PermissionAction.READ,
            description = "View users"
        )
        val role = Role(
            id = UUID.randomUUID(),
            name = "ADMIN",
            description = "Admin role",
            permissions = setOf(permission)
        )
        val user = TestData.createUser(
            id = userId,
            email = "admin@test.com",
            roles = setOf(role)
        )

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = authorizationService.hasPermission(userId, "VIEW_USERS")

        assertTrue(result)
    }

    @Test
    fun `hasPermission should return false when user lacks permission`() {
        val userId = UUID.randomUUID()
        val user = TestData.createUser(id = userId, roles = emptySet())

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = authorizationService.hasPermission(userId, "VIEW_USERS")

        assertFalse(result)
    }

    @Test
    fun `hasAnyPermission should return true when user has at least one permission`() {
        val userId = UUID.randomUUID()
        val permission = Permission(
            id = UUID.randomUUID(),
            name = "VIEW_USERS",
            resource = "users",
            action = PermissionAction.READ,
            description = null
        )
        val role = Role(
            id = UUID.randomUUID(),
            name = "VIEWER",
            description = null,
            permissions = setOf(permission)
        )
        val user = TestData.createUser(id = userId, roles = setOf(role))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = authorizationService.hasAnyPermission(
            userId, "CREATE_USERS", "VIEW_USERS", "DELETE_USERS"
        )

        assertTrue(result)
    }

    @Test
    fun `hasAllPermissions should return false when user lacks one permission`() {
        val userId = UUID.randomUUID()
        val permission = Permission(
            id = UUID.randomUUID(),
            name = "VIEW_USERS",
            resource = "users",
            action = PermissionAction.READ,
            description = null
        )
        val role = Role(
            id = UUID.randomUUID(),
            name = "VIEWER",
            description = null,
            permissions = setOf(permission)
        )
        val user = TestData.createUser(id = userId, roles = setOf(role))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = authorizationService.hasAllPermissions(
            userId, "VIEW_USERS", "CREATE_USERS"
        )

        assertFalse(result)
    }

    // ========================================
    // Role Tests
    // ========================================

    @Test
    fun `hasRole should return true when user has role`() {
        val userId = UUID.randomUUID()
        val role = Role(
            id = UUID.randomUUID(),
            name = "ADMIN",
            description = "Admin",
            permissions = emptySet()
        )
        val user = TestData.createUser(id = userId, roles = setOf(role))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = authorizationService.hasRole(userId, "ADMIN")

        assertTrue(result)
    }

    @Test
    fun `isAdmin should return true for admin users`() {
        val userId = UUID.randomUUID()
        val role = Role(id = UUID.randomUUID(), name = "ADMIN", description = null, permissions = emptySet())
        val user = TestData.createUser(id = userId, roles = setOf(role))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = authorizationService.isAdmin(userId)

        assertTrue(result)
    }

    // ========================================
    // Hierarchical Access Tests
    // ========================================

    @Test
    fun `canManageUser - admin can manage anyone`() {
        val adminId = UUID.randomUUID()
        val targetId = UUID.randomUUID()

        val adminRole = Role(id = UUID.randomUUID(), name = "ADMIN", description = null, permissions = emptySet())
        val admin = TestData.createUser(id = adminId, roles = setOf(adminRole))
        val target = TestData.createUser(id = targetId)

        whenever(userRepository.findById(adminId)).thenReturn(Optional.of(admin))
        whenever(userRepository.findById(targetId)).thenReturn(Optional.of(target))

        val result = authorizationService.canManageUser(adminId, targetId)

        assertTrue(result)
    }

    @Test
    fun `canManageUser - state coordinator can manage users in same state`() {
        val stateCoordId = UUID.randomUUID()
        val targetId = UUID.randomUUID()

        val stateRole = Role(id = UUID.randomUUID(), name = "STATE", description = null, permissions = emptySet())
        val stateCoord = TestData.createUser(
            id = stateCoordId,
            roles = setOf(stateRole),
            stateCode = "MH"
        )
        val target = TestData.createUser(id = targetId, stateCode = "MH")

        whenever(userRepository.findById(stateCoordId)).thenReturn(Optional.of(stateCoord))
        whenever(userRepository.findById(targetId)).thenReturn(Optional.of(target))

        val result = authorizationService.canManageUser(stateCoordId, targetId)

        assertTrue(result)
    }

    @Test
    fun `canManageUser - state coordinator cannot manage users in different state`() {
        val stateCoordId = UUID.randomUUID()
        val targetId = UUID.randomUUID()

        val stateRole = Role(id = UUID.randomUUID(), name = "STATE", description = null, permissions = emptySet())
        val stateCoord = TestData.createUser(
            id = stateCoordId,
            roles = setOf(stateRole),
            stateCode = "MH"
        )
        val target = TestData.createUser(id = targetId, stateCode = "GJ")

        whenever(userRepository.findById(stateCoordId)).thenReturn(Optional.of(stateCoord))
        whenever(userRepository.findById(targetId)).thenReturn(Optional.of(target))

        val result = authorizationService.canManageUser(stateCoordId, targetId)

        assertFalse(result)
    }

    @Test
    fun `canManageUser - district coordinator can manage users in same district`() {
        val districtCoordId = UUID.randomUUID()
        val targetId = UUID.randomUUID()

        val districtRole = Role(id = UUID.randomUUID(), name = "DISTRICT", description = null, permissions = emptySet())
        val districtCoord = TestData.createUser(
            id = districtCoordId,
            roles = setOf(districtRole),
            stateCode = "MH",
            districtCode = "MH01"
        )
        val target = TestData.createUser(
            id = targetId,
            stateCode = "MH",
            districtCode = "MH01"
        )

        whenever(userRepository.findById(districtCoordId)).thenReturn(Optional.of(districtCoord))
        whenever(userRepository.findById(targetId)).thenReturn(Optional.of(target))

        val result = authorizationService.canManageUser(districtCoordId, targetId)

        assertTrue(result)
    }

    @Test
    fun `canManageUser - school coordinator can manage users in same school`() {
        val schoolCoordId = UUID.randomUUID()
        val targetId = UUID.randomUUID()

        val schoolRole = Role(id = UUID.randomUUID(), name = "SCHOOL", description = null, permissions = emptySet())
        val schoolCoord = TestData.createUser(
            id = schoolCoordId,
            roles = setOf(schoolRole),
            schoolCode = "SCH001"
        )
        val target = TestData.createUser(id = targetId, schoolCode = "SCH001")

        whenever(userRepository.findById(schoolCoordId)).thenReturn(Optional.of(schoolCoord))
        whenever(userRepository.findById(targetId)).thenReturn(Optional.of(target))

        val result = authorizationService.canManageUser(schoolCoordId, targetId)

        assertTrue(result)
    }

    @Test
    fun `canManageUser - class teacher can manage students in same class`() {
        val teacherId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        val classRole = Role(id = UUID.randomUUID(), name = "CLASS", description = null, permissions = emptySet())
        val studentRole = Role(id = UUID.randomUUID(), name = "STUDENT", description = null, permissions = emptySet())

        val teacher = TestData.createUser(
            id = teacherId,
            roles = setOf(classRole),
            schoolCode = "SCH001",
            classCode = "10A"
        )
        val student = TestData.createUser(
            id = studentId,
            roles = setOf(studentRole),
            schoolCode = "SCH001",
            classCode = "10A"
        )

        whenever(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher))
        whenever(userRepository.findById(studentId)).thenReturn(Optional.of(student))

        val result = authorizationService.canManageUser(teacherId, studentId)

        assertTrue(result)
    }

    @Test
    fun `canManageUser - student cannot manage other students`() {
        val studentId1 = UUID.randomUUID()
        val studentId2 = UUID.randomUUID()

        val studentRole = Role(id = UUID.randomUUID(), name = "STUDENT", description = null, permissions = emptySet())
        val student1 = TestData.createUser(id = studentId1, roles = setOf(studentRole))
        val student2 = TestData.createUser(id = studentId2, roles = setOf(studentRole))

        whenever(userRepository.findById(studentId1)).thenReturn(Optional.of(student1))
        whenever(userRepository.findById(studentId2)).thenReturn(Optional.of(student2))

        val result = authorizationService.canManageUser(studentId1, studentId2)

        assertFalse(result)
    }

    // ========================================
    // Data Scope Tests
    // ========================================

    @Test
    fun `canAccessState - admin can access any state`() {
        val adminId = UUID.randomUUID()
        val adminRole = Role(id = UUID.randomUUID(), name = "ADMIN", description = null, permissions = emptySet())
        val admin = TestData.createUser(id = adminId, roles = setOf(adminRole))

        whenever(userRepository.findById(adminId)).thenReturn(Optional.of(admin))

        val result = authorizationService.canAccessState(adminId, "MH")

        assertTrue(result)
    }

    @Test
    fun `canAccessState - state coordinator can access their state`() {
        val stateCoordId = UUID.randomUUID()
        val stateRole = Role(id = UUID.randomUUID(), name = "STATE", description = null, permissions = emptySet())
        val stateCoord = TestData.createUser(
            id = stateCoordId,
            roles = setOf(stateRole),
            stateCode = "MH"
        )

        whenever(userRepository.findById(stateCoordId)).thenReturn(Optional.of(stateCoord))

        val result = authorizationService.canAccessState(stateCoordId, "MH")

        assertTrue(result)
    }

    @Test
    fun `canAccessState - state coordinator cannot access other state`() {
        val stateCoordId = UUID.randomUUID()
        val stateRole = Role(id = UUID.randomUUID(), name = "STATE", description = null, permissions = emptySet())
        val stateCoord = TestData.createUser(
            id = stateCoordId,
            roles = setOf(stateRole),
            stateCode = "MH"
        )

        whenever(userRepository.findById(stateCoordId)).thenReturn(Optional.of(stateCoord))

        val result = authorizationService.canAccessState(stateCoordId, "GJ")

        assertFalse(result)
    }

    // ========================================
    // Resource Ownership Tests
    // ========================================

    @Test
    fun `isResourceOwner should return true for same user`() {
        val userId = UUID.randomUUID()

        val result = authorizationService.isResourceOwner(userId, userId)

        assertTrue(result)
    }

    @Test
    fun `isResourceOwner should return false for different users`() {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        val result = authorizationService.isResourceOwner(userId1, userId2)

        assertFalse(result)
    }

    // ========================================
    // Role Level Tests
    // ========================================

    @Test
    fun `getRoleLevel should return correct hierarchy`() {
        val roles = mapOf(
            "ADMIN" to 0,
            "STATE" to 1,
            "DISTRICT" to 2,
            "SCHOOL" to 3,
            "CLASS" to 4,
            "STUDENT" to 5
        )

        roles.forEach { (roleName, expectedLevel) ->
            val userId = UUID.randomUUID()
            val role = Role(id = UUID.randomUUID(), name = roleName, description = null, permissions = emptySet())
            val user = TestData.createUser(id = userId, roles = setOf(role))

            whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

            val level = authorizationService.getRoleLevel(userId)

            assertEquals(expectedLevel, level, "Role $roleName should have level $expectedLevel")
        }
    }

    @Test
    fun `hasHigherPrivilege should work correctly`() {
        val adminId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        val adminRole = Role(id = UUID.randomUUID(), name = "ADMIN", description = null, permissions = emptySet())
        val studentRole = Role(id = UUID.randomUUID(), name = "STUDENT", description = null, permissions = emptySet())

        val admin = TestData.createUser(id = adminId, roles = setOf(adminRole))
        val student = TestData.createUser(id = studentId, roles = setOf(studentRole))

        whenever(userRepository.findById(adminId)).thenReturn(Optional.of(admin))
        whenever(userRepository.findById(studentId)).thenReturn(Optional.of(student))

        val result = authorizationService.hasHigherPrivilege(adminId, studentId)

        assertTrue(result)
    }

    // ========================================
    // Require Methods (Exception Tests)
    // ========================================

    @Test
    fun `requirePermission should throw exception when permission missing`() {
        val userId = UUID.randomUUID()
        val user = TestData.createUser(id = userId, roles = emptySet())

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val exception = assertThrows<OperationNotPermittedException> {
            authorizationService.requirePermission(userId, "VIEW_USERS")
        }

        assertTrue(exception.message!!.contains("VIEW_USERS"))
    }

    @Test
    fun `requireCanManageUser should throw exception when cannot manage`() {
        val studentId1 = UUID.randomUUID()
        val studentId2 = UUID.randomUUID()

        val studentRole = Role(id = UUID.randomUUID(), name = "STUDENT", description = null, permissions = emptySet())
        val student1 = TestData.createUser(id = studentId1, roles = setOf(studentRole))
        val student2 = TestData.createUser(id = studentId2, roles = setOf(studentRole))

        whenever(userRepository.findById(studentId1)).thenReturn(Optional.of(student1))
        whenever(userRepository.findById(studentId2)).thenReturn(Optional.of(student2))

        assertThrows<OperationNotPermittedException> {
            authorizationService.requireCanManageUser(studentId1, studentId2)
        }
    }

    @Test
    fun `requireAdmin should throw exception for non-admin`() {
        val userId = UUID.randomUUID()
        val studentRole = Role(id = UUID.randomUUID(), name = "STUDENT", description = null, permissions = emptySet())
        val user = TestData.createUser(id = userId, roles = setOf(studentRole))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        assertThrows<OperationNotPermittedException> {
            authorizationService.requireAdmin(userId)
        }
    }
}

