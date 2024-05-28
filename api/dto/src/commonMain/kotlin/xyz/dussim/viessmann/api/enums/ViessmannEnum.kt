package xyz.dussim.viessmann.api.enums

/**
 * Base interface for all DTO enums of viessmann api.
 * */
sealed interface ViessmannEnum {
    /**
     * @param name is the value of literal string used as enum value in viessmann apis
     * */
    val name: String
}
