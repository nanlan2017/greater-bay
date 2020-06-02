package com.jheng.bay.base.model

interface HasLocale {

    companion object {
        const val en_US = "en-US"
        const val zh_CN = "zh-CN"
        const val en_zh = "en-zh"
    }

    /**
     * End-User's locale, represented as a BCP47 (RFC5646) language tag.
     * This is typically an ISO 639-1 Alpha-2 [ISO639‑1] language code in lowercase
     * and an ISO 3166-1 Alpha-2 [ISO3166‑1] country code in uppercase, separated by a dash.
     * For example, en-US or fr-CA.
     */
    val locale: String?

    val language_code: String?
        get() = locale?.split("-")?.firstOrNull()

    fun <T : HasLocale> pick_proper_locale(others: Collection<T>): T? {
        if (others.isEmpty()) return null

        val loc = locale ?: en_US
        val full_matched = others.find { it.locale == loc }
        if (full_matched != null) return full_matched

        val lang = loc.slice(0..1)
        val lang_matched = others.find { it.language_code == lang }
        if (lang_matched != null) return lang_matched

        val default_loc = others.find { it.locale == en_US }
        if (default_loc != null) return default_loc

        val default_lang = others.find { it.language_code == "en" }
        if (default_lang != null) return default_lang

        return others.first()
    }
}
