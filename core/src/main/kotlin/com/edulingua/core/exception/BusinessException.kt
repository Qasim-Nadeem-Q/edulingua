package com.edulingua.core.exception

/**
 * Base exception for all business logic exceptions in the application.
 * This allows centralized exception handling and proper error responses.
 */
sealed class BusinessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when a requested resource is not found.
 */
class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Exception thrown when a resource already exists (e.g., duplicate email).
 */
class ResourceAlreadyExistsException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Exception thrown when business validation rules are violated.
 */
class BusinessValidationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Exception thrown when operation is not permitted due to business rules.
 */
class OperationNotPermittedException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)
