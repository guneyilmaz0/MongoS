package net.guneyilmaz0.mongos

import java.util.regex.Pattern

/**
 * This class represents a case-insensitive string.
 * It provides a method to compile a string into a Pattern object with case-insensitive matching.
 *
 * @property string the string to be compiled into a Pattern object.
 */
class CaseInsensitiveString(private val string: String) {

    /**
     * Compiles the string into a Pattern object with case-insensitive matching.
     *
     * @return a Pattern object.
     */
    fun compile(): Pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE)

    override fun toString(): String = string

    override fun equals(other: Any?): Boolean =
        other is CaseInsensitiveString && string.equals(other.string, ignoreCase = true)

    override fun hashCode(): Int = string.lowercase().hashCode()
}