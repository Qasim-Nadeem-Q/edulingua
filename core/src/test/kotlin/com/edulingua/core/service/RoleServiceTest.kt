package com.edulingua.core.service

import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import com.edulingua.core.service.TestData.createPermission
import com.edulingua.core.service.TestData.createRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
/**
 * Comprehensive unit tests for RoleService with 100% coverage.
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension::class)
class RoleServiceTest {

    @Mock
    private lateinit var roleRepository: RoleRepository

    @Mock
    private lateinit var permissionRepository: PermissionRepository

    @InjectMocks
    private lateinit var roleService: RoleService

    private lateinit var testPermission: Permission
    private lateinit var testPermission2: Permission
    private lateinit var testRole: Role
    private val testRoleId = UUID.randomUUID()
    private val testPermissionId = UUID.randomUUID()
    private val testPermissionId2 = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testPermission = createPermission(
            id = testPermissionId,
            name = "VIEW_USERS",
            resource = "users",
            action = PermissionAction.READ
        )

        testPermission2 = createPermission(
            id = testPermissionId2,
            name = "CREATE_USERS",
            resource = "users",
            action = PermissionAction.WRITE
        )

        testRole = createRole(
            id = testRoleId,
            name = "ADMIN",
            description = "Administrator role",
            permissions = setOf(testPermission, testPermission2)
        )
    }

    // ==================== CREATE ROLE TESTS ====================

    @Test
    fun `createRole should create role successfully with permissions`() {
        // Arrange
        val name = "NEW_ROLE"
        val description = "New role description"
        val permissionIds = setOf(testPermissionId, testPermissionId2)

        `when`(roleRepository.existsByName(name)).thenReturn(false)
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission))
        `when`(permissionRepository.findById(testPermissionId2)).thenReturn(Optional.of(testPermission2))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole)

        // Act
        val result = roleService.createRole(name, description, permissionIds)

        // Assert
        assertNotNull(result)
        assertEquals(testRole.name, result.name)
        verify(roleRepository).existsByName(name)
        verify(permissionRepository).findById(testPermissionId)
        verify(permissionRepository).findById(testPermissionId2)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `createRole should create role without permissions`() {
        // Arrange
        val name = "NEW_ROLE"
        val description = "New role description"
        val permissionIds = emptySet<UUID>()
        val roleWithoutPermissions = testRole.copy(permissions = emptySet())

        `when`(roleRepository.existsByName(name)).thenReturn(false)
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(roleWithoutPermissions)

        // Act
        val result = roleService.createRole(name, description, permissionIds)

        // Assert
        assertNotNull(result)
        verify(roleRepository).existsByName(name)
        verify(roleRepository).save(any(Role::class.java))
        verify(permissionRepository, never()).findById(any(UUID::class.java))
    }

    @Test
    fun `createRole should throw ResourceAlreadyExistsException when role name exists`() {
        // Arrange
        val name = "EXISTING_ROLE"
        val description = "Description"
        val permissionIds = emptySet<UUID>()

        `when`(roleRepository.existsByName(name)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows<ResourceAlreadyExistsException> {
            roleService.createRole(name, description, permissionIds)
        }

        assertTrue(exception.message!!.contains(name))
        verify(roleRepository).existsByName(name)
        verify(roleRepository, never()).save(any(Role::class.java))
    }

    @Test
    fun `createRole should handle invalid permission IDs gracefully`() {
        // Arrange
        val name = "NEW_ROLE"
        val description = "Description"
        val invalidPermissionId = UUID.randomUUID()
        val permissionIds = setOf(testPermissionId, invalidPermissionId)

        `when`(roleRepository.existsByName(name)).thenReturn(false)
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission))
        `when`(permissionRepository.findById(invalidPermissionId)).thenReturn(Optional.empty())
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole.copy(permissions = setOf(testPermission)))

        // Act
        val result = roleService.createRole(name, description, permissionIds)

        // Assert
        assertNotNull(result)
        verify(roleRepository).save(any(Role::class.java))
    }

    // ==================== GET ROLE TESTS ====================

    @Test
    fun `getRoleById should return role when found`() {
        // Arrange
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))

        // Act
        val result = roleService.getRoleById(testRoleId)

        // Assert
        assertNotNull(result)
        assertEquals(testRole.id, result.id)
        assertEquals(testRole.name, result.name)
        verify(roleRepository).findById(testRoleId)
    }

    @Test
    fun `getRoleById should throw ResourceNotFoundException when role not found`() {
        // Arrange
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<ResourceNotFoundException> {
            roleService.getRoleById(testRoleId)
        }

        assertTrue(exception.message!!.contains(testRoleId.toString()))
        verify(roleRepository).findById(testRoleId)
    }

    @Test
    fun `getRoleByName should return role when found`() {
        // Arrange
        `when`(roleRepository.findByName(testRole.name)).thenReturn(Optional.of(testRole))

        // Act
        val result = roleService.getRoleByName(testRole.name)

        // Assert
        assertNotNull(result)
        assertEquals(testRole.name, result.name)
        verify(roleRepository).findByName(testRole.name)
    }

    @Test
    fun `getRoleByName should throw ResourceNotFoundException when role not found`() {
        // Arrange
        val name = "NONEXISTENT"
        `when`(roleRepository.findByName(name)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<ResourceNotFoundException> {
            roleService.getRoleByName(name)
        }

        assertTrue(exception.message!!.contains(name))
        verify(roleRepository).findByName(name)
    }

    @Test
    fun `getAllRoles should return all roles`() {
        // Arrange
        val roles = listOf(testRole, testRole.copy(id = UUID.randomUUID(), name = "USER"))
        `when`(roleRepository.findAll()).thenReturn(roles)

        // Act
        val result = roleService.getAllRoles()

        // Assert
        assertEquals(2, result.size)
        verify(roleRepository).findAll()
    }

    @Test
    fun `getAllRoles should return empty list when no roles exist`() {
        // Arrange
        `when`(roleRepository.findAll()).thenReturn(emptyList())

        // Act
        val result = roleService.getAllRoles()

        // Assert
        assertEquals(0, result.size)
        verify(roleRepository).findAll()
    }

    // ==================== UPDATE ROLE PERMISSIONS TESTS ====================

    @Test
    fun `updateRolePermissions should update permissions successfully`() {
        // Arrange
        val newPermissionIds = setOf(testPermissionId)
        val updatedRole = testRole.copy(permissions = setOf(testPermission))

        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(updatedRole)

        // Act
        val result = roleService.updateRolePermissions(testRoleId, newPermissionIds)

        // Assert
        assertNotNull(result)
        verify(roleRepository).findById(testRoleId)
        verify(permissionRepository).findById(testPermissionId)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `updateRolePermissions should throw ResourceNotFoundException when role not found`() {
        // Arrange
        val permissionIds = setOf(testPermissionId)
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            roleService.updateRolePermissions(testRoleId, permissionIds)
        }

        verify(roleRepository).findById(testRoleId)
        verify(roleRepository, never()).save(any(Role::class.java))
    }

    @Test
    fun `updateRolePermissions should clear permissions when empty set provided`() {
        // Arrange
        val emptyPermissionIds = emptySet<UUID>()
        val roleWithoutPermissions = testRole.copy(permissions = emptySet())

        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(roleWithoutPermissions)

        // Act
        val result = roleService.updateRolePermissions(testRoleId, emptyPermissionIds)

        // Assert
        assertNotNull(result)
        verify(roleRepository).save(any(Role::class.java))
    }

    // ==================== UPDATE ROLE DESCRIPTION TESTS ====================

    @Test
    fun `updateRoleDescription should update description successfully`() {
        // Arrange
        val newDescription = "Updated description"
        val updatedRole = testRole.copy(description = newDescription)

        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(updatedRole)

        // Act
        val result = roleService.updateRoleDescription(testRoleId, newDescription)

        // Assert
        assertNotNull(result)
        assertEquals(newDescription, result.description)
        verify(roleRepository).findById(testRoleId)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `updateRoleDescription should throw ResourceNotFoundException when role not found`() {
        // Arrange
        val newDescription = "Updated description"
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            roleService.updateRoleDescription(testRoleId, newDescription)
        }

        verify(roleRepository).findById(testRoleId)
        verify(roleRepository, never()).save(any(Role::class.java))
    }

    // ==================== DELETE ROLE TESTS ====================

    @Test
    fun `deleteRole should delete role successfully`() {
        // Arrange
        `when`(roleRepository.existsById(testRoleId)).thenReturn(true)
        doNothing().`when`(roleRepository).deleteById(testRoleId)

        // Act
        roleService.deleteRole(testRoleId)

        // Assert
        verify(roleRepository).existsById(testRoleId)
        verify(roleRepository).deleteById(testRoleId)
    }

    @Test
    fun `deleteRole should throw ResourceNotFoundException when role not found`() {
        // Arrange
        `when`(roleRepository.existsById(testRoleId)).thenReturn(false)

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            roleService.deleteRole(testRoleId)
        }

        verify(roleRepository).existsById(testRoleId)
        verify(roleRepository, never()).deleteById(any(UUID::class.java))
    }

    // ==================== PERMISSION MANAGEMENT TESTS ====================

    @Test
    fun `createPermission should create permission successfully`() {
        // Arrange
        val name = "NEW_PERMISSION"
        val resource = "tests"
        val action = PermissionAction.WRITE
        val description = "New permission"

        `when`(permissionRepository.save(any(Role::class.java))).thenReturn(testPermission)

        // Act
        val result = roleService.createPermission(name, resource, action, description)

        // Assert
        assertNotNull(result)
        verify(permissionRepository).save(any(Role::class.java))
    }

    @Test
    fun `getPermissionById should return permission when found`() {
        // Arrange
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission))

        // Act
        val result = roleService.getPermissionById(testPermissionId)

        // Assert
        assertNotNull(result)
        assertEquals(testPermission.id, result.id)
        verify(permissionRepository).findById(testPermissionId)
    }

    @Test
    fun `getPermissionById should throw ResourceNotFoundException when permission not found`() {
        // Arrange
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            roleService.getPermissionById(testPermissionId)
        }

        verify(permissionRepository).findById(testPermissionId)
    }

    @Test
    fun `getPermissionByName should return permission when found`() {
        // Arrange
        `when`(permissionRepository.findByName(testPermission.name)).thenReturn(Optional.of(testPermission))

        // Act
        val result = roleService.getPermissionByName(testPermission.name)

        // Assert
        assertNotNull(result)
        assertEquals(testPermission.name, result.name)
        verify(permissionRepository).findByName(testPermission.name)
    }

    @Test
    fun `getPermissionByName should throw ResourceNotFoundException when permission not found`() {
        // Arrange
        val name = "NONEXISTENT"
        `when`(permissionRepository.findByName(name)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            roleService.getPermissionByName(name)
        }

        verify(permissionRepository).findByName(name)
    }

    @Test
    fun `getAllPermissions should return all permissions`() {
        // Arrange
        val permissions = listOf(testPermission, testPermission2)
        `when`(permissionRepository.findAll()).thenReturn(permissions)

        // Act
        val result = roleService.getAllPermissions()

        // Assert
        assertEquals(2, result.size)
        verify(permissionRepository).findAll()
    }

    @Test
    fun `getPermissionsByResource should return permissions for resource`() {
        // Arrange
        val resource = "users"
        `when`(permissionRepository.findByResource(resource)).thenReturn(listOf(testPermission, testPermission2))

        // Act
        val result = roleService.getPermissionsByResource(resource)

        // Assert
        assertEquals(2, result.size)
        verify(permissionRepository).findByResource(resource)
    }

    @Test
    fun `getPermissionsByResourceAndAction should return filtered permissions`() {
        // Arrange
        val resource = "users"
        val action = PermissionAction.READ
        `when`(permissionRepository.findByResourceAndAction(resource, action))
            .thenReturn(listOf(testPermission))

        // Act
        val result = roleService.getPermissionsByResourceAndAction(resource, action)

        // Assert
        assertEquals(1, result.size)
        assertEquals(action, result[0].action)
        verify(permissionRepository).findByResourceAndAction(resource, action)
    }

    @Test
    fun `deletePermission should delete permission successfully`() {
        // Arrange
        `when`(permissionRepository.existsById(testPermissionId)).thenReturn(true)
        doNothing().`when`(permissionRepository).deleteById(testPermissionId)

        // Act
        roleService.deletePermission(testPermissionId)

        // Assert
        verify(permissionRepository).existsById(testPermissionId)
        verify(permissionRepository).deleteById(testPermissionId)
    }

    @Test
    fun `deletePermission should throw ResourceNotFoundException when permission not found`() {
        // Arrange
        `when`(permissionRepository.existsById(testPermissionId)).thenReturn(false)

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            roleService.deletePermission(testPermissionId)
        }

        verify(permissionRepository).existsById(testPermissionId)
        verify(permissionRepository, never()).deleteById(any(UUID::class.java))
    }

    // ==================== ADDITIONAL HELPER METHOD TESTS ====================

    @Test
    fun `assignPermissions should call updateRolePermissions`() {
        // Arrange
        val permissionIds = setOf(testPermissionId)
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole)

        // Act
        val result = roleService.assignPermissions(testRoleId, permissionIds)

        // Assert
        assertNotNull(result)
        verify(roleRepository).findById(testRoleId)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `addPermission should add single permission to role`() {
        // Arrange
        val roleWithOnePermission = testRole.copy(permissions = setOf(testPermission))
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(roleWithOnePermission))
        `when`(permissionRepository.findById(testPermissionId2)).thenReturn(Optional.of(testPermission2))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole)

        // Act
        val result = roleService.addPermission(testRoleId, testPermissionId2)

        // Assert
        assertNotNull(result)
        verify(roleRepository).findById(testRoleId)
        verify(permissionRepository).findById(testPermissionId2)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `removePermission should remove permission from role`() {
        // Arrange
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole.copy(permissions = setOf(testPermission)))

        // Act
        val result = roleService.removePermission(testRoleId, testPermissionId2)

        // Assert
        assertNotNull(result)
        verify(roleRepository).findById(testRoleId)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `getRolePermissions should return role permissions`() {
        // Arrange
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))

        // Act
        val result = roleService.getRolePermissions(testRoleId)

        // Assert
        assertEquals(2, result.size)
        verify(roleRepository).findById(testRoleId)
    }

    @Test
    fun `hasPermission should return true when role has permission`() {
        // Arrange
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))

        // Act
        val result = roleService.hasPermission(testRoleId, "VIEW_USERS")

        // Assert
        assertTrue(result)
        verify(roleRepository).findById(testRoleId)
    }

    @Test
    fun `hasPermission should return false when role does not have permission`() {
        // Arrange
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))

        // Act
        val result = roleService.hasPermission(testRoleId, "DELETE_USERS")

        // Assert
        assertFalse(result)
        verify(roleRepository).findById(testRoleId)
    }

    @Test
    fun `getUserCountByRole should return zero`() {
        // Arrange & Act
        val result = roleService.getUserCountByRole(testRoleId)

        // Assert
        assertEquals(0L, result)
    }

    @Test
    fun `getRoleStatistics should return statistics`() {
        // Arrange
        val roles = listOf(testRole)
        val permissions = listOf(testPermission, testPermission2)
        `when`(roleRepository.findAll()).thenReturn(roles)
        `when`(permissionRepository.findAll()).thenReturn(permissions)

        // Act
        val result = roleService.getRoleStatistics()

        // Assert
        assertNotNull(result)
        assertEquals(1, result["totalRoles"])
        assertEquals(2, result["totalPermissions"])
        assertTrue(result.containsKey("rolesWithPermissions"))
        assertTrue(result.containsKey("permissionsByResource"))
        verify(roleRepository).findAll()
        verify(permissionRepository).findAll()
    }

    @Test
    fun `createRole with RoleCreateRequest should create role`() {
        // Arrange
        val request = RoleCreateRequest(
            name = "NEW_ROLE",
            description = "Description",
            permissionIds = setOf(testPermissionId)
        )
        `when`(roleRepository.existsByName(request.name)).thenReturn(false)
        `when`(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole)

        // Act
        val result = roleService.createRole(request)

        // Assert
        assertNotNull(result)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `updateRole should update role successfully`() {
        // Arrange
        val request = RoleUpdateRequest(
            name = "UPDATED_NAME",
            description = "Updated description"
        )
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(roleRepository.existsByName(request.name!!)).thenReturn(false)
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole.copy(name = request.name, description = request.description))

        // Act
        val result = roleService.updateRole(testRoleId, request)

        // Assert
        assertNotNull(result)
        verify(roleRepository).findById(testRoleId)
        verify(roleRepository).save(any(Role::class.java))
    }

    @Test
    fun `updateRole should throw exception when new name already exists`() {
        // Arrange
        val request = RoleUpdateRequest(name = "EXISTING_NAME", description = null)
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(roleRepository.existsByName(request.name!!)).thenReturn(true)

        // Act & Assert
        assertThrows<ResourceAlreadyExistsException> {
            roleService.updateRole(testRoleId, request)
        }

        verify(roleRepository).findById(testRoleId)
        verify(roleRepository, never()).save(any(Role::class.java))
    }

    @Test
    fun `updateRole should update only description when name is null`() {
        // Arrange
        val request = RoleUpdateRequest(name = null, description = "New description")
        `when`(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole))
        `when`(roleRepository.save(any(Role::class.java))).thenReturn(testRole)

        // Act
        val result = roleService.updateRole(testRoleId, request)

        // Assert
        assertNotNull(result)
        verify(roleRepository).findById(testRoleId)
        verify(roleRepository).save(any(Role::class.java))
        verify(roleRepository, never()).existsByName(any(String::class.java))
    }
}

