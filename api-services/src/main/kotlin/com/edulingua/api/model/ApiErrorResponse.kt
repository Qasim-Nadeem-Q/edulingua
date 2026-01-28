package com.edulingua.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * Standardized API error response model following RFC 7807 Problem Details specification.
 * Provides consistent error information across all API endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val details: Map<String, Any>? = null,
    val traceId: String? = null
)

/**
 * Builder for creating API error responses with a fluent interface.
 */
class ApiErrorResponseBuilder {
    private var status: Int = 500
    private var error: String = "Internal Server Error"
    private var message: String = "An unexpected error occurred"
    private var path: String? = null
    private var details: Map<String, Any>? = null
    private var traceId: String? = null

    fun status(status: Int) = apply { this.status = status }
    fun error(error: String) = apply { this.error = error }
    fun message(message: String) = apply { this.message = message }
    fun path(path: String?) = apply { this.path = path }
    fun details(details: Map<String, Any>?) = apply { this.details = details }
    fun traceId(traceId: String?) = apply { this.traceId = traceId }

    fun build() = ApiErrorResponse(
        status = status,
        error = error,
        message = message,
        path = path,
        details = details,
        traceId = traceId
    )
}
