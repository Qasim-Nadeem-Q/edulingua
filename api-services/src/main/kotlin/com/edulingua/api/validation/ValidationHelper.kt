package com.edulingua.api.validation

import java.util.regex.Pattern

/**
 * Validation helper utilities for API layer.
 * Provides reusable validation methods for common patterns.
 */
object ValidationHelper {

    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    private val PHONE_PATTERN = Pattern.compile(
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    )

    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )

    private val ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$")

    /**
     * Validates email format
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return EMAIL_PATTERN.matcher(email).matches()
    }

    /**
     * Validates phone number format
     */
    fun isValidPhoneNumber(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        return PHONE_PATTERN.matcher(phone).matches()
    }

    /**
     * Validates strong password (at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char)
     */
    fun isStrongPassword(password: String?): Boolean {
        if (password.isNullOrBlank()) return false
        return PASSWORD_PATTERN.matcher(password).matches()
    }

    /**
     * Validates basic password (minimum length)
     */
    fun isValidPassword(password: String?, minLength: Int = 8): Boolean {
        return !password.isNullOrBlank() && password.length >= minLength
    }

    /**
     * Validates alphanumeric string
     */
    fun isAlphanumeric(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return ALPHANUMERIC_PATTERN.matcher(value).matches()
    }

    /**
     * Validates string length
     */
    fun isValidLength(value: String?, minLength: Int, maxLength: Int): Boolean {
        if (value.isNullOrBlank()) return false
        return value.length in minLength..maxLength
    }

    /**
     * Validates that string is not blank
     */
    fun isNotBlank(value: String?): Boolean {
        return !value.isNullOrBlank()
    }

    /**
     * Validates code format (uppercase alphanumeric with optional hyphen)
     */
    fun isValidCode(code: String?): Boolean {
        if (code.isNullOrBlank()) return false
        return code.matches(Regex("^[A-Z0-9-]+$"))
    }

    /**
     * Validates roll number format
     */
    fun isValidRollNumber(rollNumber: String?): Boolean {
        if (rollNumber.isNullOrBlank()) return false
        return rollNumber.matches(Regex("^[A-Z0-9/-]+$"))
    }

    /**
     * Sanitizes string input by trimming and removing extra spaces
     */
    fun sanitize(value: String?): String? {
        return value?.trim()?.replace(Regex("\\s+"), " ")
    }

    /**
     * Validates date of birth (must be in the past)
     */
    fun isValidDateOfBirth(date: java.time.LocalDateTime?): Boolean {
        if (date == null) return false
        return date.isBefore(java.time.LocalDateTime.now())
    }
}

/**
 * Extension functions for validation
 */
fun String?.isValidEmail(): Boolean = ValidationHelper.isValidEmail(this)
fun String?.isValidPhoneNumber(): Boolean = ValidationHelper.isValidPhoneNumber(this)
fun String?.isStrongPassword(): Boolean = ValidationHelper.isStrongPassword(this)
fun String?.isValidCode(): Boolean = ValidationHelper.isValidCode(this)
fun String?.sanitized(): String? = ValidationHelper.sanitize(this)
