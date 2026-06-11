package com.anadolstudio.homehub.feature.sceneCreate.util

/**
 * Транслитерация и slugify для имени сцены → sceneConfigId.
 * HA принимает только [a-z0-9_], кириллицу транслитерируем по упрощённой схеме (yo/zh/sch/...).
 *
 * "Вечерний свет" → "vecherniy_svet"
 * "Evening light!" → "evening_light"
 * "" → ""
 */
internal fun slugify(name: String): String {
    val sb = StringBuilder()
    for (ch in name.lowercase()) {
        val mapped = RU_TO_LATIN[ch] ?: ch.toString()
        for (c in mapped) {
            when {
                c in 'a'..'z' || c in '0'..'9' -> sb.append(c)
                else -> sb.append('_')
            }
        }
    }
    return sb.toString()
            .replace(Regex("_+"), "_")
            .trim('_')
}

private val RU_TO_LATIN: Map<Char, String> = mapOf(
        'а' to "a",
        'б' to "b",
        'в' to "v",
        'г' to "g",
        'д' to "d",
        'е' to "e",
        'ё' to "yo",
        'ж' to "zh",
        'з' to "z",
        'и' to "i",
        'й' to "y",
        'к' to "k",
        'л' to "l",
        'м' to "m",
        'н' to "n",
        'о' to "o",
        'п' to "p",
        'р' to "r",
        'с' to "s",
        'т' to "t",
        'у' to "u",
        'ф' to "f",
        'х' to "h",
        'ц' to "ts",
        'ч' to "ch",
        'ш' to "sh",
        'щ' to "sch",
        'ъ' to "",
        'ы' to "y",
        'ь' to "",
        'э' to "e",
        'ю' to "yu",
        'я' to "ya",
)
