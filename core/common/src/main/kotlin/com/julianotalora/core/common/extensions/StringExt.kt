package com.julianotalora.core.common.extensions

import java.text.Normalizer

/**
 * Normalizes a string for search operations by converting to lowercase and removing diacritics
 */
fun String.toSearchNorm(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase()
}
