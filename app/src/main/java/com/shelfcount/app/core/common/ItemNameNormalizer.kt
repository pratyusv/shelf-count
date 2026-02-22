package com.shelfcount.app.core.common

import java.util.Locale

fun normalizeItemName(rawName: String): String = rawName.trim().replace("\\s+".toRegex(), " ")

fun normalizeItemNameKey(rawName: String): String = normalizeItemName(rawName).lowercase(Locale.ROOT)
