package com.edulingua.core.service

import com.edulingua.core.exception.BusinessValidationException
import com.edulingua.core.exception.ResourceAlreadyExistsException
import com.edulingua.core.exception.ResourceNotFoundException
import com.edulingua.core.models.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service layer for consumer management operations.
 * Handles hierarchical role-based consumer accounts.
 */
@Service
@Transactional
class ConsumerService(
    private val consumerRepository: ConsumerRepository
) {

    /**
     * Creates a new consumer account.
     */
    fun createConsumer(consumer: Consumer): ConsumerResponse {
        if (consumerRepository.existsByEmail(consumer.email)) {
            throw ResourceAlreadyExistsException("Consumer with email ${consumer.email} already exists")
        }

        // Validate hierarchy based on role
        if (!consumer.isValidHierarchy()) {
            throw BusinessValidationException(
                "Invalid hierarchy data for role ${consumer.role.description}. " +
                "Please provide all required location information."
            )
        }

        // TODO: Hash password before saving
        val savedConsumer = consumerRepository.save(consumer)
        return savedConsumer.toResponse()
    }

    /**
     * Retrieves a consumer by ID.
     */
    @Transactional(readOnly = true)
    fun getConsumerById(id: Long): ConsumerResponse {
        val consumer = consumerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Consumer with id $id not found") }
        return consumer.toResponse()
    }

    /**
     * Retrieves a consumer by email.
     */
    @Transactional(readOnly = true)
    fun getConsumerByEmail(email: String): ConsumerResponse {
        val consumer = consumerRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("Consumer with email $email not found") }
        return consumer.toResponse()
    }

    /**
     * Retrieves all consumers.
     */
    @Transactional(readOnly = true)
    fun getAllConsumers(): List<ConsumerResponse> {
        return consumerRepository.findAll().map { it.toResponse() }
    }

    /**
     * Retrieves all active consumers.
     */
    @Transactional(readOnly = true)
    fun getActiveConsumers(): List<ConsumerResponse> {
        return consumerRepository.findByActiveTrue().map { it.toResponse() }
    }

    /**
     * Retrieves consumers by role.
     */
    @Transactional(readOnly = true)
    fun getConsumersByRole(role: Role): List<ConsumerResponse> {
        return consumerRepository.findByRole(role).map { it.toResponse() }
    }

    /**
     * Retrieves consumers by state.
     */
    @Transactional(readOnly = true)
    fun getConsumersByState(stateCode: String): List<ConsumerResponse> {
        return consumerRepository.findByStateCode(stateCode).map { it.toResponse() }
    }

    /**
     * Retrieves consumers by district.
     */
    @Transactional(readOnly = true)
    fun getConsumersByDistrict(districtCode: String): List<ConsumerResponse> {
        return consumerRepository.findByDistrictCode(districtCode).map { it.toResponse() }
    }

    /**
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
