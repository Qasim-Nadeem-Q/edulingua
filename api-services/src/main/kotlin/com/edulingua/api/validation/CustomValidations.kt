package com.edulingua.api.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Custom validation annotation for strong passwords
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [StrongPasswordValidator::class])
annotation class StrongPassword(
    val message: String = "Password must contain at least 8 characters, including uppercase, lowercase, number and special character",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class StrongPasswordValidator : ConstraintValidator<StrongPassword, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return ValidationHelper.isStrongPassword(value)
    }
}

/**
 * Custom validation annotation for valid codes (state, district, school codes)
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidCodeValidator::class])
annotation class ValidCode(
    val message: String = "Code must be uppercase alphanumeric with optional hyphens",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidCodeValidator : ConstraintValidator<ValidCode, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // Use @NotNull for null checks
        return ValidationHelper.isValidCode(value)
    }
}

/**
 * Custom validation annotation for phone numbers
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneNumberValidator::class])
annotation class PhoneNumber(
    val message: String = "Invalid phone number format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PhoneNumberValidator : ConstraintValidator<PhoneNumber, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return ValidationHelper.isValidPhoneNumber(value)
    }
}

/**
 * Custom validation annotation for roll numbers
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RollNumberValidator::class])
annotation class RollNumber(
    val message: String = "Invalid roll number format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class RollNumberValidator : ConstraintValidator<RollNumber, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return ValidationHelper.isValidRollNumber(value)
    }
}
