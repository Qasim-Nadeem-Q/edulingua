package com.edulingua.api.controller

import com.edulingua.core.models.Consumer
import com.edulingua.core.models.ConsumerResponse
import com.edulingua.core.models.ConsumerUpdateRequest
import com.edulingua.core.models.Role
import com.edulingua.core.service.ConsumerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for consumer management.
 * Handles CRUD operations for test-taker accounts with hierarchical roles.
 */
@RestController
@RequestMapping("/api/v1/consumers")
class ConsumerController(
    private val consumerService: ConsumerService
) {

    @PostMapping
    fun createConsumer(@Valid @RequestBody consumer: Consumer): ResponseEntity<ConsumerResponse> {
        val created = consumerService.createConsumer(consumer)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{id}")
    fun getConsumerById(@PathVariable id: Long): ResponseEntity<ConsumerResponse> {
        val consumer = consumerService.getConsumerById(id)
        return ResponseEntity.ok(consumer)
    }

    @GetMapping("/email/{email}")
    fun getConsumerByEmail(@PathVariable email: String): ResponseEntity<ConsumerResponse> {
        val consumer = consumerService.getConsumerByEmail(email)
        return ResponseEntity.ok(consumer)
    }

    @GetMapping
    fun getAllConsumers(@RequestParam(required = false, defaultValue = "false") activeOnly: Boolean): ResponseEntity<List<ConsumerResponse>> {
        val consumers = if (activeOnly) {
            consumerService.getActiveConsumers()
        } else {
            consumerService.getAllConsumers()
        }
        return ResponseEntity.ok(consumers)
    }

    @GetMapping("/role/{roleCode}")
    fun getConsumersByRole(@PathVariable roleCode: String): ResponseEntity<List<ConsumerResponse>> {
        val role = Role.fromCode(roleCode)
            ?: return ResponseEntity.badRequest().build()

        val consumers = consumerService.getConsumersByRole(role)
        return ResponseEntity.ok(consumers)
    }

    @GetMapping("/state/{stateCode}")
    fun getConsumersByState(@PathVariable stateCode: String): ResponseEntity<List<ConsumerResponse>> {
        val consumers = consumerService.getConsumersByState(stateCode)
        return ResponseEntity.ok(consumers)
    }

    @GetMapping("/district/{districtCode}")
    fun getConsumersByDistrict(@PathVariable districtCode: String): ResponseEntity<List<ConsumerResponse>> {
        val consumers = consumerService.getConsumersByDistrict(districtCode)
        return ResponseEntity.ok(consumers)
    }

    @GetMapping("/school/{schoolCode}")
    fun getConsumersBySchool(@PathVariable schoolCode: String): ResponseEntity<List<ConsumerResponse>> {
        val consumers = consumerService.getConsumersBySchool(schoolCode)
        return ResponseEntity.ok(consumers)
    }

    @GetMapping("/school/{schoolCode}/class/{classCode}/students")
    fun getStudentsByClass(
        @PathVariable schoolCode: String,
        @PathVariable classCode: String
    ): ResponseEntity<List<ConsumerResponse>> {
        val students = consumerService.getStudentsByClass(schoolCode, classCode)
        return ResponseEntity.ok(students)
    }

    @PutMapping("/{id}")
    fun updateConsumer(
        @PathVariable id: Long,
        @Valid @RequestBody updateRequest: ConsumerUpdateRequest
    ): ResponseEntity<ConsumerResponse> {
        val updated = consumerService.updateConsumer(id, updateRequest)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteConsumer(@PathVariable id: Long): ResponseEntity<Void> {
        consumerService.deleteConsumer(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/deactivate")
    fun deactivateConsumer(@PathVariable id: Long): ResponseEntity<ConsumerResponse> {
        val deactivated = consumerService.deactivateConsumer(id)
        return ResponseEntity.ok(deactivated)
    }
}
