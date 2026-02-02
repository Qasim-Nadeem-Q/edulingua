package com.edulingua.core.service

/**
 * DEPRECATED: This service has been merged into UserService.
 * Please use UserService for all user management operations.
 *
 * AdminUser and Consumer models have been unified into a single User model
 * with role-based access control (RBAC).
 *
 * @see UserService
 */
@Deprecated(
    message = "Use UserService instead",
    replaceWith = ReplaceWith("UserService"),
    level = DeprecationLevel.ERROR
)
class ConsumerService    /**
     * Retrieves consumers by school.
     */
    @Transactional(readOnly = true)
    fun getConsumersBySchool(schoolCode: String): List<ConsumerResponse> {
        return consumerRepository.findBySchoolCode(schoolCode).map { it.toResponse() }
    }

    /**
     * Retrieves students by class.
     */
    @Transactional(readOnly = true)
    fun getStudentsByClass(schoolCode: String, classCode: String): List<ConsumerResponse> {
        return consumerRepository.findStudentsByClass(schoolCode, classCode).map { it.toResponse() }
    }

    /**
     * Updates an existing consumer.
     */
    fun updateConsumer(id: Long, updateRequest: ConsumerUpdateRequest): ConsumerResponse {
        val consumer = consumerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Consumer with id $id not found") }

        val updatedConsumer = consumer.updateWith(
            name = updateRequest.name,
            phoneNumber = updateRequest.phoneNumber,
            active = updateRequest.active,
            parentEmail = updateRequest.parentEmail
        )

        val savedConsumer = consumerRepository.save(updatedConsumer)
        return savedConsumer.toResponse()
    }

    /**
     * Deletes a consumer.
     */
    fun deleteConsumer(id: Long) {
        if (!consumerRepository.existsById(id)) {
            throw ResourceNotFoundException("Consumer with id $id not found")
        }
        consumerRepository.deleteById(id)
    }

    /**
     * Deactivates a consumer instead of deleting.
     */
    fun deactivateConsumer(id: Long): ConsumerResponse {
        val consumer = consumerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Consumer with id $id not found") }

        val deactivatedConsumer = consumer.updateWith(active = false)
        val savedConsumer = consumerRepository.save(deactivatedConsumer)
        return savedConsumer.toResponse()
    }
}
