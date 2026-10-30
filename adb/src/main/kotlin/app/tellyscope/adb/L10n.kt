// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.adb

import java.util.Locale

/**
 * Simplified Chinese for a phone set to 简体中文: `zh`, `zh-CN`, `zh-SG`, or script `Hans`.
 * Traditional (`zh-TW`, `zh-HK`, `zh-MO`, script `Hant`) stays on the English copy; this fork
 * does not ship a Traditional translation.
 */
fun simplifiedChinese(locale: Locale = Locale.getDefault()): Boolean {
    if (!locale.language.equals("zh", ignoreCase = true)) return false
    val script = locale.script
    if (script.equals("Hant", ignoreCase = true)) return false
    if (script.equals("Hans", ignoreCase = true)) return true
    return when (locale.country.uppercase(Locale.ROOT)) {
        "TW", "HK", "MO" -> false
        else -> true
    }
}

/** English unless [locale] is Simplified Chinese. Both arguments are always evaluated. */
fun tr(en: String, zh: String, locale: Locale = Locale.getDefault()): String =
    if (simplifiedChinese(locale)) zh else en
