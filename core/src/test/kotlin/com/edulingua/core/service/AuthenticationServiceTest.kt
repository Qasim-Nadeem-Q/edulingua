package com.edulingua.core.service

import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import com.edulingua.core.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import java.util.*

/**
 * Comprehensive unit tests for AuthenticationService with 100% coverage.
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    private lateinit var auditService: AuditService

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var httpServletRequest: HttpServletRequest

    @InjectMocks
    private lateinit var authenticationService: AuthenticationService

    private lateinit var testUser: User
    private lateinit var testRole: Role
    private lateinit var testPermission: Permission
    private val testUserId = UUID.randomUUID()
    private val testAccessToken = "test.access.token"
    private val testRefreshToken = "test.refresh.token"

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
            id = UUID.randomUUID(),
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
            active = true,
            emailVerified = true,
            roles = setOf(testRole)
        )

        // Set jwt expiration value using reflection
        ReflectionTestUtils.setField(authenticationService, "jwtExpirationMs", 86400000L)
    }

    // ==================== AUTHENTICATE TESTS ====================

    @Test
    fun `authenticate should return tokens when credentials are valid (by email)`() {
        // Arrange
        val loginRequest = LoginRequest(
            emailOrUsername = "test@example.com",
            password = "password123"
        )

        `when`(userRepository.findByEmail(loginRequest.emailOrUsername)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(loginRequest.password, testUser.password)).thenReturn(true)
        `when`(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn(testAccessToken)
        `when`(jwtTokenProvider.generateRefreshToken(any(), any())).thenReturn(testRefreshToken)
        `when`(userRepository.save(any())).thenReturn(testUser)
        doNothing().`when`(auditService).logLogin(any(), any(), any(), any(), any())

        // Act
        val result = authenticationService.authenticate(loginRequest, httpServletRequest)

        // Assert
        assertNotNull(result)
        assertEquals(testAccessToken, result.accessToken)
        assertEquals(testRefreshToken, result.refreshToken)
        assertEquals(86400L, result.expiresIn)
        assertEquals(testUser.email, result.user.email)
        verify(userRepository).findByEmail(loginRequest.emailOrUsername)
        verify(passwordEncoder).matches(loginRequest.password, testUser.password)
        verify(jwtTokenProvider).generateAccessToken(any(), any(), any(), any(), any())
        verify(jwtTokenProvider).generateRefreshToken(any(), any())
        verify(auditService, times(1)).logLogin(any(), any(), any(), any(), eq(true))
        verify(userRepository).save(any())
    }

    @Test
    fun `authenticate should return tokens when credentials are valid (by username)`() {
        // Arrange
        val loginRequest = LoginRequest(
            emailOrUsername = "testuser",
            password = "password123"
        )

        `when`(userRepository.findByEmail(loginRequest.emailOrUsername)).thenReturn(Optional.empty())
        `when`(userRepository.findByUsername(loginRequest.emailOrUsername)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(loginRequest.password, testUser.password)).thenReturn(true)
        `when`(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn(testAccessToken)
        `when`(jwtTokenProvider.generateRefreshToken(any(), any())).thenReturn(testRefreshToken)
        `when`(userRepository.save(any())).thenReturn(testUser)
        doNothing().`when`(auditService).logLogin(any(), any(), any(), any(), any())

        // Act
        val result = authenticationService.authenticate(loginRequest, httpServletRequest)

        // Assert
        assertNotNull(result)
        assertEquals(testAccessToken, result.accessToken)
        verify(userRepository).findByEmail(loginRequest.emailOrUsername)
        verify(userRepository).findByUsername(loginRequest.emailOrUsername)
    }

    @Test
    fun `authenticate should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val loginRequest = LoginRequest(
            emailOrUsername = "nonexistent@example.com",
            password = "password123"
        )

        `when`(userRepository.findByEmail(loginRequest.emailOrUsername)).thenReturn(Optional.empty())
        `when`(userRepository.findByUsername(loginRequest.emailOrUsername)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<ResourceNotFoundException> {
            authenticationService.authenticate(loginRequest, httpServletRequest)
        }

        assertTrue(exception.message!!.contains("credentials"))
        verify(userRepository).findByEmail(loginRequest.emailOrUsername)
        verify(userRepository).findByUsername(loginRequest.emailOrUsername)
        verify(auditService, never()).logLogin(any(), any(), any(), any(), any())
    }

    @Test
    fun `authenticate should throw BusinessValidationException when user is inactive`() {
        // Arrange
        val inactiveUser = testUser.copy(active = false)
        val loginRequest = LoginRequest(
            emailOrUsername = "test@example.com",
            password = "password123"
        )

        `when`(userRepository.findByEmail(loginRequest.emailOrUsername)).thenReturn(Optional.of(inactiveUser))
        doNothing().`when`(auditService).logLogin(any(), any(), any(), any(), any())

        // Act & Assert
        val exception = assertThrows<BusinessValidationException> {
            authenticationService.authenticate(loginRequest, httpServletRequest)
        }

        assertTrue(exception.message!!.contains("inactive"))
        verify(auditService).logLogin(any(), any(), any(), any(), eq(false))
        verify(passwordEncoder, never()).matches(any(), any())
    }

    @Test
    fun `authenticate should throw BusinessValidationException when password is incorrect`() {
        // Arrange
        val loginRequest = LoginRequest(
            emailOrUsername = "test@example.com",
            password = "wrongPassword"
        )

        `when`(userRepository.findByEmail(loginRequest.emailOrUsername)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(loginRequest.password, testUser.password)).thenReturn(false)
        doNothing().`when`(auditService).logLogin(any(), any(), any(), any(), any())

        // Act & Assert
        val exception = assertThrows<BusinessValidationException> {
            authenticationService.authenticate(loginRequest, httpServletRequest)
        }

        assertTrue(exception.message!!.contains("credentials"))
        verify(passwordEncoder).matches(loginRequest.password, testUser.password)
        verify(auditService).logLogin(any(), any(), any(), any(), eq(false))
        verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any())
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Test
    fun `refreshToken should return new access token when refresh token is valid`() {
        // Arrange
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = testRefreshToken)

        `when`(jwtTokenProvider.validateToken(testRefreshToken)).thenReturn(true)
        `when`(jwtTokenProvider.getUserIdFromToken(testRefreshToken)).thenReturn(testUserId.toString())
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn(testAccessToken)

        // Act
        val result = authenticationService.refreshToken(refreshTokenRequest)

        // Assert
        assertNotNull(result)
        assertEquals(testAccessToken, result.accessToken)
        assertEquals(86400L, result.expiresIn)
        verify(jwtTokenProvider).validateToken(testRefreshToken)
        verify(jwtTokenProvider).getUserIdFromToken(testRefreshToken)
        verify(userRepository).findById(testUserId)
        verify(jwtTokenProvider).generateAccessToken(any(), any(), any(), any(), any())
    }

    @Test
    fun `refreshToken should throw BusinessValidationException when token is invalid`() {
        // Arrange
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = "invalid.token")

        `when`(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false)

        // Act & Assert
        val exception = assertThrows<BusinessValidationException> {
            authenticationService.refreshToken(refreshTokenRequest)
        }

        assertTrue(exception.message!!.contains("Invalid or expired"))
        verify(jwtTokenProvider).validateToken("invalid.token")
        verify(jwtTokenProvider, never()).getUserIdFromToken(any())
    }

    @Test
    fun `refreshToken should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = testRefreshToken)

        `when`(jwtTokenProvider.validateToken(testRefreshToken)).thenReturn(true)
        `when`(jwtTokenProvider.getUserIdFromToken(testRefreshToken)).thenReturn(testUserId.toString())
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            authenticationService.refreshToken(refreshTokenRequest)
        }

        verify(userRepository).findById(testUserId)
        verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any())
    }

    @Test
    fun `refreshToken should throw BusinessValidationException when user is inactive`() {
        // Arrange
        val inactiveUser = testUser.copy(active = false)
        val refreshTokenRequest = RefreshTokenRequest(refreshToken = testRefreshToken)

        `when`(jwtTokenProvider.validateToken(testRefreshToken)).thenReturn(true)
        `when`(jwtTokenProvider.getUserIdFromToken(testRefreshToken)).thenReturn(testUserId.toString())
        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(inactiveUser))

        // Act & Assert
        val exception = assertThrows<BusinessValidationException> {
            authenticationService.refreshToken(refreshTokenRequest)
        }

        assertTrue(exception.message!!.contains("inactive"))
        verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any())
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    fun `changePassword should change password successfully when current password is correct`() {
        // Arrange
        val passwordChangeRequest = PasswordChangeRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )

        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(passwordChangeRequest.currentPassword, testUser.password)).thenReturn(true)
        `when`(passwordEncoder.encode(passwordChangeRequest.newPassword)).thenReturn("newEncodedPassword")
        `when`(userRepository.save(any())).thenReturn(testUser)
        doNothing().`when`(auditService).logPasswordChange(any(), any(), any(), any())

        // Act
        val result = authenticationService.changePassword(testUserId, passwordChangeRequest, httpServletRequest)

        // Assert
        assertNotNull(result)
        assertTrue(result.success)
        assertEquals("Password changed successfully", result.message)
        verify(userRepository).findById(testUserId)
        verify(passwordEncoder).matches(passwordChangeRequest.currentPassword, testUser.password)
        verify(passwordEncoder).encode(passwordChangeRequest.newPassword)
        verify(userRepository).save(any())
        verify(auditService).logPasswordChange(any(), any(), any(), eq(true))
    }

    @Test
    fun `changePassword should throw BusinessValidationException when current password is incorrect`() {
        // Arrange
        val passwordChangeRequest = PasswordChangeRequest(
            currentPassword = "wrongPassword",
            newPassword = "newPassword123"
        )

        `when`(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        `when`(passwordEncoder.matches(passwordChangeRequest.currentPassword, testUser.password)).thenReturn(false)
        doNothing().`when`(auditService).logPasswordChange(any(), any(), any(), any())

        // Act & Assert
        val exception = assertThrows<BusinessValidationException> {
            authenticationService.changePassword(testUserId, passwordChangeRequest, httpServletRequest)
        }

        assertTrue(exception.message!!.contains("Current password is incorrect"))
        verify(passwordEncoder).matches(passwordChangeRequest.currentPassword, testUser.password)
        verify(auditService).logPasswordChange(any(), any(), any(), eq(false))
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `changePassword should throw ResourceNotFoundException when user not found`() {
        // Arrange
        val passwordChangeRequest = PasswordChangeRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )

        `when`(userRepository.findById(testUserId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            authenticationService.changePassword(testUserId, passwordChangeRequest, httpServletRequest)
        }

        verify(userRepository).findById(testUserId)
        verify(passwordEncoder, never()).matches(any(), any())
        verify(userRepository, never()).save(any())
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    fun `validateToken should return true when token is valid`() {
        // Arrange
        `when`(jwtTokenProvider.validateToken(testAccessToken)).thenReturn(true)

        // Act
        val result = authenticationService.validateToken(testAccessToken)

        // Assert
        assertTrue(result)
        verify(jwtTokenProvider).validateToken(testAccessToken)
    }

    @Test
    fun `validateToken should return false when token is invalid`() {
        // Arrange
        `when`(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false)

        // Act
        val result = authenticationService.validateToken("invalid.token")

        // Assert
        assertFalse(result)
        verify(jwtTokenProvider).validateToken("invalid.token")
    }

    // ==================== GET USER ID FROM TOKEN TESTS ====================

    @Test
    fun `getUserIdFromToken should return user ID when token is valid`() {
        // Arrange
        `when`(jwtTokenProvider.getUserIdFromToken(testAccessToken)).thenReturn(testUserId.toString())

        // Act
        val result = authenticationService.getUserIdFromToken(testAccessToken)

        // Assert
        assertEquals(testUserId, result)
        verify(jwtTokenProvider).getUserIdFromToken(testAccessToken)
    }

    @Test
    fun `getUserIdFromToken should throw exception when token contains invalid UUID`() {
        // Arrange
        `when`(jwtTokenProvider.getUserIdFromToken(testAccessToken)).thenReturn("invalid-uuid")

        // Act & Assert
        assertThrows<IllegalArgumentException> {
            authenticationService.getUserIdFromToken(testAccessToken)
        }

        verify(jwtTokenProvider).getUserIdFromToken(testAccessToken)
    }
}

