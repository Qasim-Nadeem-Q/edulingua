package com.edulingua.api.security

import com.edulingua.api.filter.AuthContext
import com.edulingua.core.exception.OperationNotPermittedException
import com.edulingua.core.service.AuthorizationService
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * Aspect for enforcing hierarchical authorization.
 * Ensures users can only access resources within their organizational scope.
 */
@Aspect
@Component
class HierarchicalAuthAspect(
    private val authorizationService: AuthorizationService
) {

    /**
     * Enforces @RequireHierarchicalAccess annotation.
     * Validates that the current user can manage the target user based on hierarchy.
     */
    @Around("@annotation(requireHierarchicalAccess)")
    fun checkHierarchicalAccess(
        joinPoint: ProceedingJoinPoint,
        requireHierarchicalAccess: RequireHierarchicalAccess
    ): Any? {
        val request = getCurrentRequest()
        val currentUserId = AuthContext.getUserId(request)
            ?: throw OperationNotPermittedException("Authentication required")

        val targetUserId = extractTargetUserId(joinPoint, requireHierarchicalAccess.targetUserIdParam)
            ?: throw OperationNotPermittedException("Target user ID not found in request")

        authorizationService.requireCanManageUser(currentUserId, targetUserId)
        return joinPoint.proceed()
    }

    /**
     * Enforces @AllowSelfAccess annotation.
     * Allows access if user is accessing their own resource OR has hierarchical permission.
     */
    @Around("@annotation(allowSelfAccess)")
    fun checkSelfAccess(
        joinPoint: ProceedingJoinPoint,
        allowSelfAccess: AllowSelfAccess
    ): Any? {
        val request = getCurrentRequest()
        val currentUserId = AuthContext.getUserId(request)
            ?: throw OperationNotPermittedException("Authentication required")

        val targetUserId = extractTargetUserId(joinPoint, allowSelfAccess.targetUserIdParam)
            ?: throw OperationNotPermittedException("Target user ID not found in request")

        // Allow if accessing own resource
        if (authorizationService.isResourceOwner(currentUserId, targetUserId)) {
            return joinPoint.proceed()
        }

        // Otherwise check hierarchical access
        authorizationService.requireCanManageUser(currentUserId, targetUserId)
        return joinPoint.proceed()
    }

    /**
     * Enforces @RequireStateAccess annotation.
     */
    @Around("@annotation(requireStateAccess)")
    fun checkStateAccess(
        joinPoint: ProceedingJoinPoint,
        requireStateAccess: RequireStateAccess
    ): Any? {
        val request = getCurrentRequest()
        val currentUserId = AuthContext.getUserId(request)
            ?: throw OperationNotPermittedException("Authentication required")

        val stateCode = extractParameter(joinPoint, requireStateAccess.stateCodeParam)
            ?: throw OperationNotPermittedException("State code not found in request")

        if (!authorizationService.canAccessState(currentUserId, stateCode)) {
            throw OperationNotPermittedException("You don't have access to state: $stateCode")
        }

        return joinPoint.proceed()
    }

    /**
     * Enforces @RequireDistrictAccess annotation.
     */
    @Around("@annotation(requireDistrictAccess)")
    fun checkDistrictAccess(
        joinPoint: ProceedingJoinPoint,
        requireDistrictAccess: RequireDistrictAccess
    ): Any? {
        val request = getCurrentRequest()
        val currentUserId = AuthContext.getUserId(request)
            ?: throw OperationNotPermittedException("Authentication required")

        val districtCode = extractParameter(joinPoint, requireDistrictAccess.districtCodeParam)
            ?: throw OperationNotPermittedException("District code not found in request")

        if (!authorizationService.canAccessDistrict(currentUserId, districtCode)) {
            throw OperationNotPermittedException("You don't have access to district: $districtCode")
        }

        return joinPoint.proceed()
    }

    /**
     * Enforces @RequireSchoolAccess annotation.
     */
    @Around("@annotation(requireSchoolAccess)")
    fun checkSchoolAccess(
        joinPoint: ProceedingJoinPoint,
        requireSchoolAccess: RequireSchoolAccess
    ): Any? {
        val request = getCurrentRequest()
        val currentUserId = AuthContext.getUserId(request)
            ?: throw OperationNotPermittedException("Authentication required")

        val schoolCode = extractParameter(joinPoint, requireSchoolAccess.schoolCodeParam)
            ?: throw OperationNotPermittedException("School code not found in request")

        if (!authorizationService.canAccessSchool(currentUserId, schoolCode)) {
            throw OperationNotPermittedException("You don't have access to school: $schoolCode")
        }

        return joinPoint.proceed()
    }

    /**
     * Enforces @RequireClassAccess annotation.
     */
    @Around("@annotation(requireClassAccess)")
    fun checkClassAccess(
        joinPoint: ProceedingJoinPoint,
        requireClassAccess: RequireClassAccess
    ): Any? {
        val request = getCurrentRequest()
        val currentUserId = AuthContext.getUserId(request)
            ?: throw OperationNotPermittedException("Authentication required")

        val schoolCode = extractParameter(joinPoint, requireClassAccess.schoolCodeParam)
            ?: throw OperationNotPermittedException("School code not found in request")

        val classCode = extractParameter(joinPoint, requireClassAccess.classCodeParam)
            ?: throw OperationNotPermittedException("Class code not found in request")

        if (!authorizationService.canAccessClass(currentUserId, schoolCode, classCode)) {
            throw OperationNotPermittedException("You don't have access to class: $classCode in school: $schoolCode")
        }

        return joinPoint.proceed()
    }

    private fun getCurrentRequest(): HttpServletRequest {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw IllegalStateException("No request context available")
        return attributes.request
    }

    private fun extractTargetUserId(joinPoint: ProceedingJoinPoint, paramName: String): UUID? {
        val signature = joinPoint.signature as MethodSignature
        val parameterNames = signature.parameterNames
        val args = joinPoint.args

        val index = parameterNames.indexOf(paramName)
        if (index >= 0 && index < args.size) {
            return when (val arg = args[index]) {
                is UUID -> arg
                is String -> try { UUID.fromString(arg) } catch (e: Exception) { null }
                else -> null
            }
        }

        return null
    }

    private fun extractParameter(joinPoint: ProceedingJoinPoint, paramName: String): String? {
        val signature = joinPoint.signature as MethodSignature
        val parameterNames = signature.parameterNames
        val args = joinPoint.args

        val index = parameterNames.indexOf(paramName)
        if (index >= 0 && index < args.size) {
            return args[index]?.toString()
        }

        return null
    }
}

