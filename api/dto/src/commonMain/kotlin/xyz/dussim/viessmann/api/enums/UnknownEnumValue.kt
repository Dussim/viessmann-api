package xyz.dussim.viessmann.api.enums

/**
 * Marker interface for representing unknown enum values.
 *
 * This interface is used to denote instances of enums that are not recognized
 * by the library. Implementations of this interface serve as placeholders for
 * unknown values returned by the API.
 */
sealed interface UnknownEnumValue : ViessmannEnum
