package com.edulingua.api.security

import com.edulingua.api.filter.AuthContext
import com.edulingua.core.exception.OperationNotPermittedException
import com.edulingua.core.models.Permission
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Custom annotation for requiring specific permissions
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequirePermission(
    vararg val permissions: String,
    val requireAll: Boolean = false // If true, user must have ALL permissions, else ANY
)

/**
 * Custom annotation for requiring admin role
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireAdmin

/**
 * Custom annotation for requiring consumer role
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireConsumer

/**
 * Aspect for enforcing authorization annotations
 */
@Aspect
@Component
class AuthorizationAspect {

    /**
     * Enforces @RequirePermission annotation
     */
    @Around("@annotation(com.edulingua.api.security.RequirePermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        val request = getCurrentRequest()
        val method = (joinPoint.signature as MethodSignature).method
        val annotation = method.getAnnotation(RequirePermission::class.java)

        val permissionNames = annotation.permissions.map { it.name }.toTypedArray()
        val hasPermission = if (annotation.requireAll) {
            AuthContext.hasAllPermissions(request, *permissionNames)
        } else {
            AuthContext.hasAnyPermission(request, *permissionNames)
        }

        if (!hasPermission) {
            val requiredPerms = annotation.permissions.joinToString(", ") { it.name }
            throw OperationNotPermittedException(
                "Insufficient permissions. Required: $requiredPerms"
            )
        }

        return joinPoint.proceed()
    }

    /**
     * Enforces @RequireAdmin annotation
     */
    @Around("@annotation(com.edulingua.api.security.RequireAdmin)")
    fun checkAdmin(joinPoint: ProceedingJoinPoint): Any? {
        val request = getCurrentRequest()

        if (!AuthContext.isAdmin(request)) {
            throw OperationNotPermittedException("Admin access required")
        }

        return joinPoint.proceed()
    }

    /**
     * Enforces @RequireConsumer annotation
     */
    @Around("@annotation(com.edulingua.api.security.RequireConsumer)")
    fun checkConsumer(joinPoint: ProceedingJoinPoint): Any? {
        val request = getCurrentRequest()

        if (!AuthContext.isConsumer(request)) {
            throw OperationNotPermittedException("Consumer access required")
        }

        return joinPoint.proceed()
    }

    private fun getCurrentRequest(): HttpServletRequest {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw IllegalStateException("No request context available")
        return attributes.request
    }
}
