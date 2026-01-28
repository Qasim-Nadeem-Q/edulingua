package com.edulingua.api.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Standardized API success response wrapper for consistent response structure.
 * Provides metadata along with the actual data payload.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null,
    val metadata: Map<String, Any>? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(success = true, data = data, message = message)
        }

        fun <T> success(data: T, message: String?, metadata: Map<String, Any>): ApiResponse<T> {
            return ApiResponse(success = true, data = data, message = message, metadata = metadata)
        }

        fun empty(message: String): ApiResponse<Unit> {
            return ApiResponse(success = true, data = null, message = message)
        }
    }
}
