package xyz.dussim.viessmann.feature.api.validation

/**
 * Validation contract used by generated feature and command implementations.
 *
 * Regular validation may aggregate multiple errors, while [validateFailFast] returns after the first invalid rule.
 */
interface GeneratedValidationRule<T, E> : ValidationRule<T, E> {
    fun validateFailFast(value: T): ValidationResult<E>
}
