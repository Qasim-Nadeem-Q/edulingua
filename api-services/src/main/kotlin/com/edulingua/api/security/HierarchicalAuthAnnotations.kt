package com.edulingua.api.security

/**
 * Annotation to require hierarchical authorization for user management.
 * Ensures managers can only access users within their organizational scope.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireHierarchicalAccess(
    val targetUserIdParam: String = "id"
)

/**
 * Annotation to allow users to access their own resources.
 * If not accessing own resource, falls back to hierarchical check.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AllowSelfAccess(
    val targetUserIdParam: String = "id"
)

/**
 * Annotation to require state-level access.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireStateAccess(
    val stateCodeParam: String = "stateCode"
)

/**
 * Annotation to require district-level access.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireDistrictAccess(
    val districtCodeParam: String = "districtCode"
)

/**
 * Annotation to require school-level access.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireSchoolAccess(
    val schoolCodeParam: String = "schoolCode"
)

/**
 * Annotation to require class-level access.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireClassAccess(
    val schoolCodeParam: String = "schoolCode",
    val classCodeParam: String = "classCode"
)

