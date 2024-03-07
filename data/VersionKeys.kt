/**
 * КЛЮЧИ НЕ МЕНЯТЬ.
 */

/**
 * Ключ словаря версионирования с набором критически совместимых поддерживаемых версий МП.
 *
 * Формат записей: "id приложения": "номер версии"
 * Например: "ru.tensor.sbis.droid": "21.3119"
 */
internal const val CRITICAL_DICTIONARY_KEY = "versions"

/**
 * Ключ словаря версионирования с набором опубликованных версий МП.
 *
 * Формат записей: "id приложения": "номер версии"
 * Например: "ru.tensor.sbis.storekeeper": "22.1227",
 */
internal const val MARKET_DICTIONARY_KEY = "published_versions"
