package net.guneyilmaz0.mongos

import java.util.regex.Pattern

class CaseInsensitiveString(private val string: String) {
    fun compile(): Pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE)
}
