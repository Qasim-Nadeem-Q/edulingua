package com.edulingua.api.advice

import com.edulingua.api.model.ApiErrorResponse
import com.edulingua.api.model.ApiErrorResponseBuilder
import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.exception.OperationNotPermittedException
import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*

/**
 * Centralized exception handling advice for REST controllers.
 * Provides consistent error responses across all API endpoints.
 * Follows enterprise-grade exception handling patterns.
 */
@RestControllerAdvice
class RestExceptionAdvice {

    private val logger = LoggerFactory.getLogger(RestExceptionAdvice::class.java)

    /**
     * Handles ResourceNotFoundException - returns 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logger.warn("Resource not found: ${ex.message}", ex)

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("Resource Not Found")
            .message(ex.message ?: "The requested resource was not found")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Handles ResourceAlreadyExistsException - returns 409 Conflict
     */
    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExists(
        ex: ResourceAlreadyExistsException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logger.warn("Resource already exists: ${ex.message}", ex)

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.CONFLICT.value())
            .error("Resource Already Exists")
            .message(ex.message ?: "The resource already exists")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    /**
     * Handles BusinessValidationException - returns 400 Bad Request
     */
    @ExceptionHandler(BusinessValidationException::class)
    fun handleBusinessValidation(
        ex: BusinessValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logger.warn("Business validation failed: ${ex.message}", ex)

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Validation Failed")
            .message(ex.message ?: "Business validation failed")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles OperationNotPermittedException - returns 403 Forbidden
     */
    @ExceptionHandler(OperationNotPermittedException::class)
    fun handleOperationNotPermitted(
        ex: OperationNotPermittedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logger.warn("Operation not permitted: ${ex.message}", ex)

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.FORBIDDEN.value())
            .error("Operation Not Permitted")
            .message(ex.message ?: "You do not have permission to perform this operation")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    /**
     * Handles validation errors from @Valid annotations - returns 422 Unprocessable Entity
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logger.warn("Validation failed: ${ex.message}")

        val validationErrors = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }

        val errorMessage = validationErrors.entries
            .joinToString("; ") { "${it.key}: ${it.value}" }

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error("Validation Failed")
            .message(errorMessage)
            .path(request.requestURI)
            .details(validationErrors)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    /**
     * Handles IllegalArgumentException - returns 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        logger.warn("Illegal argument: ${ex.message}", ex)

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Invalid Request")
            .message(ex.message ?: "Invalid request parameters")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles all other unhandled exceptions - returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val traceId = generateTraceId()
        logger.error("Unexpected error occurred. TraceId: $traceId", ex)

        val errorResponse = ApiErrorResponseBuilder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please contact support with trace ID: $traceId")
            .path(request.requestURI)
            .traceId(traceId)
            .build()

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    /**
     * Generates a unique trace ID for error tracking and debugging
     */
    private fun generateTraceId(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }
}
